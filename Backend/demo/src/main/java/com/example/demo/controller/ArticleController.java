package com.example.demo.controller;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.dto.ArticleVoteRequestDTO;
import com.example.demo.dto.CommentDTO;
import com.example.demo.dto.ArticleRatingDTO;
import com.example.demo.service.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import
import org.springframework.security.core.userdetails.UserDetails; // Import
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.VoteType; // Import VoteType

import java.util.List;
import java.util.Set;
@CrossOrigin("*")
@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticleController {

     private final ArticleService articleService;
 
 
     // Use authenticated user as author, remove authorId from path
     @PostMapping 
     public ResponseEntity<ArticleDTO> createArticle(
             @Valid @RequestBody ArticleDTO articleDTO, 
             @AuthenticationPrincipal UserDetails userDetails) {
         Long actualUserId = getUserIdFromPrincipal(userDetails);
         if (actualUserId == null) {
             // Handle unauthenticated user trying to create article
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
         }
         ArticleDTO createdArticle = articleService.createArticle(articleDTO, actualUserId);
         // Return 201 Created status
         return ResponseEntity.status(HttpStatus.CREATED).body(createdArticle); 
     }
 
     // Pass authenticated user details to service
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) { // Inject principal
        Long userId = getUserIdFromPrincipal(userDetails); // Helper to extract ID
        return ResponseEntity.ok(articleService.getArticleById(id, userId));
    }

    // Pass authenticated user details to service
    @GetMapping
    public ResponseEntity<?> getAllArticles(
            Pageable pageable,
            @RequestParam(required = false) List<Long> ids,
            @AuthenticationPrincipal UserDetails userDetails) { // Inject principal
        Long userId = getUserIdFromPrincipal(userDetails); // Helper to extract ID
        
        // If IDs are provided, fetch specific articles
        if (ids != null && !ids.isEmpty()) {
            System.out.println("Fetching articles by IDs: " + ids);
            List<ArticleDTO> articles = articleService.getArticlesByIds(ids, userId);
            return ResponseEntity.ok(articles);
        }
        
        // Otherwise, return paginated results
        return ResponseEntity.ok(articleService.getAllArticles(pageable, userId));
    }

    // Pass authenticated user details to service
    @GetMapping("/sorted")
    public ResponseEntity<Page<ArticleDTO>> getAllArticlesSorted(
            Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) { // Inject principal
        Long userId = getUserIdFromPrincipal(userDetails); // Helper to extract ID
        return ResponseEntity.ok(articleService.getAllArticlesSorted(pageable, userId));
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
            // @PathVariable Long userId, // Remove userId from path if using principal
            @AuthenticationPrincipal UserDetails userDetails, // Inject principal
            @Valid @RequestBody ArticleVoteRequestDTO voteRequest) {
        Long actualUserId = getUserIdFromPrincipal(userDetails); // Use authenticated user ID
        if (actualUserId == null) {
             // Handle cases where user is not authenticated appropriately
             // For now, maybe throw an exception or return an error response
             // Depending on how you want unauthenticated users to behave.
             // Let's assume voting requires authentication.
             return ResponseEntity.status(401).build(); // Unauthorized
        }
        ArticleDTO updatedArticle = articleService.voteArticle(id, actualUserId, voteRequest);
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
    public ResponseEntity<List<ArticleDTO>> getRecommendedArticles(
             @PathVariable Long userId, // Keep this if recommendations are based on path variable user
             @AuthenticationPrincipal UserDetails userDetails) { // Also get current user if needed
        // Decide if recommendations depend on the path userId or the logged-in userDetails.id
        // Assuming it depends on the path variable for now.
         return ResponseEntity.ok(articleService.getRecommendedArticles(userId));
     }
 
     // Endpoint to get articles published by a user
     @GetMapping("/user/{authorId}")
     public ResponseEntity<List<ArticleDTO>> getArticlesByAuthor(
             @PathVariable Long authorId,
             @AuthenticationPrincipal UserDetails userDetails) {
         Long currentUserId = getUserIdFromPrincipal(userDetails);
         return ResponseEntity.ok(articleService.getArticlesByAuthorId(authorId, currentUserId));
     }
 
     // Endpoint to get articles voted on by a user
     @GetMapping("/votes/user/{userId}")
     public ResponseEntity<List<ArticleDTO>> getVotedArticles(
             @PathVariable Long userId,
             @RequestParam VoteType voteType, // Expect "UP" or "DOWN"
             @AuthenticationPrincipal UserDetails userDetails) {
         Long currentUserId = getUserIdFromPrincipal(userDetails);
         // Optional: Add check if userId from path matches currentUserId for privacy
         // if (!userId.equals(currentUserId)) { return ResponseEntity.status(403).build(); }
         return ResponseEntity.ok(articleService.getVotedArticlesByUserId(userId, voteType, currentUserId));
     }
 
     // Helper method to extract user ID from UserDetails principal
    // Adapt this based on your UserDetails implementation (e.g., if it's a custom class)
    private Long getUserIdFromPrincipal(UserDetails userDetails) {
        System.out.println("Attempting to get UserID from Principal: " + userDetails); // Log input

        // Corrected path for UserDetailsImpl
        if (userDetails instanceof com.example.demo.security.UserDetailsImpl customUserDetails) {
             // If using a custom UserDetails implementation that holds the ID
             Long userId = customUserDetails.getId();
             System.out.println("Extracted UserID from UserDetailsImpl: " + userId); // Log extracted ID
             return userId;
        } else if (userDetails != null) {
            System.out.println("UserDetails is not an instance of UserDetailsImpl. Type: " + userDetails.getClass().getName());
            // Attempt to parse ID from username if it's stored there and is numeric
            // This is less ideal and depends heavily on your setup.
            String username = userDetails.getUsername();
            System.out.println("Attempting to parse UserID from username: " + username);
            try {
                Long userId = Long.parseLong(username);
                System.out.println("Parsed UserID from username: " + userId);
                return userId;
            } catch (NumberFormatException e) {
                // Handle case where username is not the ID (e.g., email)
                // You might need to fetch the User entity based on the username
                System.err.println("Warning: Could not parse userId from UserDetails username. Username: '" + username + "'. Returning null.");
                return null; // Or throw an exception / fetch user by username
            }
        }
        System.out.println("UserDetails principal was null. Returning null userId.");
        return null; // Return null if user is not authenticated or ID cannot be determined
    }
}
