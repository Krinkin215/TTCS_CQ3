package com.example.demo.repository.projection;

/**
 * Projection Interface dùng cho Native Query leaderboard.
 * Spring Data JPA tự động map kết quả SQL → Interface này.
 *
 * Quy tắc đặt tên: get + tên_cột_alias_trong_SQL (camelCase).
 * VD: alias "user_id" → getUserId(), alias "total_score" → getTotalScore()
 */
public interface LeaderboardProjection {

    Long getUserId();

    String getUsername();
    
    String getFullName();

    String getEmail();

    String getAvatarUrl();

    /**
     * Tổng điểm trong khoảng thời gian được filter.
     * Khi sortBy='streak', cột này vẫn được trả về (= 0 nếu chưa có session).
     */
    Long getTotalScore();

    /**
     * current_streak từ bảng user_streak (all-time, không phụ thuộc timeFilter).
     */
    Integer getCurrentStreak();
}
