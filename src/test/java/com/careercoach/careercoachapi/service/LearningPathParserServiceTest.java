// LearningPathParserServiceTest.java
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.service.LearningPathParserService;
import com.careercoach.careercoachapi.dto.response.LearningRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LearningPathParserServiceTest {

    @InjectMocks
    private LearningPathParserService learningPathParserService;

    private String sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = """
            구직자님의 현재 상태를 분석한 결과, 백엔드 개발자로서 탄탄한 기초를 갖추고 계시지만 추가적인 역량 강화가 필요합니다.
            
            [기술 스택 심화] Spring Boot 고급 기능 마스터
            - 설명: Spring Security, Spring Cloud, Spring Batch 등 엔터프라이즈급 기능 습득
            - 우선순위: HIGH
            - 예상 기간: 2-3개월
            - 학습 방법: 온라인 강의 + 실습 프로젝트
            - 추천 이유: 현재 Spring Boot 경험을 바탕으로 더 깊이 있는 기술 역량 확보 가능
            
            [신기술 학습] 컨테이너 오케스트레이션 (Kubernetes)
            - 설명: Docker 경험을 바탕으로 Kubernetes 클러스터 관리 및 운영 기술 학습
            - 우선순위: MEDIUM
            - 예상 기간: 1-2개월
            - 학습 방법: 실습 위주의 온라인 코스 + 개인 프로젝트
            - 추천 이유: 현재 Docker 경험이 있어 학습 곡선이 완만하며, 최신 DevOps 트렌드에 부합
            """;
    }

    @Test
    void testParseLearningRecommendations_Success() {
        // When
        List<LearningRecommendation> recommendations =
                learningPathParserService.parseLearningRecommendations(sampleResponse);

        // Then
        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());

        // 첫 번째 추천 검증
        LearningRecommendation first = recommendations.get(0);
        assertEquals("기술 스택 심화", first.getCategory());
        assertEquals("Spring Boot 고급 기능 마스터", first.getTitle());
        assertEquals("HIGH", first.getPriority());
        assertEquals("2-3개월", first.getEstimatedDuration());
        assertTrue(first.getDescription().contains("Spring Security"));

        // 두 번째 추천 검증
        LearningRecommendation second = recommendations.get(1);
        assertEquals("신기술 학습", second.getCategory());
        assertEquals("컨테이너 오케스트레이션 (Kubernetes)", second.getTitle());
        assertEquals("MEDIUM", second.getPriority());
        // 두 번째 추천 검증
        LearningRecommendation second2 = recommendations.get(1);
        assertEquals("신기술 학습", second2.getCategory());
        assertEquals("컨테이너 오케스트레이션 (Kubernetes)", second2.getTitle());
        assertEquals("MEDIUM", second2.getPriority());
        assertEquals("1-2개월", second2.getEstimatedDuration());
        assertTrue(second2.getDescription().contains("Kubernetes"));
    }

    @Test
    void testParseLearningRecommendations_EmptyResponse() {
        // Given
        String emptyResponse = "";

        // When
        List<LearningRecommendation> recommendations =
                learningPathParserService.parseLearningRecommendations(emptyResponse);

        // Then
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void testExtractOverallAssessment_Success() {
        // When
        String assessment = learningPathParserService.extractOverallAssessment(sampleResponse);

        // Then
        assertNotNull(assessment);
        assertTrue(assessment.contains("현재 상태를 분석한 결과"));
        assertTrue(assessment.contains("백엔드 개발자"));
    }

    @Test
    void testExtractOverallAssessment_NoAssessment() {
        // Given
        String responseWithoutAssessment = """
            [기술 스택 심화] Spring Boot 고급 기능 마스터
            - 설명: Spring Security 학습
            - 우선순위: HIGH
            """;

        // When
        String assessment = learningPathParserService.extractOverallAssessment(responseWithoutAssessment);

        // Then
        assertEquals("개인 맞춤형 학습 경로가 생성되었습니다.", assessment);
    }
}
