// LearningRecommendation.java
package com.careercoach.careercoachapi.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LearningRecommendation {

    // Getter/Setter
    private String category;          // 예: "기술 스택 심화", "프로젝트 경험", "소프트 스킬"
    private String title;             // 추천 항목 제목
    private String description;       // 상세 설명
    private String priority;          // "HIGH", "MEDIUM", "LOW"
    private String estimatedDuration; // 예상 학습 기간
    private String learningMethod;    // 학습 방법 (온라인 강의, 프로젝트, 독서 등)
    private String reason;            // 추천 이유

    // 기본 생성자
    public LearningRecommendation() {}

    // 전체 생성자
    public LearningRecommendation(String category, String title, String description,
                                  String priority, String estimatedDuration,
                                  String learningMethod, String reason) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.estimatedDuration = estimatedDuration;
        this.learningMethod = learningMethod;
        this.reason = reason;
    }

}