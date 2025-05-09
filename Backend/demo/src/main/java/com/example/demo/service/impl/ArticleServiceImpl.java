package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.exception.*;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.*;
import com.example.demo.repository.*; // Import all needed repos
import com.example.demo.service.ArticleService;
//import com.example.demo.service.RecommendationService; // For recommendations
import com.example.demo.service.RecommendationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ArticleTagRepository articleTagRepository;
    private final EntityMapper entityMapper;
    private final EntityManager entityManager; // <<< Inject EntityManager
    private final RecommendationService recommendationService;
    private final ArticleVoteRepository articleVoteRepository;
    private final TagNameRepository tagNameRepository;


    @Override
    public ArticleDTO createArticle(ArticleDTO articleDTO, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Article article = entityMapper.toEntity(articleDTO);
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.now());
        article.setScore(0);
        article.setCommentCount(0L);

        // save article to avoid the null article_id while saving the tags
        Article savedArticle = articleRepository.save(article);

        // Handle tags if provided during creation
        if (articleDTO.tags() != null && !articleDTO.tags().isEmpty()) {
            Set<ArticleTag> tags = findOrCreateTags(articleDTO.tags(), savedArticle);
            savedArticle.setTags(tags);
            savedArticle = articleRepository.save(savedArticle); // Save again after setting tags
        }

        return entityMapper.toDTO(savedArticle);
    }

    
    @Override
    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id, Long userId) { // Added userId parameter
        // Removed placeholder userId = 1L;

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        Integer currentUserVote = articleVoteRepository.findByUserIdAndArticleId(userId, id)
                .map(ArticleVote::getValue) // Get the vote value (1 or -1)
                .orElse(0); // Default to 0 if no vote found

        // Map entity to DTO and then add the vote status
        ArticleDTO dto = entityMapper.toDTO(article);
        return new ArticleDTO(
                dto.id(),
                dto.title(),
                dto.summary(),
                dto.content(),
                dto.imageUrls(),
                dto.createdAt(),
                dto.authorId(),
                dto.authorUsername(),
                dto.score(),
                dto.commentCount(),
                dto.tags(),
                currentUserVote // Add the user's vote status
        );
    }

    
    @Override
    @Transactional(readOnly = true)
    public Page<ArticleDTO> getAllArticles(Pageable pageable, Long userId) { // Added userId parameter
        // Removed placeholder userId = 1L;

        Page<Article> articlePage = articleRepository.findAll(pageable);
        return mapArticlePageToDtoWithVote(articlePage, userId);
    }

    
    @Transactional(readOnly = true)
    @Override
    public Page<ArticleDTO> getAllArticlesSorted(Pageable pageable, Long userId) { // Added userId parameter
        // Removed placeholder userId = 1L;

        // Sort by score in descending order, then by createdAt in descending order
        Sort sort = Sort.by(Sort.Direction.DESC, "score", "createdAt");
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Fetch paginated and sorted articles
        Page<Article> articlePage = articleRepository.findAll(pageable);
        return mapArticlePageToDtoWithVote(articlePage, userId);
    }

    // Helper method to map Page<Article> to Page<ArticleDTO> including user vote
    private Page<ArticleDTO> mapArticlePageToDtoWithVote(Page<Article> articlePage, Long userId) {
        List<Long> articleIds = articlePage.getContent().stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        // Fetch votes for the current user and the articles on the current page
        Map<Long, Integer> userVotesMap = new HashMap<>();
        if (!articleIds.isEmpty() && userId != null) {
            List<ArticleVote> userVotes = articleVoteRepository.findByUserIdAndArticleIdIn(userId, articleIds);
            userVotesMap = userVotes.stream()
                    .collect(Collectors.toMap(vote -> vote.getArticle().getId(), ArticleVote::getValue));
        }

        // Map Article entities to ArticleDTOs, adding the currentUserVote
        Map<Long, Integer> finalUserVotesMap = userVotesMap; // Effectively final for lambda
        return articlePage.map(article -> {
            ArticleDTO dto = entityMapper.toDTO(article);
            Integer currentUserVote = finalUserVotesMap.getOrDefault(article.getId(), 0);
            return new ArticleDTO(
                    dto.id(),
                    dto.title(),
                    dto.summary(),
                    dto.content(),
                    dto.imageUrls(),
                    dto.createdAt(),
                    dto.authorId(),
                    dto.authorUsername(),
                    dto.score(),
                    dto.commentCount(),
                    dto.tags(),
                    currentUserVote
            );
        });
    }


    @Override
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO, Long userId) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        checkArticlePermissions(existingArticle, userId, "update");

        // Update basic fields from DTO
        entityMapper.updateArticleFromDto(articleDTO, existingArticle);

        // Handle tag updates separately if needed (e.g., clearing and adding)
        if (articleDTO.tags() != null) { // Check if tags were provided in update DTO
            Set<ArticleTag> tags = findOrCreateTags(articleDTO.tags(), existingArticle);
            existingArticle.setTags(tags); // Replace existing tags
        }

        // If content is updated, potentially re-trigger verification
        // existingArticle.setVerified(false); // Mark as unverified on update? Decision needed.
        // contentVerificationService.verifyArticleContent(existingArticle.getId());

        Article updatedArticle = articleRepository.save(existingArticle);
        return entityMapper.toDTO(updatedArticle);
    }


    @Override
    public void deleteArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        checkArticlePermissions(article, userId, "delete");
        articleRepository.delete(article); // Cascade should handle comments, ratings etc.
    }



    // --- Comments Implementation ---
    @Override
    public CommentDTO addComment(Long articleId, CommentDTO commentDTO, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = entityMapper.toEntity(commentDTO);
        comment.setArticle(article);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now()); // Or rely on @PrePersist

        Comment savedComment = commentRepository.save(comment);
        return entityMapper.toDTO(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByArticleId(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException("Article", "id", articleId);
        }
        // Assuming findByArticleId method exists in CommentRepository
        List<Comment> comments = commentRepository.findByArticleId(articleId);
        return comments.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        checkCommentPermissions(comment, userId, "delete");

        commentRepository.delete(comment);
    }

    // --- Voting Implementation ---
    @Override
    public ArticleDTO voteArticle(Long articleId, Long userId, ArticleVoteRequestDTO voteRequest) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<ArticleVote> existingVoteOpt = articleVoteRepository.findByUserIdAndArticleId(userId, articleId);

        int newVoteValue = voteRequest.voteType().getValue(); // +1 for UP, -1 for DOWN
        Integer finalUserVote = 0; // Default return status

        if (existingVoteOpt.isPresent()) {
            ArticleVote existingVote = existingVoteOpt.get();
            // User clicked the same button again (e.g., upvoted when already upvoted) -> remove vote
            if (existingVote.getValue() == newVoteValue) {
                articleVoteRepository.delete(existingVote);
                System.out.println("Vote Action: Deleting existing vote for user " + userId + ", article " + articleId); // Log action
                finalUserVote = 0; 
            }
            // User changed their vote (e.g., was upvote, now downvote) -> update vote
            else {
                existingVote.setValue(newVoteValue);
                articleVoteRepository.save(existingVote);
                System.out.println("Vote Action: Updating vote to " + newVoteValue + " for user " + userId + ", article " + articleId); // Log action
                finalUserVote = newVoteValue;
            }
        } else {
            // No existing vote -> create new vote
            ArticleVote newVote = new ArticleVote();
            newVote.setArticle(article);
            newVote.setUser(user);
            newVote.setValue(newVoteValue);
             articleVoteRepository.save(newVote);
             System.out.println("Vote Action: Creating new vote with value " + newVoteValue + " for user " + userId + ", article " + articleId); // Log action
             finalUserVote = newVoteValue;
         }
 
         // 3. Explicitly flush changes to the database before refreshing
         articleVoteRepository.flush(); // Ensure vote changes are sent to DB
         entityManager.refresh(article); // <<< Force reload article state from DB, recalculating @Formula
  
          // 4. Map the *refreshed* article to DTO
          // AND include the current user's vote status based on the action taken
          // (finalUserVote was determined above based on action)
  
          ArticleDTO dto = entityMapper.toDTO(article); // Map base article
          return new ArticleDTO( // Construct DTO with vote status
                 dto.id(), dto.title(), dto.summary(), dto.content(), dto.imageUrls(),
                 dto.createdAt(), dto.authorId(), dto.authorUsername(), dto.score(),
                 dto.commentCount(), dto.tags(), finalUserVote
         );
         // return entityMapper.toDTO(article); // Old version without currentUserVote in response
     }
 


    // --- Tags Implementation ---
    @Override
    public ArticleDTO addTagsToArticle(Long articleId, Set<String> tagNames) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        Set<ArticleTag> tags = findOrCreateTags(tagNames, article);
        article.getTags().addAll(tags); // Add the new tags

        Article savedArticle = articleRepository.save(article);
        return entityMapper.toDTO(savedArticle);
    }

    @Override
    public ArticleDTO removeTagsFromArticle(Long articleId, Set<String> tagNames) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        // TODO: Security check if needed

        if (tagNames == null || tagNames.isEmpty()) {
            return entityMapper.toDTO(article); // No tags to remove
        }

        // Remove only the tags specified
        article.getTags().removeIf(tag -> tagNames.contains(tag.getTagName().getName()));

        Article savedArticle = articleRepository.save(article);
        return entityMapper.toDTO(savedArticle);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ArticleDTO> getArticlesByTag(String tagName) {
        List<Article> articles = articleRepository.findByTags_NameIgnoreCase(tagName);

        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        return articles.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }



    // --- Recommendation --- Placeholder
    @Override
    @Transactional(readOnly = true)
    public List<ArticleDTO> getRecommendedArticles(Long userId) {
        System.out.println("Placeholder: Fetching recommendations for user " + userId);
        // Delegate to RecommendationService
        return recommendationService.getArticleRecommendationsForUser(userId, 10); // Example count
    }

    @Override
