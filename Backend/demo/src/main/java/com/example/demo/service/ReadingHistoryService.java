package com.example.demo.service;

import com.example.demo.dto.ReadingHistoryDTO;
import java.util.List;

public interface ReadingHistoryService {

    /*
     * Records or updates the reading history for a user and an article.
     * Updates time spent and recalculates isRead status. Sets last accessed time.
     * @param userId User ID
     * @param articleId Article ID
     * @param timeSpentIncrement Seconds spent in the current session
     * @return Updated ReadingHistoryDTO
     */
    ReadingHistoryDTO logReadingTime(Long userId, Long articleId, int timeSpentIncrement);

    /*
     * Gets the reading history for a specific user and article.
     * @param userId User ID
     * @param articleId Article ID
     * @return ReadingHistoryDTO
     * @throws ResourceNotFoundException if history not found
     */
    ReadingHistoryDTO getReadingHistory(Long userId, Long articleId);

    /*
     * Gets all reading history entries for a user.
     * @param userId User ID
     * @return List of ReadingHistoryDTOs
     */
    List<ReadingHistoryDTO> getReadingHistoryForUser(Long userId);
}