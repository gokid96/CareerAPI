// SseSessionManager.java - Redis 기반 세션 관리
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.SessionInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseSessionManager {

    // 모든 세션 데이터를 메모리에서 관리
    private final ConcurrentHashMap<String, SseSessionData> sessions = new ConcurrentHashMap<>();

    /**
     * 세션 데이터 클래스
     */
    @Getter
    public static class SseSessionData {
        // Getters
        private final String sessionId;
        private final SseEmitter emitter;
        private final long createdAt;
        private volatile String status;
        private volatile long updatedAt;

        public SseSessionData(String sessionId, SseEmitter emitter) {
            this.sessionId = sessionId;
            this.emitter = emitter;
            this.createdAt = System.currentTimeMillis();
            this.status = "CONNECTED";
            this.updatedAt = this.createdAt;
        }

        // Status update
        public void updateStatus(String newStatus) {
            this.status = newStatus;
            this.updatedAt = System.currentTimeMillis();
        }

        // 세션 만료 확인 (5분)
        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 300_000L;
        }
    }

    /**
     * 세션 생성
     */
    public void createSession(String sessionId, SseEmitter emitter) {
        try {
            // 세션 데이터 생성
            SseSessionData sessionData = new SseSessionData(sessionId, emitter);
            sessions.put(sessionId, sessionData);

            // Emitter 이벤트 핸들러 설정
            setupEmitterHandlers(sessionId, emitter);

            log.info("SSE 세션 생성 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("SSE 세션 생성 실패 - sessionId: {}", sessionId, e);
            removeSession(sessionId);
            throw new RuntimeException("세션 생성에 실패했습니다.", e);
        }
    }

    /**
     * 세션에서 emitter 조회
     */
    public SseEmitter getEmitter(String sessionId) {
        SseSessionData sessionData = sessions.get(sessionId);

        if (sessionData == null || sessionData.isExpired()) {
            log.warn("유효하지 않은 세션 - sessionId: {}", sessionId);
            if (sessionData != null) {
                removeSession(sessionId); // 만료된 세션 정리
            }
            return null;
        }

        return sessionData.getEmitter();
    }

    /**
     * 세션 유효성 확인
     */
    public boolean isSessionValid(String sessionId) {
        SseSessionData sessionData = sessions.get(sessionId);
        return sessionData != null && !sessionData.isExpired();
    }

    /**
     * 세션 상태 업데이트
     */
    public void updateSessionStatus(String sessionId, String status) {
        try {
            SseSessionData sessionData = sessions.get(sessionId);
            if (sessionData != null) {
                sessionData.updateStatus(status);
                log.info("세션 상태 업데이트 - sessionId: {}, status: {}", sessionId, status);
            }
        } catch (Exception e) {
            log.error("세션 상태 업데이트 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * 세션 제거
     */
    public void removeSession(String sessionId) {
        try {
            SseSessionData sessionData = sessions.remove(sessionId);

            if (sessionData != null) {
                try {
                    SseEmitter emitter = sessionData.getEmitter();
                    if (emitter != null) {
                        emitter.complete();
                    }
                } catch (Exception e) {
                    log.warn("Emitter 정리 중 오류 - sessionId: {}", sessionId, e);
                }
            }

            log.info("세션 정리 완료 - sessionId: {}", sessionId);

        } catch (Exception e) {
            log.error("세션 정리 실패 - sessionId: {}", sessionId, e);
        }
    }

    /**
     * 세션 정보 조회
     */
    public SessionInfo getSessionInfo(String sessionId) {
        SseSessionData sessionData = sessions.get(sessionId);

        if (sessionData == null) {
            return null;
        }

        return SessionInfo.builder()
                .sessionId(sessionData.getSessionId())
                .status(sessionData.getStatus())
                .createdAt(sessionData.getCreatedAt())
                .updatedAt(sessionData.getUpdatedAt())
                .build();
    }

    /**
     * 활성 세션 수 조회
     */
    public long getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * 만료된 세션 자동 정리 (1분마다 실행)
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        long beforeCount = sessions.size();

        sessions.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                log.debug("만료된 세션 정리 - sessionId: {}", entry.getKey());
                try {
                    SseEmitter emitter = entry.getValue().getEmitter();
                    if (emitter != null) {
                        emitter.complete();
                    }
                } catch (Exception e) {
                    log.warn("만료 세션 정리 중 오류 - sessionId: {}", entry.getKey(), e);
                }
                return true;
            }
            return false;
        });

        long afterCount = sessions.size();
        if (beforeCount != afterCount) {
            log.info("세션 정리 완료 - 정리 전: {}, 정리 후: {}", beforeCount, afterCount);
        }
    }

    /**
     * Emitter 이벤트 핸들러 설정
     */
    private void setupEmitterHandlers(String sessionId, SseEmitter emitter) {
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료 - sessionId: {}", sessionId);
            updateSessionStatus(sessionId, "COMPLETED");
            removeSession(sessionId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃 - sessionId: {}", sessionId);
            updateSessionStatus(sessionId, "TIMEOUT");
            removeSession(sessionId);
        });

        emitter.onError((throwable) -> {
            log.error("SSE 연결 오류 - sessionId: {}", sessionId, throwable);
            updateSessionStatus(sessionId, "ERROR");
            removeSession(sessionId);
        });
    }

    /**
     * 전체 세션 상태 조회 (모니터링용)
     */
    public void logSessionStatus() {
        log.info("현재 활성 세션 수: {}", sessions.size());
        sessions.forEach((sessionId, sessionData) -> {
            log.debug("세션 - ID: {}, 상태: {}, 생성시간: {}",
                    sessionId,
                    sessionData.getStatus(),
                    sessionData.getCreatedAt());
        });
    }
}