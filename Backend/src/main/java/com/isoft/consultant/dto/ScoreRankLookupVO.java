package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScoreRankLookupVO {

    private String province;
    private String subjectType;
    private Integer year;
    private Integer userScore;
    /** 查表使用的分数（可能与输入相同或向下取整） */
    private Integer matchedScore;
    /** 累计位次（越小越靠前） */
    private Integer cumulativeRank;
    private Integer segmentCount;
    /** 约超过本省同科类考生比例（%） */
    private Double beatPercent;
    /** 等效往年分 */
    private List<RankEquivalentVO> equivalents;
    private String tip;
}
