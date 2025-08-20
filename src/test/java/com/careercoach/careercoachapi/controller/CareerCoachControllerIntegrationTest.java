// CareerCoachControllerIntegrationTest.java
package com.careercoach.careercoachapi.controller;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")

class CareerCoachControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    void testGenerateLearningPath_Integration() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Given
        List<String> techSkills = Arrays.asList("Spring Boot", "Java", "Python", "AWS", "MySQL");
        ResumeInfoRequest request = new ResumeInfoRequest(
                "3년차 백엔드 개발자, Spring Boot 기반 웹 서비스 개발 경험",
                "백엔드 개발자",
                techSkills
        );

        // When & Then
        mockMvc.perform(post("/api/v1/career-coach/learning-path")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("개인 맞춤형 학습 경로가 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.recommendations").isArray())
                .andExpect(jsonPath("$.data.targetJobRole").value("백엔드 개발자"))
                .andExpect(jsonPath("$.data.currentTechSkills").isArray())
                .andExpect(jsonPath("$.data.overallAssessment").isString())
                .andExpect(jsonPath("$.data.generatedAt").exists());
    }
}