// ResumeInfoRequest.java
package com.careercoach.careercoachapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class ResumeInfoRequest {

    // Getter/Setter
    @NotBlank(message = "경력 요약은 필수입니다.")
    private String careerSummary;

    @NotBlank(message = "수행 직무는 필수입니다.")
    private String jobRole;

    @NotEmpty(message = "기술 스킬은 최소 1개 이상 입력해야 합니다.")
    private List<String> techSkills;

    // 기본 생성자
    public ResumeInfoRequest() {}

    // 전체 생성자
    public ResumeInfoRequest(String careerSummary, String jobRole, List<String> techSkills) {
        this.careerSummary = careerSummary;
        this.jobRole = jobRole;
        this.techSkills = techSkills;
    }

    @Override
    public String toString() {
        return "ResumeInfoRequest{" +
                "careerSummary='" + careerSummary + '\'' +
                ", jobRole='" + jobRole + '\'' +
                ", techSkills=" + techSkills +
                '}';
    }
}