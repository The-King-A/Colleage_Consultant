package com.isoft.consultant.service;

import com.isoft.consultant.dto.SaveVolunteerPlanRequest;
import com.isoft.consultant.dto.VolunteerPlanVO;

import java.util.List;

public interface VolunteerPlanService {

    List<VolunteerPlanVO> list(Long userId);

    VolunteerPlanVO get(Long userId, Long id);

    Long save(Long userId, SaveVolunteerPlanRequest req);

    void delete(Long userId, Long id);

    /** 供 AI 评审使用的纯文本方案摘要 */
    String buildReviewSummary(Long userId, Long id);
}
