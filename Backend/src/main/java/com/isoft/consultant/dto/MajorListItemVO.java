package com.isoft.consultant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MajorListItemVO {

    private String majorCode;
    private String majorName;
    private String majorCategory;
    private String majorSubcategory;
    private Integer hotIndex;
    private BigDecimal employmentRate;
    private Integer salaryAvg;
}
