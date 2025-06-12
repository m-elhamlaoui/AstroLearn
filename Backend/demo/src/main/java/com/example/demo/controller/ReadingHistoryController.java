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
        ReadingHistoryDTO updatedHistory = readingHistoryService.logReadingTime(userId, articleId, timeSpentIncrement);
        return ResponseEntity.ok(updatedHistory);
    }

    // Get recently read articles
    @GetMapping("/recent")
    public ResponseEntity<List<ReadingHistoryDTO>> getRecentlyReadArticles(@RequestParam Long userId) {
        List<ReadingHistoryDTO> recentlyReadArticles = readingHistoryService.getRecentlyReadArticles(userId);
        return ResponseEntity.ok(recentlyReadArticles);
    }

    // Get IDs of read articles
    @GetMapping("/read-articles")
    public ResponseEntity<List<Long>> getReadArticleIds(@RequestParam Long userId) {
        List<Long> readArticleIds = readingHistoryService.getReadArticleIds(userId);
        return ResponseEntity.ok(readArticleIds);
    }
}