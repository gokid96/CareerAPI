// CareerCoachServiceTest.java
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CareerCoachService 테스트")
class CareerCoachServiceTest {

    @Autowired
    private CareerCoachService careerCoachService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockWebServer mockWebServer;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public WebClient testWebClient() {
            return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        }
    }

    private ResumeInfoRequest testRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 요청 데이터 설정
        testRequest = new ResumeInfoRequest(
            "3년차 백엔드 개발자, Spring Boot 기반 커머스 서비스 개발",
            "백엔드 개발자",
            List.of("Java", "Spring Boot", "MySQL", "AWS")
        );
        
        // @Value 필드 설정
        ReflectionTestUtils.setField(careerCoachService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(careerCoachService, "model", "gpt-4o-mini");
    }

    @Test
    @DisplayName("면접 질문 생성 성공 테스트")
    void generateInterviewQuestions_Success() throws Exception {
        // Given
        String mockApiResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\\"questions\\": [\\"질문1\\", \\"질문2\\", \\"질문3\\", \\"질문4\\", \\"질문5\\"], \\"targetJobRole\\": \\"백엔드 개발자\\", \\"techSkills\\": [\\"Java\\", \\"Spring Boot\\"]}"
                  }
                }
              ]
            }
            """;

        String expectedJsonContent = """
            {"questions": ["질문1", "질문2", "질문3", "질문4", "질문5"], "targetJobRole": "백엔드 개발자", "techSkills": ["Java", "Spring Boot"]}
            """;

        InterviewQuestionsResponse expectedResponse = new InterviewQuestionsResponse();
        expectedResponse.setQuestions(List.of("질문1", "질문2", "질문3", "질문4", "질문5"));
        expectedResponse.setTargetJobRole("백엔드 개발자");
        expectedResponse.setTechSkills(List.of("Java", "Spring Boot"));

        // WebClient 모킹
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockApiResponse));

        // ObjectMapper 모킹
        when(objectMapper.readValue(eq(mockApiResponse), eq(java.util.Map.class)))
            .thenReturn(createMockResponseMap());
        when(objectMapper.readValue(eq(expectedJsonContent), eq(InterviewQuestionsResponse.class)))
            .thenReturn(expectedResponse);

        // When
        InterviewQuestionsResponse result = careerCoachService.generateInterviewQuestions(testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuestions()).hasSize(5);
        assertThat(result.getTargetJobRole()).isEqualTo("백엔드 개발자");
        assertThat(result.getTechSkills()).contains("Java", "Spring Boot");
        
        // WebClient 호출 검증
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("https://api.openai.com/v1/chat/completions");
    }

    @Test
    @DisplayName("학습 경로 생성 성공 테스트")
    void generateLearningPath_Success() throws Exception {
        // Given
        String mockApiResponse = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\\"recommendations\\": [], \\"targetJobRole\\": \\"백엔드 개발자\\", \\"currentTechSkills\\": [\\"Java\\"], \\"overallAssessment\\": \\"좋습니다\\"}"
                  }
                }
              ]
            }
            """;

        LearningPathResponse expectedResponse = new LearningPathResponse();
        expectedResponse.setTargetJobRole("백엔드 개발자");
        expectedResponse.setOverallAssessment("좋습니다");

        // WebClient 모킹
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockApiResponse));

        // ObjectMapper 모킹
        when(objectMapper.readValue(eq(mockApiResponse), eq(java.util.Map.class)))
            .thenReturn(createMockResponseMap());
        when(objectMapper.readValue(anyString(), eq(LearningPathResponse.class)))
            .thenReturn(expectedResponse);

        // When
        LearningPathResponse result = careerCoachService.generateLearningPath(testRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTargetJobRole()).isEqualTo("백엔드 개발자");
        assertThat(result.getOverallAssessment()).isEqualTo("좋습니다");
    }

    @Test
    @DisplayName("OpenAI API 호출 실패 시 예외 발생")
    void generateInterviewQuestions_ApiFailure_ThrowsException() {
        // Given
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
            .thenReturn(Mono.error(new RuntimeException("API 호출 실패")));

        // When & Then
        assertThatThrownBy(() -> careerCoachService.generateInterviewQuestions(testRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("AI 서비스 호출에 실패했습니다");
    }

    @Test
    @DisplayName("JSON 파싱 실패 시 예외 발생")
    void generateInterviewQuestions_JsonParsingFailure_ThrowsException() throws Exception {
        // Given
        String mockApiResponse = "invalid json";

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(mockApiResponse));

        when(objectMapper.readValue(eq(mockApiResponse), eq(java.util.Map.class)))
            .thenThrow(new RuntimeException("JSON 파싱 실패"));

        // When & Then
        assertThatThrownBy(() -> careerCoachService.generateInterviewQuestions(testRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("AI 서비스 호출에 실패했습니다");
    }

    // Helper method
    private java.util.Map<String, Object> createMockResponseMap() {
        return java.util.Map.of(
            "choices", List.of(
                java.util.Map.of(
                    "message", java.util.Map.of(
                        "content", "test content"
                    )
                )
            )
        );
    }
}