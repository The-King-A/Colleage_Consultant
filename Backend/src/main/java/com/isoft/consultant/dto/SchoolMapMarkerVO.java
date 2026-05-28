package com.isoft.consultant.dto;

import lombok.Data;

@Data
public class SchoolMapMarkerVO {

    private String schoolCode;
    private String schoolName;
    private String province;
    private String city;
    private String location;
    private Double lat;
    private Double lng;
    private Boolean is985;
    private Boolean is211;
    private Boolean isDoubleFirst;
    private Integer latestMinScore;
    private Integer latestMinRank;
}
