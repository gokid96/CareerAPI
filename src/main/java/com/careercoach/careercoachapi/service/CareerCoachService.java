// CareerCoachService.java - 보편적인 방식
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerCoachService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    /**
     * 면접 질문 생성 - 보편적인 JSON 방식
     */
    public InterviewQuestionsResponse generateInterviewQuestions(ResumeInfoRequest request) {
        log.info("면접 질문 생성 시작 - 직무: {}", request.getJobRole());

        try {
            String prompt = createInterviewQuestionsPrompt(request);
            String jsonResponse = callOpenAiWithJson(prompt);

            InterviewQuestionsResponse response = objectMapper.readValue(jsonResponse, InterviewQuestionsResponse.class);

            // generatedAt 설정
            if (response.getGeneratedAt() == null) {
                response.setGeneratedAt(LocalDateTime.now());
            }

            log.info("면접 질문 생성 완료 - 질문 수: {}", response.getQuestions().size());
            return response;

        } catch (Exception e) {
            log.error("면접 질문 생성 실패", e);
            throw new RuntimeException("면접 질문 생성에 실패했습니다.", e);
        }
    }

    /**
     * 학습 경로 생성 - 보편적인 JSON 방식
     */
    public LearningPathResponse generateLearningPath(ResumeInfoRequest request) {
        log.info("학습 경로 생성 시작 - 직무: {}", request.getJobRole());

        try {
            String prompt = createLearningPathPrompt(request);
            String jsonResponse = callOpenAiWithJson(prompt);

            LearningPathResponse response = objectMapper.readValue(jsonResponse, LearningPathResponse.class);

            // generatedAt 설정
            if (response.getGeneratedAt() == null) {
                response.setGeneratedAt(LocalDateTime.now());
            }

            log.info("학습 경로 생성 완료 - 추천 항목: {}", response.getRecommendations().size());
            return response;

        } catch (Exception e) {
            log.error("학습 경로 생성 실패", e);
            throw new RuntimeException("학습 경로 생성에 실패했습니다.", e);
        }
    }

    /**
     * OpenAI API 호출 - JSON 모드 (보편적인 방식)
     */
    private String callOpenAiWithJson(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", Arrays.asList(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", 3000,
                    "temperature", 0.3,
                    "response_format", Map.of("type", "json_object") // JSON 모드 활성화
            );

            String result = webClient
                    .post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .headers(headers -> {
                        headers.set("Authorization", "Bearer " + apiKey);
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));

            // 응답에서 content 추출
            Map<String, Object> responseMap = objectMapper.readValue(result, Map.class);
            return (String) ((Map<String, Object>) ((Map<String, Object>)
                    ((java.util.List<?>) responseMap.get("choices")).get(0)).get("message")).get("content");

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new RuntimeException("AI 서비스 호출에 실패했습니다.", e);
        }
    }

    /**
     * 면접 질문 프롬프트 생성
     */
    private String createInterviewQuestionsPrompt(ResumeInfoRequest request) {
        return String.format("""
        당신은 전문 면접관입니다. 아래 정보를 바탕으로 실제 면접에서 나올 법한 심층적인 면접 질문을 JSON 형식으로 생성해주세요.
        
        ## 지원자 정보
        직무: %s
        경력: %s
        기술 스킬: %s
        
        ## 질문 생성 요구사항
        1. 각 질문은 지원자의 경력과 기술 스킬을 구체적으로 검증할 수 있어야 함
        2. 단순한 지식 확인이 아닌 실무 경험과 문제해결 능력을 평가하는 질문
        3. 상황 기반(STAR 기법) 답변을 유도하는 행동 중심 질문 포함
        4. 해당 직무의 핵심 역량을 평가할 수 있는 기술적 질문
        5. 협업, 커뮤니케이션, 문제해결 능력을 종합적으로 평가하는 질문
        
        다음 JSON 형식으로 정확히 응답해주세요:
        {
          "questions": [
            "실무 경험을 바탕으로 한 구체적인 면접 질문 1 (100자 이내)",
            "기술적 문제해결 능력을 평가하는 면접 질문 2 (100자 이내)",
            "협업 및 커뮤니케이션 역량을 확인하는 면접 질문 3 (100자 이내)",
            "상황 기반 행동 평가 질문 4 (100자 이내)",
            "성장 가능성과 학습 의지를 확인하는 질문 5 (100자 이내)"
          ],
          "targetJobRole": "%s",
          "techSkills": %s
        }
        
        ## 주의사항
        - 반드시 5개의 질문만 생성
        - 각 질문은 100자 이내로 작성
        - JSON 형식을 정확히 준수
        - 질문은 실제 면접에서 활용 가능한 수준으로 구체적이고 실용적으로 작성
        """,
                request.getJobRole(),
                request.getCareerSummary(),
                String.join(", ", request.getTechSkills()),
                request.getJobRole(),
                toJsonString(request.getTechSkills())
        );
    }
    /**
     * 학습 경로 프롬프트 생성
     */
    private String createLearningPathPrompt(ResumeInfoRequest request) {
        return String.format("""
        당신은 전문 커리어 코치입니다. 아래 정보를 바탕으로 개인 맞춤형 학습 경로를 JSON 형식으로 생성해주세요.
        
        ## 지원자 정보
        직무: %s
        경력: %s
        기술 스킬: %s
        
        ## 학습 경로 생성 요구사항
        1. 구직자가 향후 개발 역량을 강화하고 합격률을 높일 수 있는 개인 맞춤형 학습 경로
        2. 특정 기술 스택 심화, 관련 프로젝트 경험 쌓기, 커뮤니케이션 스킬 강화 등 구체적인 방안 포함
        3. 현재 기술 수준과 목표 직무 간의 갭 분석을 통한 우선순위 설정
        4. 실무 적용 가능한 학습 방법 및 기간 제시
        5. 각 추천 항목에 대한 명확한 근거 제시
        
        다음 JSON 형식으로 정확히 응답해주세요:
        {
          "recommendations": [
            {
              "category": "기술스킬|프로젝트경험|소프트스킬|자격증|네트워킹",
              "title": "구체적인 학습 제목",
              "description": "학습 내용과 방법에 대한 상세 설명 (300자 이내)",
              "priority": "HIGH|MEDIUM|LOW",
              "estimatedDuration": "예상 소요 기간 (예: 2-3개월, 4-6주 등)",
              "learningMethod": "온라인강의|프로젝트|멘토링|독서|실습|커뮤니티참여",
              "reason": "이 학습이 필요한 구체적인 이유 (200자 이내)"
            }
          ],
          "targetJobRole": "%s",
          "currentTechSkills": %s,
          "overallAssessment": "현재 역량 수준과 목표 직무까지의 전체적인 평가 및 조언 (500자 이내)"
        }
        
        ## 주의사항
        - 4-6개의 학습 추천 항목 생성
        - 각 항목은 실제 실행 가능한 구체적인 내용으로 작성
        - priority는 긴급도와 중요도를 고려하여 설정
        - JSON 형식을 정확히 준수
        - 모든 필드 필수 입력
        """,
                request.getJobRole(),
                request.getCareerSummary(),
                String.join(", ", request.getTechSkills()),
                request.getJobRole(),
                toJsonString(request.getTechSkills())
        );
    }
    /**
     * List를 JSON 문자열로 변환
     */
    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 변환 실패", e);
            return "[]";
        }
    }
}