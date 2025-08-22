// LearningPathResponse.java
package com.careercoach.careercoachapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {
    private List<LearningRecommendation> recommendations; // 개인 맞춤형 학습 추천 항목 목록
    private String targetJobRole;                         // 목표 직무/직종
    private List<String> currentTechSkills;               // 현재 보유 기술 스택 목록
    private String overallAssessment;                     // 전체 역량 평가 및 요약

    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now(); // 학습 경로 생성 시각
}
