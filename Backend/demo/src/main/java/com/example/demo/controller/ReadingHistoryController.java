package com.example.demo.controller;

import com.example.demo.dto.ReadingHistoryDTO;
import com.example.demo.service.ReadingHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reading-history")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    public ReadingHistoryController(ReadingHistoryService readingHistoryService) {
        this.readingHistoryService = readingHistoryService;
    }

    // Log or update reading time
    @PostMapping("/log")
    public ResponseEntity<ReadingHistoryDTO> logReadingTime(
            @RequestParam Long userId,
            @RequestParam Long articleId,
            @RequestParam int timeSpentIncrement) {
        System.out.println("ReadingHistoryController.logReadingTime called with: userId=" + userId + ", articleId=" + articleId + ", timeSpentIncrement=" + timeSpentIncrement);
        try {
            ReadingHistoryDTO updatedHistory = readingHistoryService.logReadingTime(userId, articleId, timeSpentIncrement);
            System.out.println("Reading history logged successfully: " + updatedHistory);
            return ResponseEntity.ok(updatedHistory);
        } catch (Exception e) {
            System.err.println("Error logging reading history: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Get recently read articles
    @GetMapping("/recent")
    public ResponseEntity<List<ReadingHistoryDTO>> getRecentlyReadArticles(@RequestParam Long userId) {
        System.out.println("ReadingHistoryController.getRecentlyReadArticles called for userId=" + userId);
        try {
            List<ReadingHistoryDTO> recentlyReadArticles = readingHistoryService.getRecentlyReadArticles(userId);
            System.out.println("Found " + recentlyReadArticles.size() + " recently read articles for user " + userId);
            recentlyReadArticles.forEach(article -> System.out.println("  - Article: " + article.articleId() + ", Title: " + article.articleTitle() + ", IsRead: " + article.isRead()));

            return ResponseEntity.ok(recentlyReadArticles);
        } catch (Exception e) {
            System.err.println("Error getting recently read articles: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Get IDs of read articles
    @GetMapping("/read-articles")
    public ResponseEntity<List<Long>> getReadArticleIds(@RequestParam Long userId) {
        System.out.println("ReadingHistoryController.getReadArticleIds called for userId=" + userId);
        try {
            List<Long> readArticleIds = readingHistoryService.getReadArticleIds(userId);
            System.out.println("Found " + readArticleIds.size() + " read article IDs for user " + userId + ": " + readArticleIds);
            return ResponseEntity.ok(readArticleIds);
        } catch (Exception e) {
            System.err.println("Error getting read article IDs: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}