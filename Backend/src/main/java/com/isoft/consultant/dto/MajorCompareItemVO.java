package com.isoft.consultant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MajorCompareItemVO {

    private String majorCode;
    private String majorName;
    private String majorCategory;
    private String majorSubcategory;
    private Integer hotIndex;
    private BigDecimal employmentRate;
    private Integer salaryAvg;
    private Integer salary5year;
    private String degreeType;
    private Integer studyDuration;
    private String description;
    private String careerDirection;
    private String graduateDestinations;
    private List<String> typicalEmployers;
    private List<String> typicalCareers;
    private String difficultyLevel;
    private String courseList;

    /** 近五年热度 */
    private List<MajorHotTrendVO> hotTrends;
    /** 所属门类近五年就业趋势 */
    private List<EmploymentTrendVO> employmentTrends;
}
