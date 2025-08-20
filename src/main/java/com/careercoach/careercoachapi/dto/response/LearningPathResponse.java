// LearningPathResponse.java
package com.careercoach.careercoachapi.dto.response;

import com.careercoach.careercoachapi.dto.response.LearningRecommendation;

import java.time.LocalDateTime;
import java.util.List;

public class LearningPathResponse {

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

    // Getter/Setter
    public List<LearningRecommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<LearningRecommendation> recommendations) {
        this.recommendations = recommendations;
    }

    public String getTargetJobRole() {
        return targetJobRole;
    }

    public void setTargetJobRole(String targetJobRole) {
        this.targetJobRole = targetJobRole;
    }

    public List<String> getCurrentTechSkills() {
        return currentTechSkills;
    }

    public void setCurrentTechSkills(List<String> currentTechSkills) {
        this.currentTechSkills = currentTechSkills;
    }

    public String getOverallAssessment() {
        return overallAssessment;
    }

    public void setOverallAssessment(String overallAssessment) {
        this.overallAssessment = overallAssessment;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}