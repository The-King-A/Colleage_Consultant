package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("major_info")
public class MajorInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

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
    private String typicalEmployers;
    private String typicalCareers;
    private Integer salaryAvg;

    @TableField("salary_5year")
    private Integer salary5year;

    private BigDecimal employmentRate;
    private String difficultyLevel;
    private String genderRatio;
    private Integer hotIndex;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
