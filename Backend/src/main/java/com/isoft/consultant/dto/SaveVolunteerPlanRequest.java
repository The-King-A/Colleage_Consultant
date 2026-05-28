package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class SaveVolunteerPlanRequest {
    private String title;
    private String schoolProvince;
    private String scoreProvince;
    private String subjectType;
    private Integer userScore;
    private String planJson;
    private String note;
}
