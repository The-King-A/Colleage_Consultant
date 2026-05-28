package com.isoft.consultant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MajorDetailVO {

    private String majorCode;
    private String majorName;
    private String majorCategory;
    private String majorSubcategory;
    private String degreeType;
    private Integer studyDuration;
    private String description;
    private String courseList;
    private String careerDirection;
    private String graduateDestinations;
    private List<String> typicalEmployers;
    private List<String> typicalCareers;
    private String difficultyLevel;
    private Integer hotIndex;
    private BigDecimal employmentRate;
    private Integer salaryAvg;
    private Integer salary5year;
}
