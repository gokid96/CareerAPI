// StreamingOrchestrator.java - 새 파일 생성
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class StreamingOrchestrator {
    
    private final CareerCoachService careerCoachService;
    private final SseEventSender eventSender;
    private final SseSessionManager sessionManager;
    
    public void processCareerCoaching(SseEmitter emitter, String sessionId, ResumeInfoRequest request) {
        sessionManager.updateSessionStatus(sessionId, "PROCESSING");
        
        try {
            // 작업 시작 알림
            eventSender.sendProcessingStart(emitter);
            
            // 병렬 작업 실행
            CompletableFuture<InterviewQuestionsResponse> interviewFuture = 
                processInterviewQuestions(emitter, sessionId, request);
                
            CompletableFuture<LearningPathResponse> learningFuture = 
                processLearningPath(emitter, sessionId, request);
            
            // 모든 작업 완료 처리
            CompletableFuture.allOf(interviewFuture, learningFuture)
                .thenRun(() -> handleCompletion(emitter, sessionId))
                .exceptionally(throwable -> handleError(emitter, sessionId, throwable));
                
        } catch (Exception e) {
            log.error("스트리밍 처리 실패 - sessionId: {}", sessionId, e);
            handleError(emitter, sessionId, e);
        }
    }
    
    private CompletableFuture<InterviewQuestionsResponse> processInterviewQuestions(
            SseEmitter emitter, String sessionId, ResumeInfoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                eventSender.sendInterviewStart(emitter);
                
                InterviewQuestionsResponse result = 
                    careerCoachService.generateInterviewQuestions(request);
                
                eventSender.sendInterviewComplete(emitter, result);
                log.info("면접 질문 완료 - sessionId: {}", sessionId);
                
                return result;
                
            } catch (Exception e) {
                log.error("면접 질문 실패 - sessionId: {}", sessionId, e);
                throw new RuntimeException("면접 질문 생성 중 오류", e);
            }
        });
    }
    
    private CompletableFuture<LearningPathResponse> processLearningPath(
            SseEmitter emitter, String sessionId, ResumeInfoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                eventSender.sendLearningStart(emitter);
                
                LearningPathResponse result = 
                    careerCoachService.generateLearningPath(request);
                
                eventSender.sendLearningComplete(emitter, result);
                log.info("학습 경로 완료 - sessionId: {}", sessionId);
                
                return result;
                
            } catch (Exception e) {
                log.error("학습 경로 실패 - sessionId: {}", sessionId, e);
                throw new RuntimeException("학습 경로 생성 중 오류", e);
            }
        });
    }
    
    private void handleCompletion(SseEmitter emitter, String sessionId) {
        try {
            eventSender.sendCompleted(emitter);
            sessionManager.updateSessionStatus(sessionId, "COMPLETED");
            emitter.complete();
            log.info("스트리밍 전체 완료 - sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("완료 처리 실패 - sessionId: {}", sessionId, e);
            emitter.completeWithError(e);
        }
    }
    
    private Void handleError(SseEmitter emitter, String sessionId, Throwable throwable) {
        log.error("스트리밍 작업 실패 - sessionId: {}", sessionId, throwable);
        try {
            eventSender.sendError(emitter, "작업 처리 중 오류가 발생했습니다", throwable.getMessage());
            sessionManager.updateSessionStatus(sessionId, "ERROR");
        } catch (Exception e) {
            log.error("오류 메시지 전송 실패 - sessionId: {}", sessionId, e);
        }
        emitter.completeWithError(throwable);
        return null;
    }
}