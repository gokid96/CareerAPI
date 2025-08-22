package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.ApiResponse;
import com.careercoach.careercoachapi.service.CareerCoachService;
import com.careercoach.careercoachapi.service.SseEventSender;
import com.careercoach.careercoachapi.service.SseSessionManager;
import com.careercoach.careercoachapi.service.StreamingOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/career-coach")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CareerCoachController {

    // 상수 정의
    private static final long SSE_TIMEOUT_MS = 120_000L;  // 2분
    private static final String SESSION_ID_PREFIX = "stream-";

    private final SseSessionManager sessionManager;
    private final StreamingOrchestrator streamingOrchestrator;
    private final SseEventSender eventSender;

    /**
     * 스트리밍 커리어 코칭 API - 실시간으로 진행상황과 결과 전송
     */
    @PostMapping(value = "/career-coaching/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCareerCoaching(@Valid @RequestBody ResumeInfoRequest request) {
        log.info("스트리밍 API 요청 - 직무: {}", request.getJobRole());

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        String sessionId = generateSessionId();

        try {
            // 세션 생성
            sessionManager.createSession(sessionId, emitter);

            // 연결 성공 알림
            eventSender.sendConnected(emitter, sessionId);

            // 병렬 스트리밍 작업 시작
            streamingOrchestrator.processCareerCoaching(emitter, sessionId, request);

        } catch (Exception e) {
            log.error("스트리밍 초기화 실패 - sessionId: {}", sessionId, e);
            handleInitializationError(emitter, sessionId, e);
        }

        return emitter;
    }

    /**
     * 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "status", "OK",
                        "timestamp", System.currentTimeMillis(),
                        "activeStreams", sessionManager.getActiveSessionCount(),
                        "memory", getMemoryInfo()
                ),
                "Career Coach API가 정상적으로 작동중입니다."
        ));
    }

    // === Private Helper Methods ===

    private String generateSessionId() {
        return SESSION_ID_PREFIX + System.currentTimeMillis();
    }

    private void handleInitializationError(SseEmitter emitter, String sessionId, Exception e) {
        sessionManager.removeSession(sessionId);
        emitter.completeWithError(e);
    }

    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "totalMemory", runtime.totalMemory(),
                "freeMemory", runtime.freeMemory(),
                "usedMemory", runtime.totalMemory() - runtime.freeMemory()
        );
    }
}