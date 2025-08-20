// ResumeInfo.java
package com.careercoach.careercoachapi.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "resume_info")
public class ResumeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "career_summary", nullable = false, length = 1000)
    private String careerSummary;

    @Column(name = "job_role", nullable = false, length = 200)
    private String jobRole;

    @ElementCollection
    @CollectionTable(name = "tech_skills", joinColumns = @JoinColumn(name = "resume_id"))
    @Column(name = "skill")
    private List<String> techSkills;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public ResumeInfo() {}

    // 생성자
    public ResumeInfo(String careerSummary, String jobRole, List<String> techSkills) {
        this.careerSummary = careerSummary;
        this.jobRole = jobRole;
        this.techSkills = techSkills;
    }

    // JPA 생명주기 콜백
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter/Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCareerSummary() {
        return careerSummary;
    }

    public void setCareerSummary(String careerSummary) {
        this.careerSummary = careerSummary;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public List<String> getTechSkills() {
        return techSkills;
    }

    public void setTechSkills(List<String> techSkills) {
        this.techSkills = techSkills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
