package com.example.demo.controller;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ArticleRatingDTO;
import com.example.demo.service.ArticleService;
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


    @PostMapping
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO, @RequestParam Long authorId) {
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

    @PutMapping("/{id}")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO, @RequestParam Long userId) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id, @RequestParam Long userId) {
        articleService.deleteArticle(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO, @RequestParam Long userId) {
        return ResponseEntity.ok(articleService.addComment(id, commentDTO, userId));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByArticleId(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getCommentsByArticleId(id));
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<ArticleRatingDTO> rateArticle(@PathVariable Long id, @RequestBody ArticleRatingDTO ratingDTO, @RequestParam Long userId) {
        return ResponseEntity.ok(articleService.rateArticle(id, ratingDTO, userId));
    }

    @GetMapping("/{id}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getAverageRating(id));
    }

    @PostMapping("/{id}/tags")
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

    @GetMapping("/recommendations")
    public ResponseEntity<List<ArticleDTO>> getRecommendedArticles(@RequestParam Long userId) {
        return ResponseEntity.ok(articleService.getRecommendedArticles(userId));
    }
}