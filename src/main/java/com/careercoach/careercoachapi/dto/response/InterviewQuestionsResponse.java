// InterviewQuestionsResponse.java
package com.careercoach.careercoachapi.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class InterviewQuestionsResponse {

    // Getter/Setter
    private List<String> questions;
    private String targetJobRole;
    private List<String> techSkills;
    private LocalDateTime generatedAt;

    // 기본 생성자
    public InterviewQuestionsResponse() {
        this.generatedAt = LocalDateTime.now();
    }

    // 전체 생성자
    public InterviewQuestionsResponse(List<String> questions, String targetJobRole, List<String> techSkills) {
        this.questions = questions;
        this.targetJobRole = targetJobRole;
        this.techSkills = techSkills;
        this.generatedAt = LocalDateTime.now();
    }

}
