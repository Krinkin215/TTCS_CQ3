package com.example.demo.service;

import com.example.demo.enums.VocabLevel;
import com.example.demo.entity.CollectionEntity;
import com.example.demo.entity.CollectionVocabEntity;
import com.example.demo.entity.LessonEntity;
import com.example.demo.entity.TopicEntity;
import com.example.demo.entity.VocabularyEntity;
import com.example.demo.repository.CollectionRepository;
import com.example.demo.repository.CollectionVocabRepository;
import com.example.demo.repository.LessonRepository;
import com.example.demo.repository.TopicRepository;
import com.example.demo.repository.VocabularyRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.exception.NotFoundException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final TopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final LessonRepository lessonRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionVocabRepository collectionVocabRepository;

    @Transactional
    public Map<String, Object> importForUser(MultipartFile file, Long collectionId, Long userId) throws Exception {
        // Nếu có collectionId → kiểm tra quyền
        CollectionEntity collection = null;
        if (collectionId != null) {
            collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy bộ sưu tập"));
            if (!collection.getUser().getUserId().equals(userId)) {
                throw new RuntimeException("Bạn không có quyền import vào bộ sưu tập này");
            }
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

        List<VocabularyEntity> vocabList = new ArrayList<>();
        List<CollectionVocabEntity> collectionVocabList = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        int lineNum = 1;
        String[] line;
        int successCount = 0;

        while ((line = csvReader.readNext()) != null) {
            lineNum++;
            try {
                if (line.length < 6) {
                    errors.add("Dòng " + lineNum + ": thiếu cột (yêu cầu ít nhất 6 cột)");
                    continue;
                }

                String word = line[0].trim();
                String pronunciation = line[1].trim();
                String wordType = line[2].trim();
                String meaning = line[3].trim();
                String levelStr = line[4].trim();
                String example = line[5].trim();

                if (word.isBlank() || meaning.isBlank()) {
                    errors.add("Dòng " + lineNum + ": word và meaning không được để trống");
                    continue;
                }

                VocabLevel level = VocabLevel.A1; // Default
                try {
                    if (!levelStr.isBlank()) {
                        level = VocabLevel.valueOf(levelStr.toUpperCase());
                    }
                } catch (IllegalArgumentException e) {
                    errors.add("Dòng " + lineNum + ": level không hợp lệ mốc (A1, A2, B1, B2, C1, C2)");
                    continue;
                }

                // Nếu có collection → kiểm tra từ đã tồn tại trong collection chưa
                if (collection != null) {
                    boolean existsInCollection = collectionVocabRepository
                            .existsByCollectionIdAndWordIgnoreCase(collectionId, word);
                    if (existsInCollection) {
                        errors.add("Dòng " + lineNum + ": Từ '" + word + "' đã tồn tại trong bộ sưu tập");
                        continue;
                    }
                }

                // Kiểm tra từ đã tồn tại trong hệ thống chưa
                VocabularyEntity vocab = vocabularyRepository.findByWordAndCreatedBy(word, userId).orElse(null);
                if (vocab == null) {
                    // Từ chưa tồn tại → tạo mới
                    vocab = VocabularyEntity.builder()
                            .word(word)
                            .pronunciation(pronunciation)
                            .wordType(wordType)
                            .meaning(meaning)
                            .level(level)
                            .example(example)
                            .createdBy(userId)
                            .lesson(null) // Từ do user tạo không thuộc bài học cụ thể nào
                            .build();
                    vocabList.add(vocab);
                } else {
                    // Từ đã tồn tại trong từ vựng của user → bỏ qua
                    errors.add("Dòng " + lineNum + ": Từ '" + word + "' đã tồn tại trong từ vựng của bạn");
                    continue;
                }

                // Nếu có collection → thêm vào collection
                if (collection != null) {
                    CollectionVocabEntity cv = CollectionVocabEntity.builder()
                            .collection(collection)
                            .vocab(vocab)
                            .build();
                    collectionVocabList.add(cv);
                }

                successCount++;

            } catch (Exception e) {
                errors.add("Dòng " + lineNum + ": lỗi - " + e.getMessage());
            }
        }

        // Save vocab MỚI trước để có ID
        vocabularyRepository.saveAll(vocabList);
        vocabularyRepository.flush(); // Đảm bảo ID đã được gán

        // Nếu có collection → lưu quan hệ collection-vocab
        if (collection != null && !collectionVocabList.isEmpty()) {
            collectionVocabRepository.saveAll(collectionVocabList);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        return result;
    }

    @Transactional
    public Map<String, Object> importForAdmin(MultipartFile file, Long defaultTopicId, Long defaultLessonId,
            Long adminId) throws Exception {

        TopicEntity defaultTopic = defaultTopicId != null ? topicRepository.findById(defaultTopicId).orElse(null)
                : null;
        LessonEntity defaultLesson = defaultLessonId != null ? lessonRepository.findById(defaultLessonId).orElse(null)
                : null;

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

        List<VocabularyEntity> vocabList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, LessonEntity> lessonCache = new HashMap<>();

        int lineNum = 1;
        String[] line;
        int successCount = 0;

        while ((line = csvReader.readNext()) != null) {
            lineNum++;
            try {
                if (line.length < 6) {
                    errors.add("Dòng " + lineNum + ": thiếu cột (yêu cầu ít nhất 6 cột cơ bản)");
                    continue;
                }

                String word = line[0].trim();
                String pronunciation = line[1].trim();
                String wordType = line[2].trim();
                String meaning = line[3].trim();
                String levelStr = line[4].trim();
                String example = line[5].trim();
                String topicName = line.length > 6 ? line[6].trim() : "";
                String lessonName = line.length > 7 ? line[7].trim() : "";

                if (word.isBlank() || meaning.isBlank()) {
                    errors.add("Dòng " + lineNum + ": word và meaning không được để trống");
                    continue;
                }

                VocabLevel level = VocabLevel.A1;
                try {
                    if (!levelStr.isBlank()) {
                        level = VocabLevel.valueOf(levelStr.toUpperCase());
                    }
                } catch (IllegalArgumentException e) {
                    errors.add("Dòng " + lineNum + ": level không hợp lệ");
                    continue;
                }

                // Determine Lesson
                LessonEntity targetLesson = defaultLesson;
                if (!lessonName.isBlank() && defaultTopic != null) {
                    targetLesson = lessonCache.computeIfAbsent(lessonName, key -> {
                        return lessonRepository.findByLessonNameAndTopic(lessonName, defaultTopic)
                                .orElseGet(() -> {
                                    LessonEntity newLesson = new LessonEntity();
                                    newLesson.setLessonName(lessonName);
                                    newLesson.setTopic(defaultTopic);
                                    newLesson.setDifficulty(1);
                                    return lessonRepository.save(newLesson);
                                });
                    });
                }

                if (targetLesson == null) {
                    errors.add("Dòng " + lineNum + ": không xác định được bài học (Lesson)");
                    continue;
                }

                // Cập nhật từ hoặc tạo mới
                // VocabularyEntity vocab = vocabularyRepository.findByWordAndLesson(word,
                // targetLesson)
                // .orElse(new VocabularyEntity());

                VocabularyEntity vocab = vocabularyRepository.findByWord(word).orElse(null);

                if (vocab != null) {
                    // Neu tu da xuat hien trong he thong, kiem tra xem thuoc bai hoc nao
                    boolean isSameLesson = vocab.getLesson() != null && targetLesson != null
                            && vocab.getLesson().getLessonId().equals(targetLesson.getLessonId());

                    if (isSameLesson) {
                        // Tu da xuat hien trong CUNG bai hoc nay -> Bao loi trung lap, bo qua
                        errors.add("Dòng " + lineNum + ": từ '" + word + "' đã tồn tại trong bài học này");
                        continue;
                    } else {
                        // Tu co o bai hoc khac -> Bao loi trung lap va bo qua tu nay
                        errors.add("Dòng " + lineNum + ": từ '" + word + "' đã tồn tại trong bài học khác");
                        continue;
                    }

                } else {
                    // Neu chua tung co trong he thong -> Them moi
                    vocab = new VocabularyEntity();
                }

                vocab.setLesson(targetLesson);
                vocab.setWord(word);
                vocab.setWordType(wordType);
                vocab.setMeaning(meaning);
                vocab.setPronunciation(pronunciation);
                vocab.setExample(example);
                vocab.setLevel(level);
                vocab.setCreatedBy(adminId);

                vocabList.add(vocab);
                successCount++;

            } catch (Exception e) {
                errors.add("Dòng " + lineNum + ": lỗi - " + e.getMessage());
            }
        }

        vocabularyRepository.saveAll(vocabList);

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        return result;
    }
}
