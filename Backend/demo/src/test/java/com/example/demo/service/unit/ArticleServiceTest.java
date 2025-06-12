package com.example.demo.service.unit;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import com.example.demo.model.Article;
import com.example.demo.model.ArticleVote;
import com.example.demo.model.Comment;
import com.example.demo.model.User;
import com.example.demo.model.VoteType;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ArticleVoteRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.ArticleServiceImpl;
import com.example.demo.util.TestLogger;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(TestLogger.class)

class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleVoteRepository articleVoteRepository;

    @Mock
    private EntityManager entityManager;

    // Mock other dependencies as needed (e.g., RecommendationService)
    // @Mock
    // private RecommendationService recommendationService;


    @InjectMocks
    private ArticleServiceImpl articleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateArticle_Success() {
        // Arrange
        ArticleDTO articleDTO = new ArticleDTO(null, "Test Title", "Test Summary", "Test Content", Collections.emptyList(), null, null, null, 0, 0L, Collections.emptySet(), 0);
        User author = new User();
        author.setId(1L); // Assuming setId is available via Lombok @Setter

        Article article = new Article(); // Entity before saving
        // Set properties on the entity that would be mapped from DTO
        article.setTitle(articleDTO.title());
        article.setSummary(articleDTO.summary());
        article.setContent(articleDTO.content());
        article.setImageUrls(articleDTO.imageUrls());
        article.setTags(new HashSet<>()); // Initialize collection

        // Simulate saved article with generated ID and other fields set by service
        Article savedArticle = new Article();
        savedArticle.setId(1L); // Simulate generated ID
        savedArticle.setAuthor(author);
        savedArticle.setCreatedAt(LocalDateTime.now());
        savedArticle.setScore(0);
        savedArticle.setCommentCount(0L);
        savedArticle.setTitle(articleDTO.title());
        savedArticle.setSummary(articleDTO.summary());
        savedArticle.setContent(articleDTO.content());
        savedArticle.setImageUrls(articleDTO.imageUrls());
        savedArticle.setTags(new HashSet<>()); // Initialize collection

        ArticleDTO expectedDTO = new ArticleDTO(1L, "Test Title", "Test Summary", "Test Content", Collections.emptyList(), savedArticle.getCreatedAt(), 1L, "testuser", 0, 0L, Collections.emptySet(), 0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(entityMapper.toEntity(articleDTO)).thenReturn(article);
        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(entityMapper.toDTO(savedArticle)).thenReturn(expectedDTO);

        // Act
        ArticleDTO result = articleService.createArticle(articleDTO, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        verify(userRepository, times(1)).findById(1L);
        verify(entityMapper, times(1)).toEntity(articleDTO);
        verify(articleRepository, times(1)).save(article); // Verify saving the initial entity
        verify(entityMapper, times(1)).toDTO(savedArticle);
    }

    @Test
    void testCreateArticle_UserNotFound() {
        // Arrange
        ArticleDTO articleDTO = new ArticleDTO(null, "Test Title", "Test Summary", "Test Content", Collections.emptyList(), null, null, null, 0, 0L, Collections.emptySet(), 0);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.createArticle(articleDTO, 1L));
        verify(userRepository, times(1)).findById(1L);
        verifyNoInteractions(entityMapper, articleRepository);
    }

    @Test
    void testGetArticleById_Success() {
        // Arrange
        Long articleId = 1L;
        Long userId = 2L;
        Article article = new Article();
        article.setId(articleId); // Assuming setId is available
        article.setTitle("Test Title");
        article.setSummary("Test Summary");
        article.setContent("Test Content");
        article.setImageUrls(Collections.emptyList());
        article.setCreatedAt(LocalDateTime.now());
        User author = new User();
        author.setId(1L);
        author.setUsername("testuser");
        article.setAuthor(author);
        article.setScore(10); // Example score
        article.setCommentCount(5L); // Example comment count
        article.setTags(new HashSet<>());

        ArticleDTO expectedDTO = new ArticleDTO(articleId, "Test Title", "Test Summary", "Test Content", Collections.emptyList(), article.getCreatedAt(), 1L, "testuser", 10, 5L, Collections.emptySet(), 0); // Assuming currentUserVote is 0

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        // Mocking articleVoteRepository.findByUserIdAndArticleId would be needed for a real scenario
        when(entityMapper.toDTO(article)).thenReturn(expectedDTO); // Mock mapping the fetched article to DTO

        // Act
        ArticleDTO result = articleService.getArticleById(articleId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.score(), result.score());
        assertEquals(expectedDTO.commentCount(), result.commentCount());
        verify(articleRepository, times(1)).findById(articleId);
        verify(entityMapper, times(1)).toDTO(article);
    }

    @Test
    void testGetArticleById_NotFound() {
        // Arrange
        Long articleId = 1L;
        Long userId = 2L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.getArticleById(articleId, userId));
        verify(articleRepository, times(1)).findById(articleId);
        verifyNoInteractions(entityMapper);
    }

    @Test
    void testGetAllArticles_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Test Article 1");
        article.setSummary("Summary 1");
        article.setContent("Content 1");
        article.setImageUrls(Collections.emptyList());
        article.setCreatedAt(LocalDateTime.now());
        User author = new User();
        author.setId(1L);
        author.setUsername("testuser");
        article.setAuthor(author);
        article.setScore(5);
        article.setCommentCount(2L);
        article.setTags(new HashSet<>());

        List<Article> articles = Collections.singletonList(article);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        ArticleDTO articleDTO = new ArticleDTO(1L, "Test Article 1", "Summary 1", "Content 1", Collections.emptyList(), article.getCreatedAt(), 1L, "testuser", 5, 2L, Collections.emptySet(), 0); // Assuming currentUserVote is 0

        when(articleRepository.findAll(pageable)).thenReturn(articlePage);
        // Mocking articleVoteRepository.findByUserIdAndArticleIdIn would be needed for a real scenario
        when(entityMapper.toDTO(article)).thenReturn(articleDTO); // Mock mapping each article

        // Act
        Page<ArticleDTO> resultPage = articleService.getAllArticles(pageable, 2L); // Assuming userId 2L

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(articleDTO.id(), resultPage.getContent().get(0).id());
        assertEquals(articleDTO.title(), resultPage.getContent().get(0).title());
        verify(articleRepository, times(1)).findAll(pageable);
        verify(entityMapper, times(1)).toDTO(article); // Verify mapper is called for each article
    }

    @Test
    void testUpdateArticle_Success() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L; // Assuming the user is the author or admin
        ArticleDTO updatedArticleDTO = new ArticleDTO(articleId, "Updated Title", "Updated Summary", "Updated Content", Collections.emptyList(), null, null, null, 0, 0L, Collections.emptySet(), 0);

        Article existingArticle = new Article();
        existingArticle.setId(articleId);
        User author = new User();
        author.setId(userId);
        existingArticle.setAuthor(author); // Set author to match userId for permission check

        Article updatedArticle = new Article(); // Simulate the article after update and save
        updatedArticle.setId(articleId);
        updatedArticle.setTitle(updatedArticleDTO.title());
        updatedArticle.setSummary(updatedArticleDTO.summary());
        updatedArticle.setContent(updatedArticleDTO.content());
        updatedArticle.setImageUrls(updatedArticleDTO.imageUrls());
        updatedArticle.setAuthor(author);
        updatedArticle.setCreatedAt(LocalDateTime.now());
        updatedArticle.setScore(existingArticle.getScore());
        updatedArticle.setCommentCount(existingArticle.getCommentCount());
        updatedArticle.setTags(new HashSet<>());

        ArticleDTO expectedDTO = new ArticleDTO(articleId, "Updated Title", "Updated Summary", "Updated Content", Collections.emptyList(), updatedArticle.getCreatedAt(), userId, "testuser", updatedArticle.getScore(), updatedArticle.getCommentCount(), Collections.emptySet(), 0);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(userRepository.findById(userId)).thenReturn(Optional.of(author)); // Mock userRepository to return the author
        doNothing().when(entityMapper).updateArticleFromDto(updatedArticleDTO, existingArticle); // Mock the update call
        when(articleRepository.save(existingArticle)).thenReturn(updatedArticle); // Mock saving the updated article
        when(entityMapper.toDTO(updatedArticle)).thenReturn(expectedDTO); // Mock mapping the saved article to DTO

        // Act
        ArticleDTO result = articleService.updateArticle(articleId, updatedArticleDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.title(), result.title());
        assertEquals(expectedDTO.summary(), result.summary());
        verify(articleRepository, times(1)).findById(articleId);
        verify(userRepository, times(1)).findById(userId); // Verify userRepository is called
        verify(entityMapper, times(1)).updateArticleFromDto(updatedArticleDTO, existingArticle);
        verify(articleRepository, times(1)).save(existingArticle);
        verify(entityMapper, times(1)).toDTO(updatedArticle);
    }

    @Test
    void testUpdateArticle_NotFound() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        ArticleDTO updatedArticleDTO = new ArticleDTO(articleId, "Updated Title", "Updated Summary", "Updated Content", Collections.emptyList(), null, null, null, 0, 0L, Collections.emptySet(), 0);

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.updateArticle(articleId, updatedArticleDTO, userId));
        verify(articleRepository, times(1)).findById(articleId);
        verifyNoMoreInteractions(articleRepository, userRepository, entityMapper); // Verify no further interactions
    }

    // Note: Testing UnauthorizedException for updateArticle would require mocking the private checkArticlePermissions method,
    // which is typically done by using a Spy on the service or refactoring the permission check into a separate, mockable component.
    // For this unit test, we focus on the successful path and the not-found scenario.


  @Test
    void testDeleteArticle_Success() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L; // Assuming the user is authorized to delete

        Article articleToDelete = new Article();
        articleToDelete.setId(articleId);
        User author = new User();
        author.setId(userId);
        articleToDelete.setAuthor(author); // Set author to match userId for permission check

        when(userRepository.findById(userId)).thenReturn(Optional.of(author)); // Mock userRepository to return the user
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(articleToDelete));
        doNothing().when(articleRepository).delete(articleToDelete); // Mock the delete call

        // Act
        articleService.deleteArticle(articleId, userId);

        // Assert
        verify(userRepository, times(1)).findById(userId); // Verify userRepository is called
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).delete(articleToDelete);
        verifyNoMoreInteractions(articleRepository, userRepository, entityMapper); // Verify no other interactions
    }

    @Test
    void testDeleteArticle_NotFound() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.deleteArticle(articleId, userId));
        verify(articleRepository, times(1)).findById(articleId);
        verifyNoMoreInteractions(articleRepository, userRepository, entityMapper); // Verify no other interactions
    }

    // Note: Testing UnauthorizedException for deleteArticle would require mocking the private checkArticlePermissions method.

    @Test
    void testAddComment_Success() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        CommentDTO commentDTO = new CommentDTO(null, "Test Comment Content", null, null, null, null);

        Article article = new Article();
        article.setId(articleId);

        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        Comment comment = new Comment(); // Entity before saving
        comment.setContent(commentDTO.content());

        Comment savedComment = new Comment(); // Simulate saved comment
        savedComment.setId(1L);
        savedComment.setContent(commentDTO.content());
        savedComment.setArticle(article);
        savedComment.setUser(user);
        savedComment.setCreatedAt(LocalDateTime.now());

        CommentDTO expectedDTO = new CommentDTO(1L, "Test Comment Content", articleId, savedComment.getCreatedAt(), userId, "testuser");

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(entityMapper.toEntity(commentDTO)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(entityMapper.toDTO(savedComment)).thenReturn(expectedDTO);

        // Act
        CommentDTO result = articleService.addComment(articleId, commentDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.content(), result.content());
        assertEquals(expectedDTO.articleId(), result.articleId());
        assertEquals(expectedDTO.userId(), result.userId());
        verify(articleRepository, times(1)).findById(articleId);
        verify(userRepository, times(1)).findById(userId);
        verify(entityMapper, times(1)).toEntity(commentDTO);
        verify(commentRepository, times(1)).save(comment);
        verify(entityMapper, times(1)).toDTO(savedComment);
    }

    @Test
    void testAddComment_ArticleNotFound() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        CommentDTO commentDTO = new CommentDTO(null, "Test Comment Content", null, null, null, null);

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.addComment(articleId, commentDTO, userId));
        verify(articleRepository, times(1)).findById(articleId);
        verifyNoMoreInteractions(articleRepository, userRepository, entityMapper, commentRepository);
    }

    @Test
    void testAddComment_UserNotFound() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        CommentDTO commentDTO = new CommentDTO(null, "Test Comment Content", null, null, null, null);

        Article article = new Article();
        article.setId(articleId);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.addComment(articleId, commentDTO, userId));
        verify(articleRepository, times(1)).findById(articleId);
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(articleRepository, userRepository, entityMapper, commentRepository);
    }

    @Test
    void testGetCommentsByArticleId_Success() {
        // Arrange
        Long articleId = 1L;
        Article article = new Article();
        article.setId(articleId);

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setContent("Comment 1");
        comment1.setArticle(article);
        comment1.setUser(new User()); // Mock user
        comment1.setCreatedAt(LocalDateTime.now());

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setContent("Comment 2");
        comment2.setArticle(article);
        comment2.setUser(new User()); // Mock user
        comment2.setCreatedAt(LocalDateTime.now());

        List<Comment> comments = List.of(comment1, comment2);

        CommentDTO commentDTO1 = new CommentDTO(1L, "Comment 1", articleId, comment1.getCreatedAt(), 10L, "user1"); // Mock DTO
        CommentDTO commentDTO2 = new CommentDTO(2L, "Comment 2", articleId, comment2.getCreatedAt(), 11L, "user2"); // Mock DTO

        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(commentRepository.findByArticleId(articleId)).thenReturn(comments);
        when(entityMapper.toDTO(comment1)).thenReturn(commentDTO1);
        when(entityMapper.toDTO(comment2)).thenReturn(commentDTO2);

        // Act
        List<CommentDTO> result = articleService.getCommentsByArticleId(articleId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(commentDTO1.id(), result.get(0).id());
        assertEquals(commentDTO2.id(), result.get(1).id());
        verify(articleRepository, times(1)).existsById(articleId);
        verify(commentRepository, times(1)).findByArticleId(articleId);
        verify(entityMapper, times(1)).toDTO(comment1);
        verify(entityMapper, times(1)).toDTO(comment2);
    }

    @Test
    void testGetCommentsByArticleId_ArticleNotFound() {
        // Arrange
        Long articleId = 1L;
        when(articleRepository.existsById(articleId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.getCommentsByArticleId(articleId));
        verify(articleRepository, times(1)).existsById(articleId);
        verifyNoMoreInteractions(articleRepository, commentRepository, entityMapper);
    }

   @Test
 void testDeleteComment_Success() {
     // Arrange
     Long commentId = 1L;
     Long userId = 1L; // Assuming user is authorized

     Comment commentToDelete = new Comment();
     commentToDelete.setId(commentId);
     User commentAuthor = new User();
     commentAuthor.setId(userId);
     commentToDelete.setUser(commentAuthor); // Set user to match userId for permission check

     when(userRepository.findById(userId)).thenReturn(Optional.of(commentAuthor)); // Mock userRepository to return the user
     when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentToDelete));
     doNothing().when(commentRepository).delete(commentToDelete); // Mock the delete call

     // Act
     articleService.deleteComment(commentId, userId);

     // Assert
     verify(userRepository, times(1)).findById(userId); // Verify userRepository is called
     verify(commentRepository, times(1)).findById(commentId);
     verify(commentRepository, times(1)).delete(commentToDelete);
     verifyNoMoreInteractions(commentRepository, userRepository, entityMapper);
 }   @Test
    void testDeleteComment_NotFound() {
        // Arrange
        Long commentId = 1L;
        Long userId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> articleService.deleteComment(commentId, userId));
        verify(commentRepository, times(1)).findById(commentId);
        verifyNoMoreInteractions(commentRepository, userRepository, entityMapper);
    }
