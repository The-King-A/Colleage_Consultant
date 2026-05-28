package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class SchoolListItemVO {

    private String schoolCode;
    private String schoolName;
    private String schoolType;
    private String location;
    private String city;
    private Boolean is985;
    private Boolean is211;
    private Boolean isDoubleFirst;
    private Integer latestMinScore;
    private Integer latestMinRank;
}
