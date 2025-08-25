package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;

/**
 * 스트리밍 처리를 조정하는 서비스 클래스
 * 여러 비동기 작업을 조율하고 SSE를 통해 실시간으로 결과를 전송
 */
@Service  // 스프링 서비스 컴포넌트로 선언
@Slf4j    // Lombok을 사용한 로깅 기능 활성화
@RequiredArgsConstructor  // 필수 필드에 대한 생성자 자동 생성
public class StreamingOrchestrator {
    
    // 의존성 주입
    private final CareerCoachService careerCoachService;  // 커리어 코칭 핵심 서비스
    private final SseEventSender eventSender;            // SSE 이벤트 발신자
    private final SseSessionManager sessionManager;      // SSE 세션 관리자
    
    /**
     * 커리어 코칭 프로세스를 실행하고 실시간으로 결과를 스트리밍
     * 면접 질문과 학습 경로를 병렬로 처리
     */
    public void processCareerCoaching(SseEmitter emitter, String sessionId, ResumeInfoRequest request) {
        // 세션 상태를 처리 중으로 업데이트
        sessionManager.updateSessionStatus(sessionId, "PROCESSING");
        
        try {
            // 클라이언트에 처리 시작 알림
            eventSender.sendProcessingStart(emitter);
            
            // 면접 질문과 학습 경로 생성을 병렬로 실행
            CompletableFuture<InterviewQuestionsResponse> interviewFuture = 
                processInterviewQuestions(emitter, sessionId, request);
                
            CompletableFuture<LearningPathResponse> learningFuture = 
                processLearningPath(emitter, sessionId, request);
            
            // 모든 비동기 작업이 완료되면 처리
            CompletableFuture.allOf(interviewFuture, learningFuture)
                .thenRun(() -> handleCompletion(emitter, sessionId))
                .exceptionally(throwable -> handleError(emitter, sessionId, throwable));
                
        } catch (Exception e) {
            log.error("스트리밍 처리 실패 - sessionId: {}", sessionId, e);
            handleError(emitter, sessionId, e);
        }
    }
    
    /**
     * 면접 질문 생성을 비동기적으로 처리
     */
    private CompletableFuture<InterviewQuestionsResponse> processInterviewQuestions(
            SseEmitter emitter, String sessionId, ResumeInfoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 면접 질문 생성 시작 알림
                eventSender.sendInterviewStart(emitter);
                
                // 면접 질문 생성
                InterviewQuestionsResponse result = 
                    careerCoachService.generateInterviewQuestions(request);
                
                // 결과 전송 및 로깅
                eventSender.sendInterviewComplete(emitter, result);
                log.info("면접 질문 완료 - sessionId: {}", sessionId);
                
                return result;
                
            } catch (Exception e) {
                log.error("면접 질문 실패 - sessionId: {}", sessionId, e);
                throw new RuntimeException("면접 질문 생성 중 오류", e);
            }
        });
    }
    
    /**
     * 학습 경로 생성을 비동기적으로 처리
     */
    private CompletableFuture<LearningPathResponse> processLearningPath(
            SseEmitter emitter, String sessionId, ResumeInfoRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 학습 경로 생성 시작 알림
                eventSender.sendLearningStart(emitter);
                
                // 학습 경로 생성
                LearningPathResponse result = 
                    careerCoachService.generateLearningPath(request);
                
                // 결과 전송 및 로깅
                eventSender.sendLearningComplete(emitter, result);
                log.info("학습 경로 완료 - sessionId: {}", sessionId);
                
                return result;
                
            } catch (Exception e) {
                log.error("학습 경로 실패 - sessionId: {}", sessionId, e);
                throw new RuntimeException("학습 경로 생성 중 오류", e);
            }
        });
    }
    
    /**
     * 모든 작업이 성공적으로 완료되었을 때의 처리
     */
    private void handleCompletion(SseEmitter emitter, String sessionId) {
        try {
            // 완료 이벤트 전송 및 세션 상태 업데이트
            eventSender.sendCompleted(emitter);
            sessionManager.updateSessionStatus(sessionId, "COMPLETED");
            emitter.complete();
            log.info("스트리밍 전체 완료 - sessionId: {}", sessionId);
        } catch (Exception e) {
            log.error("완료 처리 실패 - sessionId: {}", sessionId, e);
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 오류 발생 시의 처리
     */
    private Void handleError(SseEmitter emitter, String sessionId, Throwable throwable) {
        log.error("스트리밍 작업 실패 - sessionId: {}", sessionId, throwable);
        try {
            // 오류 메시지 전송 및 세션 상태 업데이트
            eventSender.sendError(emitter, "작업 처리 중 오류가 발생했습니다", throwable.getMessage());
            sessionManager.updateSessionStatus(sessionId, "ERROR");
        } catch (Exception e) {
            log.error("오류 메시지 전송 실패 - sessionId: {}", sessionId, e);
        }
        emitter.completeWithError(throwable);
        return null;
    }
}