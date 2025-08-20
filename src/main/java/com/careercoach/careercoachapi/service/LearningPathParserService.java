// LearningPathParserService.java
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.response.LearningRecommendation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LearningPathParserService {
    
    /**
     * OpenAI 응답에서 학습 경로 추천 정보를 파싱
     */
    public List<LearningRecommendation> parseLearningRecommendations(String response) {
        List<LearningRecommendation> recommendations = new ArrayList<>();
        
        if (response == null || response.trim().isEmpty()) {
            return recommendations;
        }
        
        // 각 추천 항목을 구분 (카테고리로 시작하는 패턴)
        String[] sections = response.split("(?=\\[.+?\\])");
        
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;
            
            LearningRecommendation recommendation = parseRecommendationSection(section.trim());
            if (recommendation != null) {
                recommendations.add(recommendation);
            }
        }
        
        return recommendations;
    }
    
    /**
     * 개별 추천 섹션 파싱
     */
    private LearningRecommendation parseRecommendationSection(String section) {
        try {
            LearningRecommendation recommendation = new LearningRecommendation();
            
            // 카테고리와 제목 추출: [카테고리] 제목
            Pattern titlePattern = Pattern.compile("\\[(.+?)\\]\\s*(.+?)(?:\\n|$)");
            Matcher titleMatcher = titlePattern.matcher(section);
            
            if (titleMatcher.find()) {
                recommendation.setCategory(titleMatcher.group(1).trim());
                recommendation.setTitle(titleMatcher.group(2).trim());
            } else {
                return null; // 제목을 찾을 수 없으면 null 반환
            }
            
            // 각 필드 추출
            recommendation.setDescription(extractField(section, "설명"));
            recommendation.setPriority(extractField(section, "우선순위"));
            recommendation.setEstimatedDuration(extractField(section, "예상 기간"));
            recommendation.setLearningMethod(extractField(section, "학습 방법"));
            recommendation.setReason(extractField(section, "추천 이유"));
            
            // 필수 필드가 비어있으면 기본값 설정
            setDefaultValuesIfEmpty(recommendation);
            
            return recommendation;
            
        } catch (Exception e) {
            // 파싱 실패 시 null 반환
            return null;
        }
    }
    
    /**
     * 특정 필드 값 추출
     */
    private String extractField(String section, String fieldName) {
        Pattern pattern = Pattern.compile("-\\s*" + fieldName + "\\s*:?\\s*(.+?)(?=\\n-|\\n\\[|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(section);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return "";
    }
    
    /**
     * 빈 필드에 기본값 설정
     */
    private void setDefaultValuesIfEmpty(LearningRecommendation recommendation) {
        if (recommendation.getDescription() == null || recommendation.getDescription().isEmpty()) {
            recommendation.setDescription("상세 설명이 제공되지 않았습니다.");
        }
        
        if (recommendation.getPriority() == null || recommendation.getPriority().isEmpty()) {
            recommendation.setPriority("MEDIUM");
        } else {
            // 우선순위 정규화
            String priority = recommendation.getPriority().toUpperCase();
            if (!priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW")) {
                recommendation.setPriority("MEDIUM");
            }
        }
        
        if (recommendation.getEstimatedDuration() == null || recommendation.getEstimatedDuration().isEmpty()) {
            recommendation.setEstimatedDuration("1-2개월");
        }
        
        if (recommendation.getLearningMethod() == null || recommendation.getLearningMethod().isEmpty()) {
            recommendation.setLearningMethod("온라인 학습");
        }
        
        if (recommendation.getReason() == null || recommendation.getReason().isEmpty()) {
            recommendation.setReason("해당 직무 역량 향상에 도움이 됩니다.");
        }
    }
    
    /**
     * 전체 평가 및 요약 추출
     */
    public String extractOverallAssessment(String response) {
        // 응답의 첫 번째 부분이나 마지막 부분에서 전체 평가 내용 추출
        String[] lines = response.split("\n");
        StringBuilder assessment = new StringBuilder();
        
        boolean foundAssessment = false;
        for (String line : lines) {
            // 전체 평가나 요약으로 보이는 패턴
            if (line.contains("전체") || line.contains("종합") || line.contains("요약") || 
                line.contains("평가") || line.contains("현재 상태")) {
                foundAssessment = true;
                assessment.append(line).append(" ");
            } else if (foundAssessment && line.startsWith("[")) {
                // 새로운 카테고리가 시작되면 종료
                break;
            } else if (foundAssessment) {
                assessment.append(line).append(" ");
            }
        }
        
        String result = assessment.toString().trim();
        return result.isEmpty() ? "개인 맞춤형 학습 경로가 생성되었습니다." : result;
    }
}