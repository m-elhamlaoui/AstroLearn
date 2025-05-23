package com.example.demo.service.integration;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.Article;
import com.example.demo.model.ArticleTag;
import com.example.demo.model.Comment;
import com.example.demo.model.TagName;
import com.example.demo.model.User;
import com.example.demo.model.VoteType;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ArticleVoteRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.TagNameRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ArticleService;
import com.example.demo.service.RecommendationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ArticleServiceIntegrationTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleVoteRepository articleVoteRepository;

    @Autowired
    private TagNameRepository tagNameRepository;

    @Autowired
    private EntityManager entityManager; // To refresh entities

    // Mock or use a test implementation for RecommendationService if it's complex
    // @MockBean
    // private RecommendationService recommendationService;

    private User author;
    private User reader;
    private Article article;

    @BeforeEach
    void setUp() {
        // Clean up before each test - order matters due to foreign key constraints
        articleRepository.deleteAll();
        userRepository.deleteAll();
        tagNameRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test users with different data from seeder
        author = new User();
        author.setUsername("space_writer");
        author.setEmail("writer@space.com");
        author.setPassword("writepass123");
        author.setRole(User.UserRole.USER);
        author.setVerificationStatus(User.UserVerification.VERIFIED);
        author.setBio("Space science writer and researcher");
        author.setProfileImageUrl("https://i.pravatar.cc/150?u=writer");
        author.setPhotoCoverUrl("https://picsum.photos/seed/writercover/800/200");
        author.setExperiencePoints(2500);
        author = userRepository.save(author);
        entityManager.flush();

        reader = new User();
        reader.setUsername("space_reader");
        reader.setEmail("reader@space.com");
        reader.setPassword("readpass123");
        reader.setRole(User.UserRole.USER);
        reader.setVerificationStatus(User.UserVerification.VERIFIED);
        reader.setBio("Passionate about space exploration");
        reader.setProfileImageUrl("https://i.pravatar.cc/150?u=reader");
        reader.setPhotoCoverUrl("https://picsum.photos/seed/readercover/800/200");
        reader.setExperiencePoints(1800);
        reader = userRepository.save(reader);
        entityManager.flush();

        // Create and save TagName entities first
        TagName tag1 = new TagName("Interstellar Travel");
        TagName tag2 = new TagName("Space Technology");
        TagName tag3 = new TagName("Future Exploration");
        
        tag1 = tagNameRepository.save(tag1);
        tag2 = tagNameRepository.save(tag2);
        tag3 = tagNameRepository.save(tag3);
        entityManager.flush();

        // Create test article with different data from seeder
        article = new Article();
        article.setTitle("The Future of Interstellar Travel");
        article.setSummary("A comprehensive look at the challenges and possibilities of traveling between stars");
        article.setContent("Exploring the possibilities and challenges of traveling between stars, including propulsion systems, life support, and the psychological aspects of long-duration space travel.");
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.now());
        article.setScore(0);
        article.setCommentCount(0L);
        article.setImageUrls(List.of("https://picsum.photos/seed/interstellar/800/400"));
        article.setTags(new HashSet<>());
        
        // Create ArticleTag entities with saved article and tag names
        ArticleTag articleTag1 = new ArticleTag(article, tag1);
        ArticleTag articleTag2 = new ArticleTag(article, tag2);
        ArticleTag articleTag3 = new ArticleTag(article, tag3);
        
        // Add tags to article
        article.getTags().add(articleTag1);
        article.getTags().add(articleTag2);
        article.getTags().add(articleTag3);
        
        // Save the article
        article = articleRepository.save(article);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testCreateArticle() {
        ArticleDTO newArticleDTO = new ArticleDTO(
                null,
                "New Article Title",
                "New Article Summary",
                "New Article Content",
                List.of("https://picsum.photos/seed/newarticle/800/400"),
                null,
                null,
                null,
                0,
                0L,
                Set.of("space exploration", "technology"),
                0
        );

        ArticleDTO createdArticle = articleService.createArticle(newArticleDTO, author.getId());

        assertThat(createdArticle).isNotNull();
        assertThat(createdArticle.title()).isEqualTo("New Article Title");
        assertThat(createdArticle.summary()).isEqualTo("New Article Summary");
        assertThat(createdArticle.content()).isEqualTo("New Article Content");
        assertThat(createdArticle.authorId()).isEqualTo(author.getId());
        assertThat(createdArticle.tags()).containsExactlyInAnyOrder("space exploration", "technology");

        // Verify in database
        Article savedArticle = articleRepository.findById(createdArticle.id()).orElse(null);
        assertThat(savedArticle).isNotNull();
        assertThat(savedArticle.getTitle()).isEqualTo("New Article Title");
        assertThat(savedArticle.getSummary()).isEqualTo("New Article Summary");
        assertThat(savedArticle.getContent()).isEqualTo("New Article Content");
        assertThat(savedArticle.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(savedArticle.getTags()).hasSize(2);
        assertThat(savedArticle.getImageUrls()).containsExactly("https://picsum.photos/seed/newarticle/800/400");
    }

    @Test
    void testGetArticleById() {
        ArticleDTO foundArticle = articleService.getArticleById(article.getId(), reader.getId());

        assertThat(foundArticle).isNotNull();
        assertThat(foundArticle.id()).isEqualTo(article.getId());
        assertThat(foundArticle.title()).isEqualTo("The Future of Interstellar Travel");
        assertThat(foundArticle.summary()).isEqualTo("A comprehensive look at the challenges and possibilities of traveling between stars");
        assertThat(foundArticle.content()).isEqualTo("Exploring the possibilities and challenges of traveling between stars, including propulsion systems, life support, and the psychological aspects of long-duration space travel.");
        assertThat(foundArticle.authorId()).isEqualTo(author.getId());
        assertThat(foundArticle.authorUsername()).isEqualTo(author.getUsername());
        assertThat(foundArticle.imageUrls()).containsExactly("https://picsum.photos/seed/interstellar/800/400");
        assertThat(foundArticle.tags()).containsExactlyInAnyOrder("Interstellar Travel", "Space Technology", "Future Exploration");
        assertThat(foundArticle.currentUserVote()).isEqualTo(0); // No vote yet
        assertThat(foundArticle.score()).isEqualTo(0);
        assertThat(foundArticle.commentCount()).isEqualTo(0L);
    }

    @Test
    void testGetArticleById_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> articleService.getArticleById(999L, reader.getId()));
    }

    @Test
    void testGetAllArticles() {
        // Add another article
        Article anotherArticle = new Article();
        anotherArticle.setTitle("Another Article");
        anotherArticle.setSummary("Another Summary");
        anotherArticle.setContent("Another Content");
        anotherArticle.setAuthor(reader);
        anotherArticle.setCreatedAt(LocalDateTime.now());
        anotherArticle.setScore(0);
        anotherArticle.setCommentCount(0L);
        anotherArticle.setTags(new HashSet<>());
        articleRepository.save(anotherArticle);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleDTO> articlePage = articleService.getAllArticles(pageable, reader.getId());

        assertThat(articlePage).isNotNull();
        assertThat(articlePage.getTotalElements()).isEqualTo(2);
        assertThat(articlePage.getContent()).hasSize(2);
        assertThat(articlePage.getContent().stream().map(ArticleDTO::title)).containsExactlyInAnyOrder("The Future of Interstellar Travel", "Another Article");
    }

    @Test
    void testUpdateArticle() {
        ArticleDTO updatedArticleDTO = new ArticleDTO(
                article.getId(),
                "Updated Title",
                "Updated Summary",
                "Updated Content",
                List.of("https://picsum.photos/seed/updated/800/400"),
                article.getCreatedAt(),
                article.getAuthor().getId(),
                article.getAuthor().getUsername(),
                article.getScore(),
                article.getCommentCount(),
                Set.of("updated tag"),
                0
        );

        ArticleDTO result = articleService.updateArticle(article.getId(), updatedArticleDTO, author.getId());

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.summary()).isEqualTo("Updated Summary");
        assertThat(result.content()).isEqualTo("Updated Content");
        assertThat(result.imageUrls()).containsExactly("https://picsum.photos/seed/updated/800/400");
        assertThat(result.tags()).containsExactlyInAnyOrder("updated tag");

        // Verify in database
        Article updatedArticle = articleRepository.findById(article.getId()).orElse(null);
        assertThat(updatedArticle).isNotNull();
        assertThat(updatedArticle.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedArticle.getSummary()).isEqualTo("Updated Summary");
        assertThat(updatedArticle.getContent()).isEqualTo("Updated Content");
        assertThat(updatedArticle.getImageUrls()).containsExactly("https://picsum.photos/seed/updated/800/400");
        assertThat(updatedArticle.getTags()).hasSize(1);
        assertThat(updatedArticle.getTags().stream().map(t -> t.getTagName().getName())).containsExactlyInAnyOrder("updated tag");
    }

    @Test
    void testUpdateArticle_Unauthorized() {
        ArticleDTO updatedArticleDTO = new ArticleDTO(
                article.getId(),
                "Updated Title",
                "Updated Summary",
                "Updated Content",
                List.of("updated_url"),
                article.getCreatedAt(),
                article.getAuthor().getId(),
                article.getAuthor().getUsername(),
                article.getScore(),
                article.getCommentCount(),
                Set.of("updated_tag"),
                0
        );

        assertThrows(UnauthorizedException.class, () -> articleService.updateArticle(article.getId(), updatedArticleDTO, reader.getId()));
    }

    @Test
    void testDeleteArticle() {
        Long articleId = article.getId();
        articleService.deleteArticle(articleId, author.getId());

        // Verify in database
        assertThat(articleRepository.findById(articleId)).isEmpty();
    }

    @Test
    void testDeleteArticle_Unauthorized() {
        assertThrows(UnauthorizedException.class, () -> articleService.deleteArticle(article.getId(), reader.getId()));
    }

    @Test
    void testAddComment() {
        CommentDTO newCommentDTO = new CommentDTO(
                null,
                "This is a test comment",
                null,
                null,
                null,
                null
        );

        CommentDTO createdComment = articleService.addComment(article.getId(), newCommentDTO, reader.getId());

        assertThat(createdComment).isNotNull();
        assertThat(createdComment.content()).isEqualTo("This is a test comment");
        assertThat(createdComment.articleId()).isEqualTo(article.getId());
        assertThat(createdComment.userId()).isEqualTo(reader.getId());

        // Verify in database
        List<Comment> comments = commentRepository.findByArticleId(article.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getContent()).isEqualTo("This is a test comment");
        assertThat(comments.get(0).getUser().getId()).isEqualTo(reader.getId());
    }

    @Test
    void testGetCommentsByArticleId() {
        // Add a comment
        Comment comment = new Comment();
        comment.setContent("Existing comment");
        comment.setArticle(article);
        comment.setUser(reader);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        List<CommentDTO> comments = articleService.getCommentsByArticleId(article.getId());

        assertThat(comments).isNotNull();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).content()).isEqualTo("Existing comment");
    }

    @Test
    void testDeleteComment() {
        // Add a comment to delete
        Comment commentToDelete = new Comment();
        commentToDelete.setContent("Comment to delete");
        commentToDelete.setArticle(article);
        commentToDelete.setUser(reader);
        commentToDelete.setCreatedAt(LocalDateTime.now());
        commentToDelete = commentRepository.save(commentToDelete);

        Long commentId = commentToDelete.getId();
        articleService.deleteComment(commentId, reader.getId());

        // Verify in database
        assertThat(commentRepository.findById(commentId)).isEmpty();
    }

    @Test
    void testDeleteComment_Unauthorized() {
        // Add a comment
        Comment comment = new Comment();
        comment.setContent("Existing comment");
        comment.setArticle(article);
        comment.setUser(reader);
        comment.setCreatedAt(LocalDateTime.now());
        final Comment savedComment = commentRepository.save(comment);

        // Try to delete with a different user ID
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        anotherUser.setRole(User.UserRole.USER);
        anotherUser.setVerificationStatus(User.UserVerification.UNVERIFIED);
        final User savedAnotherUser = userRepository.save(anotherUser);

        assertThrows(UnauthorizedException.class, () -> articleService.deleteComment(savedComment.getId(), savedAnotherUser.getId()));
    }

    @Test
    void testVoteArticle_Upvote() {
        ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.UP);
        ArticleDTO result = articleService.voteArticle(article.getId(), reader.getId(), voteRequest);

        assertThat(result).isNotNull();
        assertThat(result.currentUserVote()).isEqualTo(1);
        
        // Get fresh article from database instead of refreshing
        Article updatedArticle = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(updatedArticle.getScore()).isEqualTo(1);

        // Verify in database
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId())).isPresent();
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId()).get().getValue()).isEqualTo(1);
    }

    @Test
    void testVoteArticle_Downvote() {
        ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.DOWN);
        ArticleDTO result = articleService.voteArticle(article.getId(), reader.getId(), voteRequest);

        assertThat(result).isNotNull();
        assertThat(result.currentUserVote()).isEqualTo(-1);
        
        // Get fresh article from database instead of refreshing
        Article updatedArticle = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(updatedArticle.getScore()).isEqualTo(-1);

        // Verify in database
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId())).isPresent();
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId()).get().getValue()).isEqualTo(-1);
    }

    @Test
    void testVoteArticle_ChangeVote() {
        // First upvote
        ArticleVoteRequestDTO upvoteRequest = new ArticleVoteRequestDTO(VoteType.UP);
        articleService.voteArticle(article.getId(), reader.getId(), upvoteRequest);
        
        // Get fresh article from database
        Article articleAfterUpvote = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(articleAfterUpvote.getScore()).isEqualTo(1);

        // Then change to downvote
        ArticleVoteRequestDTO downvoteRequest = new ArticleVoteRequestDTO(VoteType.DOWN);
        ArticleDTO result = articleService.voteArticle(article.getId(), reader.getId(), downvoteRequest);

        assertThat(result).isNotNull();
        assertThat(result.currentUserVote()).isEqualTo(-1);
        
        // Get fresh article from database
        Article articleAfterDownvote = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(articleAfterDownvote.getScore()).isEqualTo(-1);

        // Verify in database
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId())).isPresent();
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId()).get().getValue()).isEqualTo(-1);
    }

    @Test
    void testVoteArticle_RemoveVote() {
        // First upvote
        ArticleVoteRequestDTO upvoteRequest = new ArticleVoteRequestDTO(VoteType.UP);
        articleService.voteArticle(article.getId(), reader.getId(), upvoteRequest);
        
        // Get fresh article from database
        Article articleAfterUpvote = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(articleAfterUpvote.getScore()).isEqualTo(1);

        // Then click upvote again to remove
        ArticleVoteRequestDTO removeVoteRequest = new ArticleVoteRequestDTO(VoteType.UP);
        ArticleDTO result = articleService.voteArticle(article.getId(), reader.getId(), removeVoteRequest);

        assertThat(result).isNotNull();
        assertThat(result.currentUserVote()).isEqualTo(0);
        
        // Get fresh article from database and verify vote is removed
        Article articleAfterRemove = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(articleAfterRemove.getScore()).isEqualTo(0);

        // Verify vote is removed from database
        assertThat(articleVoteRepository.findByUserIdAndArticleId(reader.getId(), article.getId())).isEmpty();

        // Double check by trying to find any votes for this article
        assertThat(articleVoteRepository.findByArticleId(article.getId())).isEmpty();
    }


    @Test
    void testGetArticlesByTag() {
        // Create articles with tags
        Article article1 = new Article();
        article1.setTitle("Article 1");
        article1.setSummary("Summary 1");
        article1.setContent("Content 1");
        article1.setAuthor(author);
        article1.setCreatedAt(LocalDateTime.now());
        article1.setScore(0);
        article1.setCommentCount(0L);
        article1 = articleRepository.save(article1);
        articleService.addTagsToArticle(article1.getId(), Set.of("space", "nasa"));

        Article article2 = new Article();
        article2.setTitle("Article 2");
        article2.setSummary("Summary 2");
        article2.setContent("Content 2");
        article2.setAuthor(reader);
        article2.setCreatedAt(LocalDateTime.now());
        article2.setScore(0);
        article2.setCommentCount(0L);
        article2 = articleRepository.save(article2);
        articleService.addTagsToArticle(article2.getId(), Set.of("astronomy", "space"));

        Article article3 = new Article();
        article3.setTitle("Article 3");
        article3.setSummary("Summary 3");
        article3.setContent("Content 3");
        article3.setAuthor(author);
        article3.setCreatedAt(LocalDateTime.now());
        article3.setScore(0);
        article3.setCommentCount(0L);
        article3 = articleRepository.save(article3);
        articleService.addTagsToArticle(article3.getId(), Set.of("science"));

        entityManager.flush();
        entityManager.clear();

        List<ArticleDTO> spaceArticles = articleService.getArticlesByTag("space");
        assertThat(spaceArticles).hasSize(2);
        assertThat(spaceArticles.stream().map(ArticleDTO::title)).containsExactlyInAnyOrder("Article 1", "Article 2");

        List<ArticleDTO> nasaArticles = articleService.getArticlesByTag("nasa");
        assertThat(nasaArticles).hasSize(1);
        assertThat(nasaArticles.get(0).title()).isEqualTo("Article 1");

        List<ArticleDTO> nonExistentTagArticles = articleService.getArticlesByTag("nonexistent");
        assertThat(nonExistentTagArticles).isEmpty();
    }

    // Note: getRecommendedArticles and getPopularArticles require RecommendationService logic
    // and potentially more complex data setup (reading history, votes) to test fully.
    // For a basic integration test, you might mock RecommendationService or test the fallback logic.

    @Test
    void testGetCommentsByUserId() {
        // Add comments by different users
        Comment comment1 = new Comment();
        comment1.setContent("Comment by reader");
        comment1.setArticle(article);
        comment1.setUser(reader);
        comment1.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setContent("Comment by author");
        comment2.setArticle(article);
        comment2.setUser(author);
        comment2.setCreatedAt(LocalDateTime.now().plusMinutes(1));
        commentRepository.save(comment2);

        entityManager.flush();
        entityManager.clear();

        List<CommentDTO> readerComments = articleService.getCommentsByUserId(reader.getId());
        assertThat(readerComments).hasSize(1);
        assertThat(readerComments.get(0).content()).isEqualTo("Comment by reader");
        assertThat(readerComments.get(0).userId()).isEqualTo(reader.getId());

        List<CommentDTO> authorComments = articleService.getCommentsByUserId(author.getId());
        assertThat(authorComments).hasSize(1);
        assertThat(authorComments.get(0).content()).isEqualTo("Comment by author");
        assertThat(authorComments.get(0).userId()).isEqualTo(author.getId());
    }

    @Test
    void testGetArticlesByAuthorId() {
        // Create articles by different authors
        Article articleByReader = new Article();
        articleByReader.setTitle("Reader's Article");
        articleByReader.setSummary("Summary");
        articleByReader.setContent("Content");
        articleByReader.setAuthor(reader);
        articleByReader.setCreatedAt(LocalDateTime.now());
        articleByReader.setScore(0);
        articleByReader.setCommentCount(0L);
        articleRepository.save(articleByReader);

        entityManager.flush();
        entityManager.clear();

        List<ArticleDTO> authorArticles = articleService.getArticlesByAuthorId(author.getId(), reader.getId());
        assertThat(authorArticles).hasSize(1);
        assertThat(authorArticles.get(0).title()).isEqualTo("The Future of Interstellar Travel");
        assertThat(authorArticles.get(0).authorId()).isEqualTo(author.getId());

        List<ArticleDTO> readerArticles = articleService.getArticlesByAuthorId(reader.getId(), author.getId());
        assertThat(readerArticles).hasSize(1);
        assertThat(readerArticles.get(0).title()).isEqualTo("Reader's Article");
        assertThat(readerArticles.get(0).authorId()).isEqualTo(reader.getId());
    }

    @Test
    void testGetVotedArticlesByUserId() {
        // Reader upvotes the article
        ArticleVoteRequestDTO upvoteRequest = new ArticleVoteRequestDTO(VoteType.UP);
        articleService.voteArticle(article.getId(), reader.getId(), upvoteRequest);

        // Author downvotes the article
        ArticleVoteRequestDTO downvoteRequest = new ArticleVoteRequestDTO(VoteType.DOWN);
        articleService.voteArticle(article.getId(), author.getId(), downvoteRequest);

        entityManager.flush();
        entityManager.clear();

        // Get articles upvoted by reader
        List<ArticleDTO> readerUpvotedArticles = articleService.getVotedArticlesByUserId(reader.getId(), VoteType.UP, reader.getId());
        assertThat(readerUpvotedArticles).hasSize(1);
        assertThat(readerUpvotedArticles.get(0).id()).isEqualTo(article.getId());
        assertThat(readerUpvotedArticles.get(0).currentUserVote()).isEqualTo(1); // Reader's vote status

        // Get articles downvoted by author
        List<ArticleDTO> authorDownvotedArticles = articleService.getVotedArticlesByUserId(author.getId(), VoteType.DOWN, author.getId());
        assertThat(authorDownvotedArticles).hasSize(1);
        assertThat(authorDownvotedArticles.get(0).id()).isEqualTo(article.getId());
        assertThat(authorDownvotedArticles.get(0).currentUserVote()).isEqualTo(-1); // Author's vote status

        // Get articles upvoted by author (should be empty)
        List<ArticleDTO> authorUpvotedArticles = articleService.getVotedArticlesByUserId(author.getId(), VoteType.UP, author.getId());
        assertThat(authorUpvotedArticles).isEmpty();
    }
}
