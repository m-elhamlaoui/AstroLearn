package com.example.demo.controller;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ArticleRatingDTO;
import com.example.demo.service.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
@CrossOrigin("*")
@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticleController {

    private final ArticleService articleService;


    @PostMapping("/{authorId}")
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO, @PathVariable Long authorId) {
        return ResponseEntity.ok(articleService.createArticle(articleDTO, authorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @PathVariable Long userId, @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO, userId));
    }

    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id, @PathVariable Long userId) {
        articleService.deleteArticle(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments/user/{userId}")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO, @PathVariable Long userId) {
        return ResponseEntity.ok(articleService.addComment(id, commentDTO, userId));
    }


    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticleId(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getCommentsByArticleId(id));
    }

    @DeleteMapping("/comments/{commentId}/user/{userId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @PathVariable Long userId) {
        articleService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // get all comments by userId
    @GetMapping("/comments/user/{userId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(articleService.getCommentsByUserId(userId));
    }



    // Using POST for voting action
    @PostMapping("/{id}/vote/user/{userId}") // Or get userId from @AuthenticationPrincipal
    public ResponseEntity<ArticleDTO> voteArticle(
            @PathVariable Long id,
            @PathVariable Long userId, // Ideally get this from security context
            @Valid @RequestBody ArticleVoteRequestDTO voteRequest) {
        // Long actualUserId = getUserIdFromPrincipal(userDetails); // Use authenticated user ID
        ArticleDTO updatedArticle = articleService.voteArticle(id, userId, voteRequest);
        return ResponseEntity.ok(updatedArticle);
    }

    @PutMapping("/{id}/tags")
    public ResponseEntity<ArticleDTO> addTagsToArticle(@PathVariable Long id, @RequestBody Set<String> tagNames) {
        return ResponseEntity.ok(articleService.addTagsToArticle(id, tagNames));
    }

    @DeleteMapping("/{id}/tags")
    public ResponseEntity<ArticleDTO> removeTagsFromArticle(@PathVariable Long id, @RequestBody Set<String> tagNames) {
        return ResponseEntity.ok(articleService.removeTagsFromArticle(id, tagNames));
    }

    @GetMapping("/tags/{tagName}")
    public ResponseEntity<List<ArticleDTO>> getArticlesByTag(@PathVariable String tagName) {
        return ResponseEntity.ok(articleService.getArticlesByTag(tagName));
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<ArticleDTO>> getRecommendedArticles(@PathVariable Long userId) {
        return ResponseEntity.ok(articleService.getRecommendedArticles(userId));
    }


}