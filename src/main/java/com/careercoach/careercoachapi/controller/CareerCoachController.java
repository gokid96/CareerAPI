// CareerCoachController.java
package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.ApiResponse;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.entity.ResumeInfo;
import com.careercoach.careercoachapi.service.CareerCoachService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/career-coach")
@CrossOrigin(origins = "*") // 개발용 - 운영 시 특정 도메인으로 제한
public class CareerCoachController {

    @Autowired
    private CareerCoachService careerCoachService;

    /**
     * 이력서 정보 기반 맞춤형 면접 질문 생성
     * POST /api/v1/career-coach/interview-questions
     */
    @PostMapping("/interview-questions")
    public ResponseEntity<ApiResponse<InterviewQuestionsResponse>> generateInterviewQuestions(
            @Valid @RequestBody ResumeInfoRequest request) {

        try {
            InterviewQuestionsResponse response = careerCoachService.generateInterviewQuestions(request);

            return ResponseEntity.ok(
                    ApiResponse.success(response, "면접 질문이 성공적으로 생성되었습니다.")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("면접 질문 생성 중 오류가 발생했습니다: " + e.getMessage(), 500));
        }
    }

    /**
     * 이력서 정보 기반 개인 맞춤형 학습 경로 추천
     * POST /api/v1/career-coach/learning-path
     */
    @PostMapping("/learning-path")
    public ResponseEntity<ApiResponse<LearningPathResponse>> generateLearningPath(
            @Valid @RequestBody ResumeInfoRequest request) {

        try {
            LearningPathResponse response = careerCoachService.generateLearningPath(request);

            return ResponseEntity.ok(
                    ApiResponse.success(response, "개인 맞춤형 학습 경로가 성공적으로 생성되었습니다.")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("학습 경로 추천 생성 중 오류가 발생했습니다: " + e.getMessage(), 500));
        }
    }

    /**
     * 저장된 이력서 정보 단건 조회
     * GET /api/v1/career-coach/resume/{id}
     */
    @GetMapping("/resume/{id}")
    public ResponseEntity<ApiResponse<ResumeInfo>> getResumeInfo(@PathVariable Long id) {

        try {
            Optional<ResumeInfo> resumeInfo = careerCoachService.getResumeInfo(id);

            if (resumeInfo.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.success(resumeInfo.get(), "이력서 정보를 조회했습니다.")
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("해당 이력서 정보를 찾을 수 없습니다.", 404));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("이력서 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), 500));
        }
    }

    /**
     * 저장된 모든 이력서 정보 조회
     * GET /api/v1/career-coach/resume
     */
    @GetMapping("/resume")
    public ResponseEntity<ApiResponse<List<ResumeInfo>>> getAllResumeInfo() {

        try {
            List<ResumeInfo> resumeInfoList = careerCoachService.getAllResumeInfo();

            return ResponseEntity.ok(
                    ApiResponse.success(resumeInfoList, "모든 이력서 정보를 조회했습니다.")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("이력서 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), 500));
        }
    }

    /**
     * 직무별 이력서 정보 조회
     * GET /api/v1/career-coach/resume/job-role?role={jobRole}
     */
    @GetMapping("/resume/job-role")
    public ResponseEntity<ApiResponse<List<ResumeInfo>>> getResumeInfoByJobRole(
            @RequestParam String role) {

        try {
            List<ResumeInfo> resumeInfoList = careerCoachService.getResumeInfoByJobRole(role);

            return ResponseEntity.ok(
                    ApiResponse.success(resumeInfoList, "직무별 이력서 정보를 조회했습니다.")
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("이력서 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), 500));
        }
    }

    /**
     * 최근 등록된 이력서 정보 조회
     * GET /api/v1/career-coach/resume/latest
     */
    @GetMapping("/resume/latest")
    public ResponseEntity<ApiResponse<ResumeInfo>> getLatestResumeInfo() {

        try {
            Optional<ResumeInfo> resumeInfo = careerCoachService.getLatestResumeInfo();

            if (resumeInfo.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.success(resumeInfo.get(), "최근 이력서 정보를 조회했습니다.")
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("등록된 이력서 정보가 없습니다.", 404));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("이력서 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), 500));
        }
    }

    /**
     * API 상태 확인
     * GET /api/v1/career-coach/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("OK", "Career Coach API가 정상적으로 작동중입니다.")
        );
    }
}