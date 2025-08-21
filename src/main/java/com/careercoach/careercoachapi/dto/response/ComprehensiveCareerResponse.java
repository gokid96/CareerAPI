package com.careercoach.careercoachapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComprehensiveCareerResponse {
    private InterviewQuestionsResponse interviewQuestions; // 면접질문
    private LearningPathResponse learningPath;             // 학습경로
}