package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;

@Data
public class WizardProgressVO {

    private String currentStep;
    private int progressPercent;
    private List<WizardStepStatusVO> steps;
    private Integer userScore;
    private String scoreProvince;
    private String subjectType;
    private String schoolProvince;
    private String hollandCode;
    private String hollandDominantType;
    private Long lastPlanId;
    private String lastPlanTitle;
    private Integer favoriteCount;
    private Integer matchRushCount;
    private Integer matchStableCount;
    private Integer matchSafeCount;
    /** 各步骤摘要与可编辑数据 */
    private List<WizardStepDetailVO> stepDetails;
}
