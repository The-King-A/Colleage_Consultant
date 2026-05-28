package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class AdmissionScoreVO {

    private Integer year;
    private String province;
    private String subjectType;
    private String batch;
    private String majorCode;
    private String majorName;
    private Integer minScore;
    private Integer avgScore;
    private Integer minRank;
    private Integer provinceLine;
    private Integer overLineScore;
    private String dataSource;
}
