// InterviewQuestionsResponse.java
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
public class InterviewQuestionsResponse {
    private List<String> questions;   // 생성된 면접 질문 목록
    private String targetJobRole;     // 목표 직무/직종 (예: "백엔드 개발자", "프론트엔드 개발자")
    private List<String> techSkills;  // 지원자 보유 기술 스택 목록

    @Builder.Default
    private LocalDateTime generatedAt = LocalDateTime.now(); // 면접 질문 생성 시각
}
