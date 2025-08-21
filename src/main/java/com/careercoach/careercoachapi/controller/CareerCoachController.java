// CareerCoachController.java - 개선된 예외처리
package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.ApiResponse;
import com.careercoach.careercoachapi.dto.response.ComprehensiveCareerResponse;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import com.careercoach.careercoachapi.service.CareerCoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("/api/v1/career-coach")
@CrossOrigin(origins = "*") // 개발용 - 운영 시 특정 도메인으로 제한
@RequiredArgsConstructor  // @Autowired 대신 생성자 주입
public class CareerCoachController {

    private final CareerCoachService careerCoachService;

    @PostMapping("/career-coaching")
    public ResponseEntity<ApiResponse<ComprehensiveCareerResponse>> generateCareerCoaching(
            @Valid @RequestBody ResumeInfoRequest request) {

        log.info("커리어 코칭 생성 요청 - 직무: {}", request.getJobRole());

        CompletableFuture<InterviewQuestionsResponse> interviewQuestionsFuture =
                CompletableFuture.supplyAsync(() ->
                        careerCoachService.generateInterviewQuestions(request));

        CompletableFuture<LearningPathResponse> learningPathFuture =
                CompletableFuture.supplyAsync(() ->
                        careerCoachService.generateLearningPath(request));

        CompletableFuture<ComprehensiveCareerResponse> combinedFuture =
                interviewQuestionsFuture.thenCombine(learningPathFuture,
                        (interviewQuestions, learningPath) ->
                                ComprehensiveCareerResponse.builder()
                                        .interviewQuestions(interviewQuestions)
                                        .learningPath(learningPath)
                                        .build());

        try {
            // checked exception -> try-catch
            ComprehensiveCareerResponse response = combinedFuture.get(45, TimeUnit.SECONDS);

            log.info("커리어 코칭 생성 성공 - 직무: {}", request.getJobRole());

            return ResponseEntity.ok(
                    ApiResponse.success(response, "커리어 코칭 정보가 성공적으로 생성되었습니다.")
            );

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // checked exception을 RuntimeException으로 변환하여 GlobalExceptionHandler로 전달
            log.error("비동기 작업 처리 중 오류 - 직무: {}", request.getJobRole(), e);

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                throw new RuntimeException("요청 처리가 중단되었습니다.", e);
            } else if (e instanceof TimeoutException) {
                throw new RuntimeException("요청 처리 시간이 초과되었습니다.", e);
            } else {
                throw new RuntimeException("비동기 작업 처리 중 오류가 발생했습니다.", e);
            }
        }
    }

    /**
     * API 상태 확인
     * GET /api/v1/career-coach/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("헬스체크 요청");
        return ResponseEntity.ok(
                ApiResponse.success("OK", "Career Coach API가 정상적으로 작동중입니다.")
        );
    }
}