@Test
void testVoteArticle_UpvoteNew() {
    // Arrange
    Long articleId = 1L;
    Long userId = 1L;
    ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.UP);

    Article article = new Article();
    article.setId(articleId);
    article.setScore(0); // Initial score
    article.setTitle("Test Article");
    article.setSummary("Test Summary");
    article.setContent("Test Content");
    article.setImageUrls(Collections.emptyList());
    article.setCreatedAt(LocalDateTime.now());
    User author = new User();
    author.setId(10L);
    author.setUsername("authoruser");
    article.setAuthor(author);
    article.setCommentCount(0L);
    article.setTags(new HashSet<>());

    User user = new User();
    user.setId(userId);

    ArticleVote newVote = new ArticleVote();
    newVote.setArticle(article);
    newVote.setUser(user);
    newVote.setValue(1); // Upvote value

    // Simulate article after vote and refresh - update score based on action
    Article articleAfterVote = new Article();
    articleAfterVote.setId(articleId);
    articleAfterVote.setScore(article.getScore() + 1); // Score increases by 1
    articleAfterVote.setTitle(article.getTitle());
    articleAfterVote.setSummary(article.getSummary());
    articleAfterVote.setContent(article.getContent());
    articleAfterVote.setImageUrls(article.getImageUrls());
    articleAfterVote.setCreatedAt(article.getCreatedAt());
    articleAfterVote.setAuthor(article.getAuthor());
    articleAfterVote.setCommentCount(article.getCommentCount());
    articleAfterVote.setTags(article.getTags());

    // Convert tags to Set<String> for ArticleDTO
    Set<String> tagNames = articleAfterVote.getTags().stream()
        .map(tag -> tag.getTagName().getName())
        .collect(Collectors.toSet());

    ArticleDTO expectedDTO = new ArticleDTO(
        articleId,
        articleAfterVote.getTitle(),
        articleAfterVote.getSummary(),
        articleAfterVote.getContent(),
        articleAfterVote.getImageUrls(),
        articleAfterVote.getCreatedAt(),
        articleAfterVote.getAuthor().getId(),
        articleAfterVote.getAuthor().getUsername(),
        articleAfterVote.getScore(),
        articleAfterVote.getCommentCount(),
        tagNames, // Pass the converted Set<String>
        1 // currentUserVote is 1
    );

    when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(articleVoteRepository.findByUserIdAndArticleId(userId, articleId)).thenReturn(Optional.empty()); // No existing vote
    when(articleVoteRepository.save(any(ArticleVote.class))).thenReturn(newVote); // Save new vote
    doNothing().when(articleVoteRepository).flush(); // Mock flush
    doNothing().when(entityManager).refresh(article); // Mock refresh
    when(entityMapper.toDTO(article)).thenReturn(expectedDTO); // Mock mapping the refreshed article

    // Act
    ArticleDTO result = articleService.voteArticle(articleId, userId, voteRequest);

    // Assert
    assertNotNull(result);
    assertEquals(expectedDTO.score(), result.score());
    assertEquals(expectedDTO.currentUserVote(), result.currentUserVote());
    verify(articleRepository, times(1)).findById(articleId);
    verify(userRepository, times(1)).findById(userId);
    verify(articleVoteRepository, times(1)).findByUserIdAndArticleId(userId, articleId);
    verify(articleVoteRepository, times(1)).save(any(ArticleVote.class));
    verify(articleVoteRepository, times(1)).flush();
    verify(entityManager, times(1)).refresh(article);
    verify(entityMapper, times(1)).toDTO(article);
}

    @Test
    void testVoteArticle_DownvoteNew() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.DOWN);

        Article article = new Article();
        article.setId(articleId);
        article.setScore(0); // Initial score
        article.setTitle("Test Article"); // Add other necessary fields for mapping
        article.setSummary("Test Summary");
        article.setContent("Test Content");
        article.setImageUrls(Collections.emptyList());
        article.setCreatedAt(LocalDateTime.now());
        User author = new User();
        author.setId(10L);
        author.setUsername("authoruser");
        article.setAuthor(author);
        article.setCommentCount(0L);
        article.setTags(new HashSet<>());

        User user = new User();
        user.setId(userId);

        ArticleVote newVote = new ArticleVote();
        newVote.setArticle(article);
        newVote.setUser(user);
        newVote.setValue(-1); // Downvote value

        // Simulate article after vote and refresh - update score based on action
        Article articleAfterVote = new Article();
        articleAfterVote.setId(articleId);
        articleAfterVote.setScore(article.getScore() - 1); // Score decreases by 1
        // Copy other properties from the original article
        articleAfterVote.setTitle(article.getTitle());
        articleAfterVote.setSummary(article.getSummary());
        articleAfterVote.setContent(article.getContent());
        articleAfterVote.setImageUrls(article.getImageUrls());
        articleAfterVote.setCreatedAt(article.getCreatedAt());
        articleAfterVote.setAuthor(article.getAuthor());
        articleAfterVote.setCommentCount(article.getCommentCount());
        articleAfterVote.setTags(article.getTags());

        Set<String> tagNames = articleAfterVote.getTags().stream()
                .map(tag -> tag.getTagName().getName()) // Assuming ArticleTag has a TagName field with a getName() method
                .collect(Collectors.toSet());

        ArticleDTO expectedDTO = new ArticleDTO(articleId, articleAfterVote.getTitle(), articleAfterVote.getSummary(), articleAfterVote.getContent(), articleAfterVote.getImageUrls(), articleAfterVote.getCreatedAt(), articleAfterVote.getAuthor().getId(), articleAfterVote.getAuthor().getUsername(), articleAfterVote.getScore(), articleAfterVote.getCommentCount(), tagNames, -1); // currentUserVote is -1

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleVoteRepository.findByUserIdAndArticleId(userId, articleId)).thenReturn(Optional.empty()); // No existing vote
        when(articleVoteRepository.save(any(ArticleVote.class))).thenReturn(newVote); // Save new vote
        doNothing().when(articleVoteRepository).flush(); // Mock flush
        doNothing().when(entityManager).refresh(article); // Mock refresh
        when(entityMapper.toDTO(article)).thenReturn(expectedDTO); // Mock mapping the refreshed article

        // Act
        ArticleDTO result = articleService.voteArticle(articleId, userId, voteRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.score(), result.score());
        assertEquals(expectedDTO.currentUserVote(), result.currentUserVote());
        verify(articleRepository, times(1)).findById(articleId);
        verify(userRepository, times(1)).findById(userId);
        verify(articleVoteRepository, times(1)).findByUserIdAndArticleId(userId, articleId);
        verify(articleVoteRepository, times(1)).save(any(ArticleVote.class));
        verify(articleVoteRepository, times(1)).flush();
        verify(entityManager, times(1)).refresh(article);
        verify(entityMapper, times(1)).toDTO(article);
    }

    @Test
    void testVoteArticle_ChangeVote() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.DOWN); // Change from UP to DOWN

        Article article = new Article();
        article.setId(articleId);
        article.setScore(1); // Initial score (was upvoted)
        article.setTitle("Test Article"); // Add other necessary fields for mapping
        article.setSummary("Test Summary");
        article.setContent("Test Content");
        article.setImageUrls(Collections.emptyList());
        article.setCreatedAt(LocalDateTime.now());
        User author = new User();
        author.setId(10L);
        author.setUsername("authoruser");
        article.setAuthor(author);
        article.setCommentCount(0L);
        article.setTags(new HashSet<>());

        User user = new User();
        user.setId(userId);

        ArticleVote existingVote = new ArticleVote();
        existingVote.setId(10L);
        existingVote.setArticle(article);
        existingVote.setUser(user);
        existingVote.setValue(1); // Existing vote was UP

        // Simulate article after vote change and refresh - update score based on action
        Article articleAfterVote = new Article();
        articleAfterVote.setId(articleId);
        articleAfterVote.setScore(article.getScore() - 2); // Score changes from +1 to -1 (subtract 1 for old vote, subtract 1 for new vote)
        // Copy other properties from the original article
        articleAfterVote.setTitle(article.getTitle());
        articleAfterVote.setSummary(article.getSummary());
        articleAfterVote.setContent(article.getContent());
        articleAfterVote.setImageUrls(article.getImageUrls());
        articleAfterVote.setCreatedAt(article.getCreatedAt());
        articleAfterVote.setAuthor(article.getAuthor());
        articleAfterVote.setCommentCount(article.getCommentCount());
        articleAfterVote.setTags(article.getTags());


        Set<String> tagNames = articleAfterVote.getTags().stream()
                .map(tag -> tag.getTagName().getName()) // Assuming ArticleTag has a TagName field with a getName() method
                .collect(Collectors.toSet());

        ArticleDTO expectedDTO = new ArticleDTO(articleId, articleAfterVote.getTitle(), articleAfterVote.getSummary(), articleAfterVote.getContent(), articleAfterVote.getImageUrls(), articleAfterVote.getCreatedAt(), articleAfterVote.getAuthor().getId(), articleAfterVote.getAuthor().getUsername(), articleAfterVote.getScore(), articleAfterVote.getCommentCount(), tagNames, -1); // currentUserVote is -1

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleVoteRepository.findByUserIdAndArticleId(userId, articleId)).thenReturn(Optional.of(existingVote)); // Existing vote found
        when(articleVoteRepository.save(existingVote)).thenReturn(existingVote); // Save updated vote
        doNothing().when(articleVoteRepository).flush(); // Mock flush
        doNothing().when(entityManager).refresh(article); // Mock refresh
        when(entityMapper.toDTO(article)).thenReturn(expectedDTO); // Mock mapping the refreshed article

        // Act
        ArticleDTO result = articleService.voteArticle(articleId, userId, voteRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.score(), result.score());
        assertEquals(expectedDTO.currentUserVote(), result.currentUserVote());
        verify(articleRepository, times(1)).findById(articleId);
        verify(userRepository, times(1)).findById(userId);
        verify(articleVoteRepository, times(1)).findByUserIdAndArticleId(userId, articleId);
        verify(articleVoteRepository, times(1)).save(existingVote); // Verify saving the existing vote
        verify(articleVoteRepository, times(1)).flush();
        verify(entityManager, times(1)).refresh(article);
        verify(entityMapper, times(1)).toDTO(article);
        verify(articleVoteRepository, never()).delete(any()); // Verify delete was not called
    }

    @Test
    void testVoteArticle_RemoveVote() {
        // Arrange
        Long articleId = 1L;
        Long userId = 1L;
        ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.UP); // Remove existing UP vote

        Article article = new Article();
        article.setId(articleId);
        article.setScore(1); // Initial score (was upvoted)
        article.setTitle("Test Article"); // Add other necessary fields for mapping
        article.setSummary("Test Summary");
        article.setContent("Test Content");
        article.setImageUrls(Collections.emptyList());
        article.setCreatedAt(LocalDateTime.now());
        User author = new User();
        author.setId(10L);
        author.setUsername("authoruser");
        article.setAuthor(author);
        article.setCommentCount(0L);
        article.setTags(new HashSet<>());

        User user = new User();
        user.setId(userId);

        ArticleVote existingVote = new ArticleVote();
        existingVote.setId(10L);
        existingVote.setArticle(article);
        existingVote.setUser(user);
        existingVote.setValue(1); // Existing vote was UP

        // Simulate article after vote removal and refresh - update score based on action
        Article articleAfterVote = new Article();
        articleAfterVote.setId(articleId);
        articleAfterVote.setScore(article.getScore() - 1); // Score decreases by 1
        // Copy other properties from the original article
        articleAfterVote.setTitle(article.getTitle());
        articleAfterVote.setSummary(article.getSummary());
        articleAfterVote.setContent(article.getContent());
        articleAfterVote.setImageUrls(article.getImageUrls());
        articleAfterVote.setCreatedAt(article.getCreatedAt());
        articleAfterVote.setAuthor(article.getAuthor());
        articleAfterVote.setCommentCount(article.getCommentCount());
        articleAfterVote.setTags(article.getTags());

        Set<String> tagNames = articleAfterVote.getTags().stream()
                .map(tag -> tag.getTagName().getName()) // Assuming ArticleTag has a TagName field with a getName() method
                .collect(Collectors.toSet());
        ArticleDTO expectedDTO = new ArticleDTO(articleId, articleAfterVote.getTitle(), articleAfterVote.getSummary(), articleAfterVote.getContent(), articleAfterVote.getImageUrls(), articleAfterVote.getCreatedAt(), articleAfterVote.getAuthor().getId(), articleAfterVote.getAuthor().getUsername(), articleAfterVote.getScore(), articleAfterVote.getCommentCount(), tagNames, 0); // currentUserVote is 0

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleVoteRepository.findByUserIdAndArticleId(userId, articleId)).thenReturn(Optional.of(existingVote)); // Existing vote found
        doNothing().when(articleVoteRepository).delete(existingVote); // Delete existing vote
        doNothing().when(articleVoteRepository).flush(); // Mock flush
        doNothing().when(entityManager).refresh(article); // Mock refresh
        when(entityMapper.toDTO(article)).thenReturn(expectedDTO); // Mock mapping the refreshed article

        // Act
        ArticleDTO result = articleService.voteArticle(articleId, userId, voteRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDTO.score(), result.score());
        assertEquals(expectedDTO.currentUserVote(), result.currentUserVote());
        verify(articleRepository, times(1)).findById(articleId);
        verify(userRepository, times(1)).findById(userId);
        verify(articleVoteRepository, times(1)).findByUserIdAndArticleId(userId, articleId);
        verify(articleVoteRepository, times(1)).delete(existingVote); // Verify delete was called
        verify(articleVoteRepository, times(1)).flush();
        verify(entityManager, times(1)).refresh(article);
        verify(entityMapper, times(1)).toDTO(article);
        verify(articleVoteRepository, never()).save(any()); // Verify save was not called
    }


    // testGetArticlesByAuthorId_Success, testGetVotedArticlesByUserId_Success
}
