// LearningPathResponse.java
package com.careercoach.careercoachapi.dto.response;

import com.careercoach.careercoachapi.dto.response.LearningRecommendation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class LearningPathResponse {

    // Getter/Setter
    private List<LearningRecommendation> recommendations;
    private String targetJobRole;
    private List<String> currentTechSkills;
    private String overallAssessment;
    private LocalDateTime generatedAt;

    // 기본 생성자
    public LearningPathResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    // 전체 생성자
    public LearningPathResponse(List<LearningRecommendation> recommendations,
                                String targetJobRole,
                                List<String> currentTechSkills,
                                String overallAssessment) {
        this.recommendations = recommendations;
        this.targetJobRole = targetJobRole;
        this.currentTechSkills = currentTechSkills;
        this.overallAssessment = overallAssessment;
        this.generatedAt = LocalDateTime.now();
    }

}