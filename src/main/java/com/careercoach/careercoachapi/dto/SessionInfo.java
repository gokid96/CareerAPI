package com.careercoach.careercoachapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// SessionInfo.java - 세션 정보 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    private String sessionId;
    private long createdAt;
    private Long updatedAt;
    private String status; // CONNECTED, PROCESSING, COMPLETED, TIMEOUT, ERROR
}