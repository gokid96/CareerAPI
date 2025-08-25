package com.careercoach.careercoachapi.dto.response;

// Lombok 어노테이션을 사용하여 반복적인 코드 생성을 줄임
import lombok.AllArgsConstructor;  // 모든 필드를 파라미터로 받는 생성자 생성
import lombok.Builder;            // 빌더 패턴 구현을 위한 어노테이션
import lombok.Data;              // getter, setter, toString, equals, hashCode 메서드 자동 생성
import lombok.NoArgsConstructor;  // 기본 생성자 생성

@Data   // 클래스의 모든 필드에 대한 기본 메서드들을 자동으로 생성
@Builder    // 빌더 패턴을 사용하여 객체 생성을 용이하게 함
@NoArgsConstructor   // 매개변수가 없는 기본 생성자를 생성
@AllArgsConstructor  // 모든 필드를 매개변수로 받는 생성자를 생성
public class LearningRecommendation {
    private String category;          // 학습 카테고리 (예: "기술 스택 심화", "프로젝트 경험", "소프트 스킬")
    private String title;             // 학습 추천 제목
    private String description;       // 학습 내용에 대한 상세 설명
    private String priority;          // 학습 우선순위 ("HIGH", "MEDIUM", "LOW")
    private String estimatedDuration; // 예상되는 학습 소요 기간
    private String learningMethod;    // 권장되는 학습 방법 (온라인 강의, 프로젝트, 독서 등)
    private String reason;            // 해당 학습이 추천된 구체적인 이유
}