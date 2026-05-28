package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HollandResultVO {
    private String hollandCode;
    private String dominantType;
    private String personalitySummary;
    private Map<String, Integer> dimensionScores;
    private Map<String, String> dimensionNames;
    private List<String> recommendedMajors;
    private List<MajorMatchVO> majorMatches;

    @Data
    public static class MajorMatchVO {
        private String majorName;
        private String category;
        private int matchPercent;
        private String reason;
    }
}
