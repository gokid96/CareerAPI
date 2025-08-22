// ModernCareerCoachControllerTest.java - WebTestClient 사용
package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.service.SseEventSender;
import com.careercoach.careercoachapi.service.SseSessionManager;
import com.careercoach.careercoachapi.service.StreamingOrchestrator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Modern CareerCoachController 테스트")
class ModernCareerCoachControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private StreamingOrchestrator streamingOrchestrator;

    @Autowired
    private SseEventSender eventSender;

    @Autowired
    private SseSessionManager sessionManager;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public StreamingOrchestrator mockStreamingOrchestrator() {
            return Mockito.mock(StreamingOrchestrator.class);
        }

        @Bean
        @Primary
        public SseEventSender mockSseEventSender() {
            return Mockito.mock(SseEventSender.class);
        }

        @Bean
        @Primary
        public SseSessionManager mockSseSessionManager() {
            SseSessionManager mock = Mockito.mock(SseSessionManager.class);
            when(mock.getActiveSessionCount()).thenReturn(5L);
            return mock;
        }

        @Bean
        @Primary
        public WebTestClient webTestClient() {
            return WebTestClient.bindToServer()
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        }
    }

    @Test
    @DisplayName("헬스 체크 API - WebTestClient")
    void healthCheck_WithWebTestClient() {
        webTestClient.get()
                .uri("/api/v1/career-coach/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.status").isEqualTo("OK")
                .jsonPath("$.data.activeStreams").isEqualTo(5)
                .jsonPath("$.data.timestamp").exists()
                .jsonPath("$.data.memory").exists()
                .jsonPath("$.data.memory.totalMemory").exists()
                .jsonPath("$.data.memory.freeMemory").exists()
                .jsonPath("$.data.memory.usedMemory").exists()
                .jsonPath("$.message").isEqualTo("Career Coach API가 정상적으로 작동중입니다.");

        verify(sessionManager).getActiveSessionCount();
    }

    @Test
    @DisplayName("스트리밍 API - 유효한 요청")
    void streamCareerCoaching_ValidRequest() {
        // Given
        ResumeInfoRequest request = new ResumeInfoRequest(
                "3년차 백엔드 개발자, Spring Boot 기반 서비스 개발",
                "백엔드 개발자",
                List.of("Java", "Spring Boot", "MySQL")
        );

        // When & Then
        webTestClient.post()
                .uri("/api/v1/career-coach/career-coaching/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM);

        // 비동기 작업 검증
        verify(sessionManager, timeout(2000)).createSession(anyString(), any());
        verify(eventSender, timeout(2000)).sendConnected(any(), anyString());
        verify(streamingOrchestrator, timeout(2000)).processCareerCoaching(any(), anyString(), eq(request));
    }

    @Test
    @DisplayName("스트리밍 API - 잘못된 요청 데이터")
    void streamCareerCoaching_InvalidRequest() {
        // Given - 필수 필드 누락
        ResumeInfoRequest invalidRequest = new ResumeInfoRequest();
        invalidRequest.setJobRole(""); // 빈 문자열
        invalidRequest.setCareerSummary(null); // null
        invalidRequest.setTechSkills(List.of()); // 빈 리스트

        // When & Then
        webTestClient.post()
                .uri("/api/v1/career-coach/career-coaching/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(400)
                .jsonPath("$.message").exists();

        // 비즈니스 로직이 호출되지 않았는지 확인
        verify(streamingOrchestrator, never()).processCareerCoaching(any(), any(), any());
    }

    @Test
    @DisplayName("스트리밍 API - 잘못된 Content-Type")
    void streamCareerCoaching_InvalidContentType() {
        webTestClient.post()
                .uri("/api/v1/career-coach/career-coaching/stream")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("invalid request")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }

    @Test
    @DisplayName("스트리밍 API - 잘못된 JSON 형식")
    void streamCareerCoaching_InvalidJson() {
        webTestClient.post()
                .uri("/api/v1/career-coach/career-coaching/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ invalid json }")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트")
    void nonExistentEndpoint_ReturnsNotFound() {
        webTestClient.get()
                .uri("/api/v1/career-coach/non-existent")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드")
    void unsupportedHttpMethod_ReturnsMethodNotAllowed() {
        webTestClient.delete()
                .uri("/api/v1/career-coach/health")
                .exchange()
                .expectStatus().isEqualTo(405); // Method Not Allowed
    }

    @Test
    @DisplayName("CORS 헤더 확인")
    void corsHeaders_ArePresent() {
        webTestClient.options()
                .uri("/api/v1/career-coach/health")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "*");
    }
}