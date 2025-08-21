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
    private List<String> questions;   // 질문
    private String targetJobRole;     // 직종
    private List<String> techSkills;  // 기술
    private LocalDateTime generatedAt;// 생성시간

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
