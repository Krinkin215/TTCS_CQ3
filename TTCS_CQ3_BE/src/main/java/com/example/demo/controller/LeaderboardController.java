package com.example.demo.controller;

import com.example.demo.dto.response.LeaderboardResponseDTO;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller xử lý API Leaderboard.
 *
 * Endpoint: GET /api/leaderboard
 *
 * Query Parameters:
 *   - sortBy     : "score" | "streak"   (mặc định: "score")
 *   - timeFilter : "day" | "week" | "month" | "year" | "all"  (mặc định: "all")
 *   - limit      : số nguyên dương      (mặc định: 100)
 *
 * Authentication: OPTIONAL
 *   - Nếu có JWT token hợp lệ → tự động lấy userId để đánh dấu isCurrentUser & tính currentUserRank
 *   - Nếu không có token (guest) → userId = null, isCurrentUser = false, currentUserRank = 0
 *
 * Lý do không bắt buộc auth: Leaderboard là thông tin công khai,
 * user chưa đăng nhập vẫn có thể xem (như các app game phổ biến).
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<LeaderboardResponseDTO> getLeaderboard(
            @RequestParam(defaultValue = "score")  String sortBy,
            @RequestParam(defaultValue = "all")    String timeFilter,
            @RequestParam(defaultValue = "100")    int limit,
            Authentication authentication  // Spring inject null tự động nếu chưa đăng nhập
    ) {
        // Validate limit (tránh request với limit=99999)
        if (limit <= 0 || limit > 200) {
            limit = 100;
        }

        // Validate sortBy
        if (!sortBy.equalsIgnoreCase("score") && !sortBy.equalsIgnoreCase("streak")) {
            sortBy = "score";
        }

        // Validate timeFilter
        if (!timeFilter.matches("day|week|month|year|all")) {
            timeFilter = "all";
        }

        // Lấy userId của user đang đăng nhập (nếu có)
        Long currentUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                currentUserId = userOpt.get().getUserId();
            }
        }

        LeaderboardResponseDTO response = leaderboardService.getLeaderboard(sortBy, timeFilter, limit, currentUserId);
        return ResponseEntity.ok(response);
    }
}
