package com.example.demo.service;

import com.example.demo.dto.response.LearnedVocabStatsResponseDTO;
import com.example.demo.dto.response.VocabStatusSummaryDTO;

public interface UserVocabProgressService {
    void updateProgress(Long userId, Long vocabId, boolean is_correct);

    void updateProgress(Long userId, Long vocabId, boolean is_correct, Integer responseTime);

    LearnedVocabStatsResponseDTO getLearnedVocabStats(Long userId);

    /** Đếm số từ NEW / LEARNING / MASTERED trong một collection theo tiến độ học của user đang đăng nhập */
    VocabStatusSummaryDTO getStatusSummaryForCollection(Long userId, Long collectionId);

    /** Đếm số từ NEW / LEARNING / MASTERED trong một lesson theo tiến độ học của user đang đăng nhập */
    VocabStatusSummaryDTO getStatusSummaryForLesson(Long userId, Long lessonId);
}
