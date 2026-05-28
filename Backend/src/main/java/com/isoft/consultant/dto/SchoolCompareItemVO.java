package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;

@Data
public class SchoolCompareItemVO {

    private String schoolCode;
    private String schoolName;
    private String location;
    private String city;
    private String schoolType;
    private Boolean is985;
    private Boolean is211;
    private Boolean isDoubleFirst;
    private String description;
    private List<AdmissionScoreVO> scores;

    /** 最近一年录取数据摘要 */
    private Integer latestYear;
    private Integer latestMinScore;
    private Integer latestMinRank;
    private Integer latestOverLineScore;
}
