// ResumeInfoRepository.java
package com.careercoach.careercoachapi.repository;

import com.careercoach.careercoachapi.entity.ResumeInfo;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeInfoRepository extends JpaRepository<ResumeInfo, Long> {

    // 직무별 이력서 조회
    // List<ResumeInfo> findByJobRoleContainingIgnoreCase(String jobRole);
    // 포함 검색 (대소문자 무시)
    List<ResumeInfo> findByJobRoleContainingIgnoreCase(String jobRole);

    // 특정 기간 내 생성된 이력서 조회
    List<ResumeInfo> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 기술 스킬로 이력서 검색 (커스텀 쿼리)
    @Query("SELECT r FROM ResumeInfo r JOIN r.techSkills ts WHERE ts IN :skills")
    List<ResumeInfo> findByTechSkillsIn(@Param("skills") List<String> skills);

    // 최근 생성된 이력서 조회
    Optional<ResumeInfo> findTopByOrderByCreatedAtDesc();
}