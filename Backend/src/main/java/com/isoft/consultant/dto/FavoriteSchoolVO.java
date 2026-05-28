package com.isoft.consultant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteSchoolVO {
    private Long id;
    private String schoolCode;
    private String schoolName;
    private String schoolType;
    private String location;
    private String city;
    private String note;
    private Boolean is985;
    private Boolean is211;
    private LocalDateTime createTime;
}
