package com.example.demo.dto.internal;

import com.example.demo.entity.VocabularyEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureDTO {
    private VocabularyEntity vocab;
    private int correctCount;
    private int incorrectCount;
    private double rememberRate;
    private int daysSinceLastLearn;
    private double responseTime;
    /** Giá trị pForget đang lưu trong DB (có thể null nếu chưa có lịch sử ML). */
    private Double dbPForget;
    /** Primary key của bản ghi UserVocabProgress mới nhất, dùng để cập nhật pForget. */
    private Long progressId;
}