public List<CommentDTO> getCommentsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        List<Comment> comments = commentRepository.findByUserId(userId);
        return comments.stream()
                .map(entityMapper::toDTO)
                 .collect(Collectors.toList());
 
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<ArticleDTO> getArticlesByAuthorId(Long authorId, Long currentUserId) {
         List<Article> articles = articleRepository.findByAuthorId(authorId); // Assuming this method exists
         // Use a similar mapping approach as mapArticlePageToDtoWithVote but for a List
         return mapArticleListToDtoWithVote(articles, currentUserId);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<ArticleDTO> getVotedArticlesByUserId(Long userId, VoteType voteType, Long currentUserId) {
         int voteValue = voteType == VoteType.UP ? 1 : -1;
         List<Article> articles = articleRepository.findArticlesVotedByUser(userId, voteValue); // Assuming this method exists
         // Use a similar mapping approach as mapArticlePageToDtoWithVote but for a List
         return mapArticleListToDtoWithVote(articles, currentUserId);
     }
 

    private Set<ArticleTag> findOrCreateTags(Set<String> tagNames, Article article) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }

        Set<ArticleTag> result = new HashSet<>();

        for (String name : tagNames) {
            String cleanedName = name.trim().toLowerCase();
            ArticleTag tag = findOrCreateTag(cleanedName, article);
            result.add(tag);
        }

         return result;
     }
 
     // Helper method to map List<Article> to List<ArticleDTO> including user vote
     private List<ArticleDTO> mapArticleListToDtoWithVote(List<Article> articles, Long userId) {
         if (articles.isEmpty()) {
             return Collections.emptyList();
         }
         List<Long> articleIds = articles.stream()
                 .map(Article::getId)
                 .collect(Collectors.toList());
 
         // Fetch votes for the current user and the articles in the list
         Map<Long, Integer> userVotesMap = new HashMap<>();
         if (!articleIds.isEmpty() && userId != null) {
             List<ArticleVote> userVotes = articleVoteRepository.findByUserIdAndArticleIdIn(userId, articleIds);
             userVotesMap = userVotes.stream()
                     .collect(Collectors.toMap(vote -> vote.getArticle().getId(), ArticleVote::getValue));
         }
 
         // Map Article entities to ArticleDTOs, adding the currentUserVote
         Map<Long, Integer> finalUserVotesMap = userVotesMap; // Effectively final for lambda
         return articles.stream().map(article -> {
             ArticleDTO dto = entityMapper.toDTO(article);
             Integer currentUserVote = finalUserVotesMap.getOrDefault(article.getId(), 0);
             return new ArticleDTO(
                     dto.id(), dto.title(), dto.summary(), dto.content(), dto.imageUrls(),
                     dto.createdAt(), dto.authorId(), dto.authorUsername(), dto.score(),
                     dto.commentCount(), dto.tags(), currentUserVote
             );
         }).collect(Collectors.toList());
     }
 

    private ArticleTag findOrCreateTag(String tagName, Article article) {
        TagName tag = tagNameRepository.findByNameIgnoreCase(tagName)
                .orElseGet(() -> tagNameRepository.save(new TagName(tagName)));

        return articleTagRepository.findByArticleAndTagName_NameIgnoreCase(article, tagName)
                .orElseGet(() -> articleTagRepository.save(new ArticleTag(article, tag)));
    }




    private void checkArticlePermissions(Article article, Long userId, String action) {
        // TODO: Implement security check using Spring Security
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAuthor = article.getAuthor() != null && article.getAuthor().getId().equals(userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("User not authorized to " + action + " this article.");
        }
        System.out.println("Placeholder: Permissions check passed for user " + userId + " to " + action + " article " + article.getId());
    }

    private void checkCommentPermissions(Comment comment, Long userId, String action) {
        // TODO: Implement security check using Spring Security
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAuthor = comment.getUser() != null && comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("User not authorized to " + action + " this comment.");
        }
        System.out.println("Placeholder: Permissions check passed for user " + userId + " to " + action + " comment " + comment.getId());
    }
}
