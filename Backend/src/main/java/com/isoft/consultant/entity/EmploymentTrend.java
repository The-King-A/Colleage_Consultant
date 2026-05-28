package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employment_trend")
public class EmploymentTrend {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String majorCategory;
    private Integer year;
    private BigDecimal employmentRate;
    private Integer avgSalary;
    private Integer jobOpenings;
    private BigDecimal growthRate;
    private String trendDirection;
    private LocalDateTime createTime;
}
