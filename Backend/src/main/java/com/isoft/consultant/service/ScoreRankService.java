package com.isoft.consultant.service;

import com.isoft.consultant.dto.ScoreRankLookupVO;

public interface ScoreRankService {

    ScoreRankLookupVO lookup(String province, String subjectType, Integer score, Integer year);

    /** 若表为空则写入样本一分一段 */
    void seedIfEmpty();
}
