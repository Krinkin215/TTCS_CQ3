import { apiRequest } from '../apiClient';

export async function updateProgress(userId, vocabId, isCorrect) {
  return apiRequest(`/api/progress/update?userId=${userId}&vocabId=${vocabId}&isCorrect=${isCorrect}`, { 
    method: 'POST', 
    auth: true 
  });
}

export async function getLearnedVocabStats(userId) {
  return apiRequest(`/api/progress/learned-statistics?userId=${userId}`, { 
    method: 'GET', 
    auth: true 
  });
}

/**
 * Lấy số từ theo trạng thái thực tế (NEW/LEARNING/MASTERED) trong một collection.
 * @returns {{ newCount, learningCount, masteredCount, totalCount }}
 */
export async function getCollectionStatusSummary(collectionId) {
  return apiRequest(`/api/progress/summary/collection/${collectionId}`, {
    method: 'GET',
    auth: true,
  });
}

/**
 * Lấy số từ theo trạng thái thực tế (NEW/LEARNING/MASTERED) trong một lesson.
 * @returns {{ newCount, learningCount, masteredCount, totalCount }}
 */
export async function getLessonStatusSummary(lessonId) {
  return apiRequest(`/api/progress/summary/lesson/${lessonId}`, {
    method: 'GET',
    auth: true,
  });
}
