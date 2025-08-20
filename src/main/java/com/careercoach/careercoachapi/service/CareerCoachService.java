// CareerCoachService.java
package com.careercoach.careercoachapi.service;

import com.careercoach.careercoachapi.dto.response.LearningPathResponse;
import com.careercoach.careercoachapi.dto.response.LearningRecommendation;
import com.careercoach.careercoachapi.dto.request.ResumeInfoRequest;
import com.careercoach.careercoachapi.dto.response.InterviewQuestionsResponse;
import com.careercoach.careercoachapi.entity.ResumeInfo;
import com.careercoach.careercoachapi.repository.ResumeInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CareerCoachService {
    
    @Autowired
    private ResumeInfoRepository resumeInfoRepository;
    
    @Autowired
    private OpenAiService openAiService;
    
    @Autowired
    private LearningPathParserService learningPathParserService;
    
    /**
     * 이력서 정보를 저장하고 맞춤형 면접 질문 생성
     */
    public InterviewQuestionsResponse generateInterviewQuestions(ResumeInfoRequest request) {
        try {
            // 1. 이력서 정보 저장
            ResumeInfo resumeInfo = saveResumeInfo(request);
            
            // 2. OpenAI를 통한 면접 질문 생성
            List<String> questions = openAiService.generateInterviewQuestions(
                request.getCareerSummary(),
                request.getJobRole(),
                request.getTechSkills()
            );
            
            // 3. 응답 DTO 생성
            return new InterviewQuestionsResponse(
                questions,
                request.getJobRole(),
                request.getTechSkills()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("면접 질문 생성 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 이력서 정보를 바탕으로 개인 맞춤형 학습 경로 추천 생성
     */
    public LearningPathResponse generateLearningPath(ResumeInfoRequest request) {
        try {
            // 1. 이력서 정보 저장 (중복 저장 방지를 위해 기존 데이터 확인)
            ResumeInfo resumeInfo = findOrSaveResumeInfo(request);
            
            // 2. OpenAI를 통한 학습 경로 추천 생성
            String rawResponse = openAiService.generateLearningPath(
                request.getCareerSummary(),
                request.getJobRole(),
                request.getTechSkills()
            );
            
            // 3. 응답 파싱
            List<LearningRecommendation> recommendations =
                learningPathParserService.parseLearningRecommendations(rawResponse);
            
            String overallAssessment = 
                learningPathParserService.extractOverallAssessment(rawResponse);
            
            // 4. 응답 DTO 생성
            return new LearningPathResponse(
                recommendations,
                request.getJobRole(),
                request.getTechSkills(),
                overallAssessment
            );
            
        } catch (Exception e) {
            throw new RuntimeException("학습 경로 추천 생성 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 중복 저장 방지를 위한 이력서 정보 조회 또는 저장
     */
    private ResumeInfo findOrSaveResumeInfo(ResumeInfoRequest request) {
        // 동일한 내용의 이력서가 이미 있는지 확인 (간단한 중복 체크)
        List<ResumeInfo> existingResumes = resumeInfoRepository.findByJobRoleContainingIgnoreCase(request.getJobRole());
        
        for (ResumeInfo existing : existingResumes) {
            if (existing.getCareerSummary().equals(request.getCareerSummary()) &&
                new HashSet<>(existing.getTechSkills()).containsAll(request.getTechSkills()) &&
                new HashSet<>(request.getTechSkills()).containsAll(existing.getTechSkills())) {
                return existing; // 기존 데이터 반환
            }
        }
        
        // 새로운 데이터 저장
        return saveResumeInfo(request);
    }
    private ResumeInfo saveResumeInfo(ResumeInfoRequest request) {
        ResumeInfo resumeInfo = new ResumeInfo(
            request.getCareerSummary(),
            request.getJobRole(),
            request.getTechSkills()
        );
        
        return resumeInfoRepository.save(resumeInfo);
    }
    
    /**
     * 저장된 이력서 정보 조회
     */
    @Transactional(readOnly = true)
    public Optional<ResumeInfo> getResumeInfo(Long id) {
        return resumeInfoRepository.findById(id);
    }
    
    /**
     * 모든 이력서 정보 조회
     */
    @Transactional(readOnly = true)
    public List<ResumeInfo> getAllResumeInfo() {
        return resumeInfoRepository.findAll();
    }
    
    /**
     * 직무별 이력서 정보 조회
     */
    @Transactional(readOnly = true)
    public List<ResumeInfo> getResumeInfoByJobRole(String jobRole) {
        return resumeInfoRepository.findByJobRoleContainingIgnoreCase(jobRole);
    }
    
    /**
     * 특정 기술 스킬을 보유한 이력서 정보 조회
     */
    @Transactional(readOnly = true)
    public List<ResumeInfo> getResumeInfoByTechSkills(List<String> techSkills) {
        return resumeInfoRepository.findByTechSkillsIn(techSkills);
    }
    
    /**
     * 최근 등록된 이력서 정보 조회
     */
    @Transactional(readOnly = true)
    public Optional<ResumeInfo> getLatestResumeInfo() {
        return resumeInfoRepository.findTopByOrderByCreatedAtDesc();
    }
}