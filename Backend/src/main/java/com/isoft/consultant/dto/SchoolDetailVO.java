package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;

@Data
public class SchoolDetailVO {

    private String schoolCode;
    private String schoolName;
    private String schoolType;
    private String schoolNature;
    private String location;
    private String city;
    private String website;
    private String description;
    private Boolean is985;
    private Boolean is211;
    private Boolean isDoubleFirst;
    private Integer foundedYear;
    private List<AdmissionScoreVO> admissionScores;
}
