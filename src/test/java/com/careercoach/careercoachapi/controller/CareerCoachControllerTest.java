package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.controller.CareerCoachController;
import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.service.CareerCoachService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CareerCoachController.class)
class CareerCoachControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareerCoachService careerCoachService;

    @Autowired
    private ObjectMapper objectMapper;

    private ResumeInfoRequest sampleRequest;
    private InterviewQuestionsResponse sampleResponse;

    @BeforeEach
    void setUp() {
        // 테스트용 샘플 데이터 준비
        List<String> techSkills = Arrays.asList("Spring Boot", "Java", "MySQL", "AWS");
        sampleRequest = new ResumeInfoRequest(
                "3년차 백엔드 개발자, Spring Boot 기반 웹 서비스 개발 경험",
                "백엔드 개발자",
                techSkills
        );

        List<String> questions = Arrays.asList(
                "Spring Boot를 사용한 프로젝트에서 가장 어려웠던 기술적 문제와 해결 방법을 설명해주세요.",
                "3년간의 백엔드 개발 경험 중 가장 성과가 있었던 프로젝트는 무엇인가요?",
                "AWS 환경에서 서비스 운영 시 고려해야 할 사항들을 설명해주세요.",
                "팀 프로젝트에서 다른 개발자와의 협업 경험을 구체적으로 말씀해주세요.",
                "현재 보유한 기술 스택 외에 추가로 학습하고 싶은 기술이 있다면 무엇인가요?"
        );

        sampleResponse = new InterviewQuestionsResponse(questions, "백엔드 개발자", techSkills);
    }

    @Test
    void testGenerateInterviewQuestions_Success() throws Exception {
        // Given
        when(careerCoachService.generateInterviewQuestions(any(ResumeInfoRequest.class)))
                .thenReturn(sampleResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/career-coach/interview-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("면접 질문이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andExpect(jsonPath("$.data.questions").isNotEmpty())
                .andExpect(jsonPath("$.data.targetJobRole").value("백엔드 개발자"));
    }

    @Test
    void testGenerateInterviewQuestions_ValidationError() throws Exception {
        // Given - 잘못된 요청 데이터 (필수 필드 누락)
        ResumeInfoRequest invalidRequest = new ResumeInfoRequest();
        invalidRequest.setCareerSummary(""); // 빈 값
        invalidRequest.setJobRole(""); // 빈 값
        invalidRequest.setTechSkills(Arrays.asList()); // 빈 리스트

        // When & Then
        mockMvc.perform(post("/api/v1/career-coach/interview-questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/career-coach/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("OK"))
                .andExpect(jsonPath("$.message").value("Career Coach API가 정상적으로 작동중입니다."));
    }
}