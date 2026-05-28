package com.isoft.consultant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmploymentTrendVO {

    private Integer year;
    private BigDecimal employmentRate;
    private Integer avgSalary;
    private Integer jobOpenings;
    private BigDecimal growthRate;
    private String trendDirection;
}
