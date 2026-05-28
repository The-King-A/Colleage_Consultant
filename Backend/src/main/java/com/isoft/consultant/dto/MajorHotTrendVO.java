package com.isoft.consultant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MajorHotTrendVO {

    private Integer year;
    private BigDecimal hotScore;
    private Integer rank;
    private Integer searchVolume;
    private BigDecimal applyGrowth;
}
