package com.careercoach.careercoachapi.controller;

// 필요한 의존성 import
import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.ApiResponse;
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

/**
 * 경력 코칭 관련 API 엔드포인트를 제공하는 컨트롤러
 * 이 컨트롤러는 경력 분석 및 코칭 관련 요청을 처리합니다.
 */
@Slf4j  // Lombok을 사용한 로깅 기능 활성화
@RestController  // REST API 컨트롤러 선언
@RequestMapping("/api/v1/career-coach")  // 기본 URL 경로 설정
@CrossOrigin(origins = "*")  // CORS 설정 - 모든 도메인에서의 접근 허용
@RequiredArgsConstructor  // 필수 필드에 대한 생성자 자동 생성 (Lombok)
public class CareerCoachController {

    // 상수 정의
    private static final long SSE_TIMEOUT_MS = 120_000L;  // SSE 연결 타임아웃 시간: 2분
    private static final String SESSION_ID_PREFIX = "stream-";  // 세션 ID 접두사

    // 의존성 주입될 서비스 컴포넌트들
    private final SseSessionManager sessionManager;        // SSE 세션 관리자
    private final StreamingOrchestrator streamingOrchestrator;  // 스트리밍 처리 조정자
    private final SseEventSender eventSender;             // SSE 이벤트 발신자

    /**
     * 스트리밍 커리어 코칭 API 엔드포인트
     * Server-Sent Events를 사용하여 실시간으로 진행상황과 결과를 전송
     */
    @PostMapping(value = "/career-coaching/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCareerCoaching(@Valid @RequestBody ResumeInfoRequest request) {
        log.info("스트리밍 API 요청 - 직무: {}", request.getJobRole());  // 로그 기록

        // SSE 이미터 생성 (2분 타임아웃)
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        String sessionId = generateSessionId();  // 고유 세션 ID 생성

        try {
            // 새로운 SSE 세션 생성
            sessionManager.createSession(sessionId, emitter);

            // 클라이언트에 연결 성공 알림
            eventSender.sendConnected(emitter, sessionId);

            // 비동기로 코칭 처리 시작
            streamingOrchestrator.processCareerCoaching(emitter, sessionId, request);

        } catch (Exception e) {
            // 오류 발생 시 로그 기록 및 오류 처리
            log.error("스트리밍 초기화 실패 - sessionId: {}", sessionId, e);
            handleInitializationError(emitter, sessionId, e);
        }

        return emitter;  // SSE 이미터 반환
    }

    /**
     * 서비스 상태 확인을 위한 헬스체크 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        // 서비스 상태 정보를 포함한 응답 생성
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

    // === 내부 헬퍼 메소드들 ===

    /**
     * 타임스탬프 기반의 고유 세션 ID 생성
     */
    private String generateSessionId() {
        return SESSION_ID_PREFIX + System.currentTimeMillis();
    }

    /**
     * 스트리밍 초기화 중 발생한 오류 처리
     */
    private void handleInitializationError(SseEmitter emitter, String sessionId, Exception e) {
        sessionManager.removeSession(sessionId);  // 세션 제거
        emitter.completeWithError(e);  // 클라이언트에 오류 전송
    }

    /**
     * JVM 메모리 사용 정보 수집
     */
    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "totalMemory", runtime.totalMemory(),     // 총 할당된 메모리
                "freeMemory", runtime.freeMemory(),       // 사용 가능한 메모리
                "usedMemory", runtime.totalMemory() - runtime.freeMemory()  // 사용 중인 메모리
        );
    }
}