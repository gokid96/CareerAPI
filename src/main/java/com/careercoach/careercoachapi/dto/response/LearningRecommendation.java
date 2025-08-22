// LearningRecommendation.java
package com.careercoach.careercoachapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecommendation {
    private String category;          // 예: "기술 스택 심화", "프로젝트 경험", "소프트 스킬"
    private String title;             // 추천 항목 제목
    private String description;       // 상세 설명
    private String priority;          // "HIGH", "MEDIUM", "LOW"
    private String estimatedDuration; // 예상 학습 기간
    private String learningMethod;    // 학습 방법 (온라인 강의, 프로젝트, 독서 등)
    private String reason;            // 추천 이유
}