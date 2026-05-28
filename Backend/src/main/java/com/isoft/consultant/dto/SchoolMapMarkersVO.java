package com.isoft.consultant.dto;

import lombok.Data;

import java.util.List;

@Data
public class SchoolMapMarkersVO {

    private List<SchoolMapMarkerVO> markers;
    private int totalQueried;
    private int mappedCount;
    private int skippedNoCoords;
}
