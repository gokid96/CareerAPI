// CareerCoachControllerTest.java - 최신 방식
package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.service.SseEventSender;
import com.careercoach.careercoachapi.service.SseSessionManager;
import com.careercoach.careercoachapi.service.StreamingOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("CareerCoachController 통합 테스트")
class CareerCoachControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    // Mock 객체들은 TestConfiguration에서 Bean으로 등록
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
            when(mock.getActiveSessionCount()).thenReturn(3L);
            return mock;
        }
    }

    @Test
    @DisplayName("헬스 체크 API 테스트")
    void healthCheck_ReturnsOk() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/career-coach/health",
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> body = response.getBody();
        assertThat(body.get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertThat(data.get("status")).isEqualTo("OK");
        assertThat(data.get("activeStreams")).isEqualTo(3);
        assertThat(data.get("timestamp")).isNotNull();
        assertThat(data.get("memory")).isNotNull();

        verify(sessionManager).getActiveSessionCount();
    }

    @Test
    @DisplayName("스트리밍 API - 잘못된 요청 데이터 검증")
    void streamCareerCoaching_InvalidRequest_ReturnsBadRequest() {
        // Given - 필수 필드 누락
        ResumeInfoRequest invalidRequest = new ResumeInfoRequest();
        invalidRequest.setJobRole(""); // 빈 문자열
        invalidRequest.setCareerSummary(null); // null
        invalidRequest.setTechSkills(List.of()); // 빈 리스트

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ResumeInfoRequest> requestEntity = new HttpEntity<>(invalidRequest, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/career-coach/career-coaching/stream",
                requestEntity,
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        Map<String, Object> body = response.getBody();
        assertThat(body.get("success")).isEqualTo(false);
        assertThat(body.get("errorCode")).isEqualTo(400);

        // 비즈니스 로직이 호출되지 않았는지 확인
        verify(streamingOrchestrator, never()).processCareerCoaching(any(), any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 엔드포인트 호출")
    void nonExistentEndpoint_ReturnsNotFound() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/career-coach/non-existent",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드")
    void unsupportedHttpMethod_ReturnsMethodNotAllowed() {
        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/career-coach/health",
                HttpMethod.DELETE,
                null,
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }
}