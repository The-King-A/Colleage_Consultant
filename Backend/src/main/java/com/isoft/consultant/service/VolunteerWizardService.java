package com.isoft.consultant.service;

import com.isoft.consultant.dto.CompleteWizardStepRequest;
import com.isoft.consultant.dto.WizardProgressVO;

public interface VolunteerWizardService {

    WizardProgressVO getProgress(Long userId);

    WizardProgressVO completeStep(Long userId, CompleteWizardStepRequest request);

    /** 保存步骤数据，默认不标记完成、不推进 */
    WizardProgressVO saveStep(Long userId, CompleteWizardStepRequest request);
}
