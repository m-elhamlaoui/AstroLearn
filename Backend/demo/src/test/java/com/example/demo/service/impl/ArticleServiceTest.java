package com.example.demo.service;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.exception.ResourceNotFoundException; // Supposons que cette exception existe
import com.example.demo.mapper.ArticleMapper; // Supposons qu'un mapper existe
import com.example.demo.model.Article;       // Supposons que l'entité Article existe
import com.example.demo.model.User;         // Supposons que l'entité User existe
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CommentRepository; // Ajout des autres dépendances probables
import com.example.demo.repository.TagRepository;
import com.example.demo.repository.ArticleVoteRepository; // Supposons cette dépendance
import com.example.demo.mapper.CommentMapper;         // Supposons ce mapper

import org.junit.jupiter.api.BeforeEach; // Utile si une configuration commune est nécessaire avant chaque test
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.LocalDateTime; // Exemple de champ possible dans l'entité/DTO

// --- Importations statiques pour assertions et mocking ---
import static org.assertj.core.api.Assertions.assertThat; // AssertJ pour des assertions fluides
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*; // Pour when, verify, any, etc.

@ExtendWith(MockitoExtension.class) // Intégration Mockito <-> JUnit 5
class ArticleServiceImplTest { // Note: J'utilise Impl ici pour indiquer qu'on teste l'implémentation

    // --- Mocks pour les dépendances ---
    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository; // Nécessaire pour la création, mise à jour, etc.

    @Mock
    private CommentRepository commentRepository; // Pour les opérations sur les commentaires

    @Mock
    private TagRepository tagRepository; // Pour les opérations sur les tags

    @Mock
    private ArticleVoteRepository articleVoteRepository; // Pour les votes

    @Mock
    private ArticleMapper articleMapper; // Pour la conversion Article <-> ArticleDTO

    @Mock
    private CommentMapper commentMapper; // Pour la conversion Comment <-> CommentDTO


    // --- Instance sous test avec injection des mocks ---
    @InjectMocks
    private ArticleServiceImpl articleService; // Assurez-vous que votre classe d'implémentation s'appelle bien ArticleServiceImpl

    // --- Variables de Test Réutilisables (optionnel) ---
    private Article articleEntity;
    private ArticleDTO articleDTO;
    private User author;
    private Long articleId = 1L;
    private Long nonExistentArticleId = 999L;
    private Long authorId = 10L;

    @BeforeEach // Méthode exécutée avant chaque test
    void setUp() {
        // Initialiser des objets communs ici si nécessaire pour éviter la répétition
        author = new User();
        author.setId(authorId);
        author.setUsername("testAuthor");

        articleEntity = new Article();
        articleEntity.setId(articleId);
        articleEntity.setTitle("Test Title");
        articleEntity.setContent("Test Content");
        articleEntity.setAuthor(author);
        articleEntity.setCreatedAt(LocalDateTime.now());

        articleDTO = new ArticleDTO();
        articleDTO.setId(articleId);
        articleDTO.setTitle("Test Title");
        articleDTO.setContent("Test Content");
        articleDTO.setAuthorId(authorId); // Ou un DTO Auteur selon votre structure
        articleDTO.setAuthorUsername("testAuthor");
        articleDTO.setCreatedAt(articleEntity.getCreatedAt());
    }

    // --- Tests pour getArticleById ---

    @Test
    void getArticleById_WhenArticleExists_ShouldReturnArticleDTO() {
        // Arrange (Préparation)
        // 1. Dire au mock repository quoi retourner quand findById est appelé
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(articleEntity));
        // 2. Dire au mock mapper quoi retourner quand toDto est appelé avec l'entité
        when(articleMapper.toDto(articleEntity)).thenReturn(articleDTO);

        // Act (Action)
        // Appeler la méthode du service que l'on teste
        ArticleDTO result = articleService.getArticleById(articleId);

        // Assert (Vérification)
        // 1. Vérifier que le résultat n'est pas null et correspond au DTO attendu
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(articleId);
        assertThat(result.getTitle()).isEqualTo(articleDTO.getTitle());
        assertThat(result).isEqualTo(articleDTO); // Fonctionne si ArticleDTO a une méthode equals() bien définie

        // 2. Vérifier que les méthodes des mocks ont été appelées comme prévu
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleMapper, times(1)).toDto(articleEntity);
        verifyNoMoreInteractions(articleRepository, articleMapper); // Assure qu'il n'y a pas eu d'autres appels imprévus sur ces mocks
        verifyNoInteractions(userRepository, commentRepository, tagRepository, articleVoteRepository, commentMapper); // Assure que les autres mocks n'ont pas été touchés
    }

    @Test
    void getArticleById_WhenArticleDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange (Préparation)
        // Dire au mock repository de retourner un Optional vide
        when(articleRepository.findById(nonExistentArticleId)).thenReturn(Optional.empty());

        // Act & Assert (Action & Vérification de l'exception)
        // Vérifier qu'appeler la méthode lève bien l'exception attendue
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            articleService.getArticleById(nonExistentArticleId);
        });

        // Optionnel: Vérifier le message de l'exception
        assertThat(exception.getMessage()).contains("Article not found with id: " + nonExistentArticleId);

        // Assert (Vérification des interactions Mocks)
        // Vérifier que findById a été appelé
        verify(articleRepository, times(1)).findById(nonExistentArticleId);
        // Vérifier que le mapper n'a JAMAIS été appelé car aucun article n'a été trouvé
        verify(articleMapper, never()).toDto(any(Article.class));
        verifyNoInteractions(userRepository, commentRepository, tagRepository, articleVoteRepository, commentMapper);
    }

    // --- TODO: Ajoutez les tests pour les autres méthodes ---
    // @Test void createArticle_ShouldReturnCreatedArticleDTO() { ... }
    // @Test void createArticle_WhenAuthorNotFound_ShouldThrowException() { ... }
    // @Test void getAllArticles_ShouldReturnListOfArticleDTOs() { ... }
    // @Test void getAllArticles_WhenNoArticles_ShouldReturnEmptyList() { ... }
    // @Test void updateArticle_WhenUserIsAuthor_ShouldReturnUpdatedArticleDTO() { ... }
    // @Test void updateArticle_WhenArticleNotFound_ShouldThrowResourceNotFoundException() { ... }
    // @Test void updateArticle_WhenUserIsNotAuthor_ShouldThrowUnauthorizedException() { ... } // Supposer une UnauthorizedException
    // @Test void deleteArticle_WhenUserIsAuthor_ShouldDeleteSuccessfully() { ... }
    // @Test void deleteArticle_WhenUserIsAdmin_ShouldDeleteSuccessfully() { ... } // Si l'admin peut aussi
    // @Test void deleteArticle_WhenArticleNotFound_ShouldThrowResourceNotFoundException() { ... }
    // @Test void deleteArticle_WhenUserIsNotAuthorOrAdmin_ShouldThrowUnauthorizedException() { ... }
    // ... tests pour addComment, getCommentsByArticleId, deleteComment ...
    // ... tests pour voteArticle ...
    // ... tests pour addTagsToArticle, removeTagsFromArticle, getArticlesByTag ...
    // ... tests pour getRecommendedArticles, getCommentsByUserId ...

}
