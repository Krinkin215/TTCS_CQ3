package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Summary số từ theo trạng thái học của user trong một nguồn (collection hoặc lesson).
 * - newCount    : chưa học lần nào (không có bản ghi progress hoặc totalAttempts = 0)
 * - learningCount : đang học (có bản ghi, pForget >= 0.2)
 * - masteredCount : đã thuộc (pForget < 0.2)
 * - totalCount  : tổng số từ trong nguồn
 */
@Data
@Builder
public class VocabStatusSummaryDTO {
    private long newCount;
    private long learningCount;
    private long masteredCount;
    private long totalCount;
}
