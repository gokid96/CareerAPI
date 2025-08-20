// OpenAiService.java
package com.careercoach.careercoachapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Autowired
    private WebClient openAiWebClient;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private int maxTokens;

    @Value("${openai.api.temperature}")
    private double temperature;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 이력서 정보를 바탕으로 면접 질문 생성
     */
    public List<String> generateInterviewQuestions(String careerSummary, String jobRole, List<String> techSkills) {
        try {
            String prompt = createInterviewQuestionPrompt(careerSummary, jobRole, techSkills);
            String response = callOpenAiApi(prompt);
            return parseInterviewQuestions(response);
        } catch (Exception e) {
            throw new RuntimeException("면접 질문 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이력서 정보를 바탕으로 개인 맞춤형 학습 경로 추천
     */
    public String generateLearningPath(String careerSummary, String jobRole, List<String> techSkills) {
        try {
            String prompt = createLearningPathPrompt(careerSummary, jobRole, techSkills);
            String response = callOpenAiApi(prompt);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("학습 경로 추천 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 학습 경로 추천을 위한 프롬프트 작성
     */
    private String createLearningPathPrompt(String careerSummary, String jobRole, List<String> techSkills) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 이력서 정보를 분석하여 구직자의 합격률을 높일 수 있는 실현 가능한 현실적인 개인 맞춤형 학습 경로를 제안해주세요.\n\n");
        prompt.append("** 이력서 정보 **\n");
        prompt.append("- 경력 요약: ").append(careerSummary).append("\n");
        prompt.append("- 담당 직무: ").append(jobRole).append("\n");
        prompt.append("- 보유 기술: ").append(String.join(", ", techSkills)).append("\n\n");

        prompt.append("** 추천 항목별 가이드라인 **\n");
        prompt.append("1. 기술 스택 심화: 현재 보유 기술의 고급 활용법이나 관련 심화 기술\n");
        prompt.append("2. 신기술 학습: 웹 검색을 통한 해당 직무에서 최근 주목받는 기술이나 트렌드\n");
        prompt.append("3. 프로젝트 경험: 포트폴리오 강화를 위한 구체적인 프로젝트 아이디어\n");
        prompt.append("4. 소프트 스킬: 커뮤니케이션, 협업, 리더십 등 업무 역량\n");
        prompt.append("5. 자격증/인증: 해당 분야에서 인정받는 자격증이나 인증\n\n");

        prompt.append("** 응답 형식 **\n");
        prompt.append("각 추천 항목을 다음 형식으로 작성해주세요:\n\n");
        prompt.append("[카테고리] 제목\n");
        prompt.append("- 설명: (구체적인 내용)\n");
        prompt.append("- 표현방식: (위 가이드라인을 통한 종합 로드맵 형식)\n");
        prompt.append("- 예상 기간: (학습에 필요한 시간)\n");
        prompt.append("- 추천 이유: (왜 이 학습이 필요한지,학습시 향후 어떤 직종으로 취업이 가능한지)\n\n");

        return prompt.toString();
    }
    /**
     * 면접 질문 생성을 위한 프롬프트 작성
     */
    private String createInterviewQuestionPrompt(String careerSummary, String jobRole, List<String> techSkills) {
        StringBuilder prompt = new StringBuilder();

        // 경력 수준 분석
        String careerLevel = analyzeCareerLevel(careerSummary);
        String jobCategory = categorizeJob(jobRole);

        prompt.append("다음 지원자 정보를 바탕으로 실제 면접에서 물어볼 법한 현실적이고 구체적인 질문 5개를 생성해주세요.\n\n");

        prompt.append("** 지원자 정보 **\n");
        prompt.append("- 경력 수준: ").append(careerLevel).append("\n");
        prompt.append("- 경력 요약: ").append(careerSummary).append("\n");
        prompt.append("- 지원 직무: ").append(jobRole).append("\n");
        prompt.append("- 보유 기술: ").append(String.join(", ", techSkills)).append("\n");
        prompt.append("- 직무 유형: ").append(jobCategory).append("\n\n");

        prompt.append("** 질문 생성 원칙 **\n");
        prompt.append("1. 실제 면접관이 물어볼 만한 현실적인 질문\n");
        prompt.append("2. 지원자가 ").append(careerLevel).append(" 수준에서 답변 가능한 난이도\n");
        prompt.append("3. ").append(jobCategory).append(" 직무 특성을 반영한 질문\n");
        prompt.append("4. 구체적인 상황과 경험을 묻는 질문 (추상적 질문 지양)\n");
        prompt.append("5. 한 문장으로 명확하게 표현 (복합 질문 지양)\n\n");

        prompt.append("** 질문 유형별 배분 **\n");

        if (careerLevel.contains("신입")) {
            prompt.append("- 학습 의지 및 성장 가능성: 2문항\n");
            prompt.append("- 기초 기술 이해도: 1문항\n");
            prompt.append("- 협업 및 소통 능력: 1문항\n");
            prompt.append("- 직무 적합성 및 동기: 1문항\n");
        } else if (careerLevel.contains("주니어")) {
            prompt.append("- 실무 경험 및 문제 해결: 2문항\n");
            prompt.append("- 기술 활용 능력: 1문항\n");
            prompt.append("- 협업 및 커뮤니케이션: 1문항\n");
            prompt.append("- 성장 방향 및 목표: 1문항\n");
        } else {
            prompt.append("- 프로젝트 리딩 및 의사결정: 2문항\n");
            prompt.append("- 기술적 깊이 및 아키텍처: 1문항\n");
            prompt.append("- 팀 관리 및 멘토링: 1문항\n");
            prompt.append("- 비즈니스 이해 및 전략: 1문항\n");
        }

        prompt.append("\n** 기술 관련 질문 가이드 **\n");
        if (jobCategory.equals("기술직")) {
            prompt.append("- 보유 기술(").append(String.join(", ", techSkills)).append(") 중 최소 2개 이상 언급\n");
            prompt.append("- 기술적 선택의 이유나 트레이드오프를 묻는 질문 포함\n");
            prompt.append("- 실제 구현 경험이나 문제 해결 사례 요구\n");
        } else {
            prompt.append("- 업무 도구 활용 경험이나 데이터 분석 능력 확인\n");
            prompt.append("- 프로세스 개선이나 효율성 향상 사례 요구\n");
        }

        prompt.append("\n** 금지 사항 **\n");
        prompt.append("- 개인정보나 민감한 내용을 묻는 질문\n");
        prompt.append("- 너무 추상적이거나 철학적인 질문\n");
        prompt.append("- 한 번에 여러 가지를 묻는 복합 질문\n");
        prompt.append("- 단순 암기 위주의 이론적 질문\n\n");

        prompt.append("** 응답 형식 **\n");
        prompt.append("다음과 같이 번호를 매겨 5개의 질문을 작성하되, 각 질문은 한 줄로 완결해주세요:\n\n");
        prompt.append("1. [구체적이고 현실적인 질문]\n");
        prompt.append("2. [구체적이고 현실적인 질문]\n");
        prompt.append("3. [구체적이고 현실적인 질문]\n");
        prompt.append("4. [구체적이고 현실적인 질문]\n");
        prompt.append("5. [구체적이고 현실적인 질문]\n");

        return prompt.toString();
    }

    /**
     * 경력 수준 분석
     */
    private String analyzeCareerLevel(String careerSummary) {
        String summary = careerSummary.toLowerCase();

        if (summary.contains("신입") || summary.contains("졸업") || summary.contains("인턴") ||
                summary.contains("부트캠프") || summary.contains("취업 준비")) {
            return "신입급";
        } else if (summary.contains("1년") || summary.contains("2년") || summary.contains("주니어")) {
            return "주니어급 (1-2년차)";
        } else if (summary.contains("3년") || summary.contains("4년") || summary.contains("5년")) {
            return "중급 (3-5년차)";
        } else if (summary.contains("시니어") || summary.contains("리드") || summary.contains("팀장") ||
                summary.matches(".*[6-9]년.*") || summary.contains("10년")) {
            return "시니어급 (6년 이상)";
        } else {
            return "경력자"; // 기본값
        }
    }

    /**
     * 직무 카테고리 분류
     */
    private String categorizeJob(String jobRole) {
        String role = jobRole.toLowerCase();

        if (role.contains("개발") || role.contains("엔지니어") || role.contains("프로그래머") ||
                role.contains("devops") || role.contains("데이터")) {
            return "기술직";
        } else if (role.contains("마케팅") || role.contains("기획") || role.contains("영업") ||
                role.contains("세일즈") || role.contains("pm") || role.contains("매니저")) {
            return "비즈니스직";
        } else if (role.contains("디자인") || role.contains("ux") || role.contains("ui")) {
            return "디자인직";
        } else if (role.contains("인사") || role.contains("hr") || role.contains("채용") ||
                role.contains("교육") || role.contains("총무")) {
            return "지원직";
        } else {
            return "일반직";
        }
    }

    /**
     * OpenAI API 호출
     */
    private String callOpenAiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", Arrays.asList(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", maxTokens,
                "temperature", temperature
        );

        try {
            Mono<String> response = openAiWebClient
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class);

            String result = response.block();

            // OpenAI 응답에서 content 추출
            JsonNode jsonNode = objectMapper.readTree(result);
            return jsonNode.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            throw new RuntimeException("OpenAI API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * OpenAI 응답에서 면접 질문 파싱
     */
    private List<String> parseInterviewQuestions(String response) {
        // 응답에서 번호가 있는 질문들을 추출
        return Arrays.stream(response.split("\n"))
                .filter(line -> line.matches("^\\d+\\..*"))  // 숫자로 시작하는 라인
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", "").trim())  // 번호 제거
                .filter(question -> !question.isEmpty())
                .limit(5)  // 최대 5개 질문
                .toList();
    }
}