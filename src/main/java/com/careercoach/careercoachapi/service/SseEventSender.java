// SseEventSender.java - 새 파일 생성
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class SseEventSender {
    
    private static final long SSE_RECONNECT_TIME_MS = 1_000L;
    
    public void sendConnected(SseEmitter emitter, String sessionId) {
        sendEvent(emitter, "connected", Map.of(
            "sessionId", sessionId,
            "message", "스트리밍 연결이 설정되었습니다."
        ));
    }
    
    public void sendProcessingStart(SseEmitter emitter) {
        sendEvent(emitter, "processing_start", Map.of(
            "message", "면접 질문과 학습 경로를 동시에 생성중입니다...",
            "progress", 0
        ));
    }
    
    public void sendInterviewStart(SseEmitter emitter) {
        sendEvent(emitter, "interview_start", Map.of(
            "message", "면접 질문 생성 중...",
            "progress", 10
        ));
    }
    
    public void sendInterviewComplete(SseEmitter emitter, InterviewQuestionsResponse data) {
        sendEvent(emitter, "interview_complete", Map.of(
            "data", data,
            "message", "면접 질문 생성 완료",
            "progress", 50
        ));
    }
    
    public void sendLearningStart(SseEmitter emitter) {
        sendEvent(emitter, "learning_start", Map.of(
            "message", "학습 경로 생성 중...", 
            "progress", 10
        ));
    }
    
    public void sendLearningComplete(SseEmitter emitter, LearningPathResponse data) {
        sendEvent(emitter, "learning_complete", Map.of(
            "data", data,
            "message", "학습 경로 생성 완료",
            "progress", 50
        ));
    }
    
    public void sendCompleted(SseEmitter emitter) {
        sendEvent(emitter, "completed", Map.of(
            "message", "모든 작업이 완료되었습니다",
            "progress", 100
        ));
    }
    
    public void sendError(SseEmitter emitter, String message, String error) {
        sendEvent(emitter, "error", Map.of(
            "message", message,
            "error", error != null ? error : "알 수 없는 오류"
        ));
    }
    
    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                .name(eventName)
                .data(data)
                .id(String.valueOf(System.currentTimeMillis()))
                .reconnectTime(SSE_RECONNECT_TIME_MS));
        } catch (IOException e) {
            log.error("SSE 이벤트 전송 실패: {}", eventName, e);
            throw new RuntimeException("이벤트 전송 실패: " + eventName, e);
        }
    }
}