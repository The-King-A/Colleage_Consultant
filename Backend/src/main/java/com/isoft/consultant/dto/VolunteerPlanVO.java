package com.isoft.consultant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VolunteerPlanVO {
    private Long id;
    private String title;
    private String schoolProvince;
    private String scoreProvince;
    private String subjectType;
    private Integer userScore;
    private String planJson;
    private String note;
    private Integer rushCount;
    private Integer stableCount;
    private Integer safeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
