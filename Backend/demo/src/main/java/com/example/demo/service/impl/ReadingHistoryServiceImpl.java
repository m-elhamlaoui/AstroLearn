package com.example.demo.service.impl;

import com.example.demo.dto.ReadingHistoryDTO;
import com.example.demo.model.Article;
import com.example.demo.model.ReadingHistory;
import com.example.demo.model.User;
import com.example.demo.repository.ArticleRepository;
import com.example.demo.repository.ReadingHistoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReadingHistoryService;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReadingHistoryServiceImpl implements ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final EntityMapper entityMapper;

    public ReadingHistoryServiceImpl(ReadingHistoryRepository readingHistoryRepository,
                                     UserRepository userRepository,
                                     ArticleRepository articleRepository,
                                     EntityMapper entityMapper) {
        this.readingHistoryRepository = readingHistoryRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional
    public ReadingHistoryDTO logReadingTime(Long userId, Long articleId, int timeSpentIncrement) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", articleId));

        ReadingHistory readingHistory = readingHistoryRepository.findByUserAndArticle(user, article)
                .orElseGet(() -> new ReadingHistory(null, user, article, false, 0, LocalDateTime.now()));

        readingHistory.setTimeSpentSeconds(readingHistory.getTimeSpentSeconds() + timeSpentIncrement);
        readingHistory.setLastAccessed(LocalDateTime.now());
        readingHistory.updateIsRead();

        ReadingHistory savedHistory = readingHistoryRepository.save(readingHistory);
        return entityMapper.toDTO(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingHistoryDTO> getRecentlyReadArticles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Article> articles = readingHistoryRepository.findRecentlyReadArticles(userId);
        return articles.stream()
                .map(article -> new ReadingHistoryDTO(
                        null, false, 0, null, userId, article.getId(), article.getTitle()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getReadArticleIds(Long userId) {
        return readingHistoryRepository.findReadArticleIdsByUserId(userId).stream().toList();
    }
}