package com.example.demo.service;

import com.example.demo.dto.response.LeaderboardResponseDTO;

public interface LeaderboardService {

    /**
     * Lấy bảng xếp hạng.
     *
     * @param sortBy     "score" hoặc "streak"
     * @param timeFilter "day" | "week" | "month" | "year" | "all"
     * @param limit      Số user tối đa trả về (mặc định 100)
     * @param userId     ID user hiện tại (nullable) – để đánh dấu isCurrentUser và tính rank
     */
    LeaderboardResponseDTO getLeaderboard(String sortBy, String timeFilter, int limit, Long userId);
}
