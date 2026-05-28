package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class RankEquivalentVO {

    private Integer year;
    private Integer equivalentScore;
    private Integer cumulativeRankAtYear;
    private String note;
}
