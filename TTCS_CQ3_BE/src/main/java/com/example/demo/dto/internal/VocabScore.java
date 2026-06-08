package com.example.demo.dto.internal;

import com.example.demo.entity.VocabularyEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VocabScore {
    private VocabularyEntity vocab;
    private double score;    // finalScore = max(db, ml) — dùng để sắp xếp & lọc
    private double mlScore;  // mlPForget — giá trị đã lưu DB, hiển thị ra UI
}
