package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class CompleteWizardStepRequest {

    private String step;
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

    /** 是否标记该步为已完成，默认 true */
    private Boolean markComplete;
    /** 完成后是否推进到下一步，默认与 markComplete 一致 */
    private Boolean advance;
    /** 仅将当前步骤设为该步（用于回看/修改），不自动推进 */
    private String setCurrentStep;
}
