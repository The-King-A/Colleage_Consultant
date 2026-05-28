package com.isoft.consultant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("volunteer_wizard_progress")
public class VolunteerWizardProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String currentStep;
    private String completedSteps;
    private Integer userScore;
    private String scoreProvince;
    private String subjectType;
    private String schoolProvince;
    private String hollandCode;
    private Long lastPlanId;
    private String metaJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
