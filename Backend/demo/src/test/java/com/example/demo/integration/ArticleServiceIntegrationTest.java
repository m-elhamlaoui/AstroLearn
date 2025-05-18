package com.example.demo.integration;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.model.VoteType;
import com.example.demo.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ArticleServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        setUpTestUser();
    }

    @Test
    void shouldCreateAndRetrieveArticle() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags = new HashSet<>();
        tags.add("astronomy");
        tags.add("space");

        ArticleDTO articleDTO = new ArticleDTO(
            null, // id will be generated
            "Test Article",
            "This is a test article summary",
            "This is the full content of the test article.",
            "article-image.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            tags
        );

        // When
        ArticleDTO savedArticle = articleService.createArticle(articleDTO, testUserId);

        // Then
        assertThat(savedArticle.id()).isNotNull();
        ArticleDTO foundArticle = articleService.getArticleById(savedArticle.id());
        assertThat(foundArticle).isNotNull();
        assertThat(foundArticle.title()).isEqualTo("Test Article");
        assertThat(foundArticle.tags()).containsExactlyInAnyOrder("astronomy", "space");
    }

    @Test
    void shouldListAllArticles() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags1 = new HashSet<>();
        tags1.add("astronomy");
        
        Set<String> tags2 = new HashSet<>();
        tags2.add("space");

        ArticleDTO article1 = new ArticleDTO(
            null,
            "Article 1",
            "Summary 1",
            "Content 1",
            "image1.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            tags1
        );

        ArticleDTO article2 = new ArticleDTO(
            null,
            "Article 2",
            "Summary 2",
            "Content 2",
            "image2.jpg",
            now.plusDays(1),
            testUserId,
            "testuser",
            0,
            0L,
            tags2
        );

        articleService.createArticle(article1, testUserId);
        articleService.createArticle(article2, testUserId);

        // When
        List<ArticleDTO> articles = articleService.getAllArticles();

        // Then
        assertThat(articles).hasSize(2);
        assertThat(articles).extracting(ArticleDTO::title)
                          .containsExactlyInAnyOrder("Article 1", "Article 2");
    }

    @Test
    void shouldUpdateArticle() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags = new HashSet<>();
        tags.add("astronomy");

        ArticleDTO originalArticle = new ArticleDTO(
            null,
            "Original Article",
            "Original Summary",
            "Original Content",
            "original-image.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            tags
        );

        ArticleDTO savedArticle = articleService.createArticle(originalArticle, testUserId);

        // When
        Set<String> updatedTags = new HashSet<>();
        updatedTags.add("astronomy");
        updatedTags.add("space");

        ArticleDTO updatedArticleDTO = new ArticleDTO(
            savedArticle.id(),
            "Updated Article",
            "Updated Summary",
            "Updated Content",
            "updated-image.jpg",
            savedArticle.createdAt(),
            savedArticle.authorId(),
            savedArticle.authorUsername(),
            savedArticle.score(),
            savedArticle.commentCount(),
            updatedTags
        );

        ArticleDTO updatedArticle = articleService.updateArticle(savedArticle.id(), updatedArticleDTO, testUserId);

        // Then
        assertThat(updatedArticle.title()).isEqualTo("Updated Article");
        assertThat(updatedArticle.tags()).containsExactlyInAnyOrder("astronomy", "space");
    }

    @Test
    void shouldAddAndRetrieveComments() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags = new HashSet<>();
        tags.add("astronomy");

        ArticleDTO article = new ArticleDTO(
            null,
            "Comment Test Article",
            "Summary",
            "Content",
            "image.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            tags
        );

        ArticleDTO savedArticle = articleService.createArticle(article, testUserId);

        // When
        CommentDTO commentDTO = new CommentDTO(
            null,
            "This is a test comment",
            savedArticle.id(),
            now,
            testUserId,
            "testuser"
        );

        CommentDTO savedComment = articleService.addComment(savedArticle.id(), commentDTO, testUserId);
        List<CommentDTO> comments = articleService.getCommentsByArticleId(savedArticle.id());

        // Then
        assertThat(savedComment.id()).isNotNull();
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).content()).isEqualTo("This is a test comment");
    }

    @Test
    void shouldVoteOnArticle() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<String> tags = new HashSet<>();
        tags.add("astronomy");

        ArticleDTO article = new ArticleDTO(
            null,
            "Vote Test Article",
            "Summary",
            "Content",
            "image.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            tags
        );

        ArticleDTO savedArticle = articleService.createArticle(article, testUserId);

        // When
        ArticleVoteRequestDTO voteRequest = new ArticleVoteRequestDTO(VoteType.UP);
        ArticleDTO votedArticle = articleService.voteArticle(savedArticle.id(), testUserId, voteRequest);

        // Then
        assertThat(votedArticle.score()).isEqualTo(1);
    }

    @Test
    void shouldAddAndRemoveTags() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<String> initialTags = new HashSet<>();
        initialTags.add("astronomy");

        ArticleDTO article = new ArticleDTO(
            null,
            "Tag Test Article",
            "Summary",
            "Content",
            "image.jpg",
            now,
            testUserId,
            "testuser",
            0,
            0L,
            initialTags
        );

        ArticleDTO savedArticle = articleService.createArticle(article, testUserId);

        // When adding tags
        Set<String> tagsToAdd = new HashSet<>();
        tagsToAdd.add("space");
        tagsToAdd.add("nasa");
        
        ArticleDTO articleWithAddedTags = articleService.addTagsToArticle(savedArticle.id(), tagsToAdd);
        
        // Then
        assertThat(articleWithAddedTags.tags()).containsExactlyInAnyOrder("astronomy", "space", "nasa");
        
        // When removing tags
        Set<String> tagsToRemove = new HashSet<>();
        tagsToRemove.add("space");
        
        ArticleDTO articleWithRemovedTags = articleService.removeTagsFromArticle(savedArticle.id(), tagsToRemove);
        
        // Then
        assertThat(articleWithRemovedTags.tags()).containsExactlyInAnyOrder("astronomy", "nasa");
    }
} 
