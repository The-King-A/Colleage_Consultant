package com.isoft.consultant.service;

import com.isoft.consultant.dto.PageResult;
import com.isoft.consultant.dto.SchoolCompareItemVO;
import com.isoft.consultant.dto.SchoolDetailVO;
import com.isoft.consultant.dto.SchoolListItemVO;
import com.isoft.consultant.dto.SchoolMapMarkersVO;

import java.util.List;

public interface SchoolService {

    PageResult<SchoolListItemVO> pageSchools(
            String keyword,
            String location,
            Boolean is985,
            Boolean is211,
            String scoreProvince,
            String subjectType,
            int page,
            int size);

    SchoolDetailVO getDetail(String schoolCode, String scoreProvince, String subjectType);

    List<SchoolCompareItemVO> compare(
            List<String> schoolCodes,
            String scoreProvince,
            String subjectType,
            List<Integer> years);

    List<String> listLocations();

    List<SchoolListItemVO> listOptions(
            String keyword,
            String codes,
            String location,
            String scoreProvince,
            String subjectType,
            int limit);

    List<String> listScoreProvinces();

    SchoolMapMarkersVO listMapMarkers(
            String keyword,
            String location,
            Boolean is985,
            Boolean is211,
            String scoreProvince,
            String subjectType,
            int limit);
}
