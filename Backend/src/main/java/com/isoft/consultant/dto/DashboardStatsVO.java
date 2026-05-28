package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardStatsVO {
    private long schoolCount;
    private long majorCount;
    private long admissionRecordCount;
    private long provinceWithScoreCount;
    private List<Map<String, Object>> topHotMajors;
    private List<Map<String, Object>> schoolsByProvince;
}
