// ComprehensiveCareerResponse.java
package com.careercoach.careercoachapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComprehensiveCareerResponse {
    private InterviewQuestionsResponse interviewQuestions; // 면접 질문 응답 데이터
    private LearningPathResponse learningPath;             // 학습 경로 추천 응답 데이터
}