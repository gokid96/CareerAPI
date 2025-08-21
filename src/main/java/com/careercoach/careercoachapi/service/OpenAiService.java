// OpenAiService.java
package com.careercoach.careercoachapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private int maxTokens;

    @Value("${openai.api.temperature}")
    private double temperature;

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient;

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
     * 개선된 학습 경로 추천을 위한 프롬프트 작성
     */
    private String createLearningPathPrompt(String careerSummary, String jobRole, List<String> techSkills) {
        StringBuilder prompt = new StringBuilder();

        // 현재 상태 분석
        String careerLevel = analyzeCareerLevel(careerSummary);
        String jobCategory = categorizeJob(jobRole);
        String gapAnalysis = analyzeSkillGap(careerSummary, jobRole, techSkills);

        prompt.append("다음 구직자의 현재 상태를 분석하여 합격률을 극대화할 수 있는 효율적이고 실현 가능한 학습 로드맵을 제시해주세요.\n\n");

        prompt.append("** 구직자 현황 분석 **\n");
        prompt.append("- 현재 수준: ").append(careerLevel).append("\n");
        prompt.append("- 경력 요약: ").append(careerSummary).append("\n");
        prompt.append("- 목표 직무: ").append(jobRole).append("\n");
        prompt.append("- 보유 기술: ").append(String.join(", ", techSkills)).append("\n");
        prompt.append("- 직무 유형: ").append(jobCategory).append("\n");
        prompt.append("- 예상 갭: ").append(gapAnalysis).append("\n\n");

        prompt.append("** 로드맵 설계 원칙 **\n");
        prompt.append("1. 우선순위: HIGH(3개월 내 필수) → MEDIUM(6개월 내 권장) → LOW(장기 목표)\n");
        prompt.append("2. 실현 가능성: 현재 상황에서 실제 실행 가능한 계획\n");
        prompt.append("3. 효율성: 최소 투자로 최대 효과를 낼 수 있는 항목 우선\n");
        prompt.append("4. 연계성: 각 학습 항목 간의 시너지 효과 고려\n");
        prompt.append("5. 검증 가능성: 학습 성과를 객관적으로 증명할 수 있는 방법 포함\n\n");

        prompt.append("** 추천 항목 가이드라인 **\n");

        if (careerLevel.contains("신입")) {
            prompt.append("- 기초 역량 강화 (40%): 핵심 기술 스킬 및 업무 기초 소양\n");
            prompt.append("- 포트폴리오 구축 (30%): 실무 능력 증명을 위한 프로젝트\n");
            prompt.append("- 자격증/인증 (20%): 객관적 역량 증명\n");
            prompt.append("- 소프트 스킬 (10%): 기본적인 업무 소통 능력\n");
        } else if (careerLevel.contains("주니어")) {
            prompt.append("- 기술 심화 (35%): 현재 기술의 고급 활용 및 신기술 습득\n");
            prompt.append("- 실무 프로젝트 (25%): 업무 경험 확장 및 문제 해결 능력\n");
            prompt.append("- 업계 네트워킹 (20%): 커뮤니티 참여 및 정보 습득\n");
            prompt.append("- 리더십 개발 (20%): 팀워크 및 의사소통 능력 향상\n");
        } else {
            prompt.append("- 전략적 사고 (30%): 비즈니스 이해 및 기술 전략 수립\n");
            prompt.append("- 아키텍처 설계 (25%): 고급 기술 및 시스템 설계 능력\n");
            prompt.append("- 팀 리딩 (25%): 멘토링 및 프로젝트 관리 경험\n");
            prompt.append("- 업계 영향력 (20%): 발표, 블로그, 오픈소스 기여 등\n");
        }

        prompt.append("\n** 카테고리별 세부 지침 **\n");
        prompt.append("1. 기술 스택 심화: 현재 기술 중 가장 중요한 1-2개 선택하여 전문성 강화\n");
        prompt.append("2. 신기술 학습: 목표 직무에서 요구되는 최신 기술 중 학습 곡선이 완만한 것\n");
        prompt.append("3. 프로젝트 경험: 실제 업무와 유사한 상황의 프로젝트, GitHub 공개 필수\n");
        prompt.append("4. 자격증/인증: 해당 직무에서 실제로 인정받는 자격증만 선별\n");
        prompt.append("5. 소프트 스킬: 측정 가능한 방법으로 개발 (발표, 글쓰기, 멘토링 등)\n\n");

        prompt.append("** 현실적 제약사항 고려 **\n");
        prompt.append("- 학습 시간: 직장인 기준 주 10-15시간, 구직자 기준 주 30-40시간\n");
        prompt.append("- 비용: 무료 또는 월 10만원 이하의 합리적 비용\n");
        prompt.append("- 접근성: 온라인으로 학습 가능하며, 한국어 자료 존재\n");
        prompt.append("- 검증: 포트폴리오, 자격증, 또는 실무 적용으로 증명 가능\n\n");

        prompt.append("** 응답 형식 (총 5-6개 항목) **\n");
        prompt.append("각 추천 항목을 다음 형식으로 작성해주세요:\n\n");
        prompt.append("[카테고리] 구체적인 학습 제목\n");
        prompt.append("- 설명: 무엇을 어떻게 학습할지 구체적으로 기술\n");
        prompt.append("- 우선순위: HIGH/MEDIUM/LOW (이유 포함)\n");
        prompt.append("- 예상 기간: 현실적인 학습 기간 (주간 학습시간 포함)\n");
        prompt.append("- 학습 방법: 구체적인 학습 경로 및 자료 (온라인 강의, 책, 프로젝트 등)\n");
        prompt.append("- 성과 측정: 학습 완료를 어떻게 증명할지 (포트폴리오, 자격증, 프로젝트 등)\n");
        prompt.append("- 추천 이유: 현재 상태에서 이 학습이 필요한 구체적 이유\n");
        prompt.append("- 기대 효과: 학습 후 어떤 직무 기회가 생기는지, 연봉 향상 가능성\n\n");

        prompt.append("** 중요 지침 **\n");
        prompt.append("- 일반적인 조언보다는 이 구직자만의 맞춤형 추천\n");
        prompt.append("- 막연한 표현보다는 구체적이고 실행 가능한 계획\n");
        prompt.append("- 이상적인 목표보다는 현실적으로 달성 가능한 목표\n");
        prompt.append("- 개별 학습보다는 서로 연관성 있는 통합적 로드맵\n");

        return prompt.toString();
    }

    /**
     * 범용적 스킬 갭 분석 (모든 직종 적용 가능)
     */
    private String analyzeSkillGap(String careerSummary, String jobRole, List<String> techSkills) {
        String role = jobRole.toLowerCase();
        String summary = careerSummary.toLowerCase();
        List<String> gaps = new ArrayList<>();

        // 1. 경력 수준 vs 목표 직급 갭 분석
        if (role.contains("시니어") || role.contains("리드") || role.contains("팀장") || role.contains("매니저")) {
            if (!summary.contains("팀") && !summary.contains("리딩") && !summary.contains("관리") &&
                    !summary.contains("멘토") && !summary.contains("프로젝트 관리")) {
                gaps.add("리더십/관리 경험 부족");
            }
        }

        // 2. 신입 vs 경력직 요구사항 갭
        if (role.contains("신입") || role.contains("주니어")) {
            if (!summary.contains("프로젝트") && !summary.contains("경험") && !summary.contains("실습")) {
                gaps.add("실무/프로젝트 경험 부족");
            }
        }

        // 3. 경력 기간 vs 기술 스킬 수준 갭
        boolean hasAdvancedSkills = techSkills.stream().anyMatch(skill ->
                skill.toLowerCase().contains("고급") || skill.toLowerCase().contains("전문") ||
                        skill.toLowerCase().contains("아키텍처") || skill.toLowerCase().contains("설계"));

        if ((summary.contains("3년") || summary.contains("4년") || summary.contains("5년")) && !hasAdvancedSkills) {
            gaps.add("경력 대비 고급 기술 스킬 부족");
        }

        // 4. 현대적 업무 도구/방법론 갭
        boolean hasModernTools = techSkills.stream().anyMatch(skill -> {
            String s = skill.toLowerCase();
            return s.contains("git") || s.contains("jira") || s.contains("slack") ||
                    s.contains("notion") || s.contains("협업") || s.contains("애자일") ||
                    s.contains("scrum") || s.contains("클라우드") || s.contains("aws") ||
                    s.contains("google") || s.contains("office 365");
        });

        if (!hasModernTools) {
            gaps.add("현대적 업무 도구/방법론 경험 부족");
        }

        // 5. 데이터 활용 능력 갭 (모든 직종에서 중요)
        boolean hasDataSkills = techSkills.stream().anyMatch(skill -> {
            String s = skill.toLowerCase();
            return s.contains("excel") || s.contains("데이터") || s.contains("분석") ||
                    s.contains("sql") || s.contains("tableau") || s.contains("power bi") ||
                    s.contains("google analytics") || s.contains("통계");
        });

        if (!hasDataSkills) {
            gaps.add("데이터 분석/활용 능력 부족");
        }

        // 6. 소프트 스킬 갭
        boolean hasSoftSkills = summary.contains("소통") || summary.contains("협업") ||
                summary.contains("발표") || summary.contains("교육") ||
                summary.contains("멘토") || summary.contains("리딩");

        if (!hasSoftSkills) {
            gaps.add("소프트 스킬(소통/협업/리더십) 경험 부족");
        }

        // 7. 업계 트렌드 이해도 갭
        boolean hasModernConcepts = techSkills.stream().anyMatch(skill -> {
            String s = skill.toLowerCase();
            return s.contains("ai") || s.contains("머신러닝") || s.contains("자동화") ||
                    s.contains("디지털") || s.contains("클라우드") || s.contains("모바일") ||
                    s.contains("ux") || s.contains("사용자");
        });

        if (!hasModernConcepts && !summary.contains("최신") && !summary.contains("트렌드")) {
            gaps.add("최신 업계 트렌드 이해도 부족");
        }

        // 결과 반환
        if (gaps.isEmpty()) {
            return "전반적으로 목표 직무에 적합한 역량 보유";
        } else if (gaps.size() == 1) {
            return gaps.get(0);
        } else {
            return String.join(", ", gaps.subList(0, Math.min(2, gaps.size()))) + " 등";
        }
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
            String result = webClient
                    .post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .headers(headers -> {
                        headers.set("Authorization", "Bearer " + apiKey);
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .bodyValue(requestBody)  // Map을 직접 전달
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        // 에러 처리
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new RuntimeException("OpenAI API 오류: " + response.statusCode() + " - " + errorBody)
                                ));
                    })
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));  // 타임아웃 설정

            // 응답 파싱은 동일
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