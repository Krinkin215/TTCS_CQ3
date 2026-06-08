package com.example.demo.dto.response;

import com.example.demo.enums.VocabLevel;
import com.example.demo.enums.VocabStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VocabularyResponseDTO {

    private Long lessonId;
    private String lessonName;
    //không trả về topic và difficulty(vì đây là độ khó lesson, không phải độ khó từ -> không cần thiết)
    private Long vocabId;
    private String word;
    private String wordType;
    private String meaning;
    private String pronunciation;
    private String example;
    private String audio;
    private VocabLevel level;
    private VocabStatus status;
    private Double pForget;
}
