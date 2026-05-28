package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.dto.AdmissionScoreVO;
import com.isoft.consultant.entity.AdmissionScore;
import com.isoft.consultant.entity.ProvinceScoreLine;
import com.isoft.consultant.mapper.AdmissionScoreMapper;
import com.isoft.consultant.mapper.ProvinceScoreLineMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AdmissionScoreHelper {

    private final AdmissionScoreMapper admissionScoreMapper;
    private final ProvinceScoreLineMapper provinceScoreLineMapper;

    public AdmissionScoreHelper(
            AdmissionScoreMapper admissionScoreMapper,
            ProvinceScoreLineMapper provinceScoreLineMapper) {
        this.admissionScoreMapper = admissionScoreMapper;
        this.provinceScoreLineMapper = provinceScoreLineMapper;
    }

    public List<AdmissionScoreVO> listSchoolScores(
            String schoolCode,
            String province,
            String subjectType,
            List<Integer> years,
            boolean schoolLevelOnly) {

        LambdaQueryWrapper<AdmissionScore> wrapper = new LambdaQueryWrapper<AdmissionScore>()
                .eq(AdmissionScore::getSchoolCode, schoolCode)
                .eq(AdmissionScore::getProvince, province)
                .eq(AdmissionScore::getSubjectType, subjectType);

        if (schoolLevelOnly) {
            wrapper.and(w -> w.isNull(AdmissionScore::getMajorCode)
                    .or()
                    .eq(AdmissionScore::getMajorCode, ""));
        }

        if (years != null && !years.isEmpty()) {
            wrapper.in(AdmissionScore::getYear, years);
        }

        wrapper.orderByDesc(AdmissionScore::getYear);

        List<AdmissionScore> rows = admissionScoreMapper.selectList(wrapper);
        if (schoolLevelOnly) {
            rows = rows.stream()
                    .filter(r -> r.getMajorCode() == null || r.getMajorCode().isBlank())
                    .collect(Collectors.toList());
        }

        Map<String, Integer> lineCache = loadProvinceLines(province, subjectType);

        List<AdmissionScoreVO> result = new ArrayList<>();
        for (AdmissionScore row : rows) {
            result.add(toVo(row, lineCache));
        }
        result.sort(Comparator.comparing(AdmissionScoreVO::getYear).reversed());
        return result;
    }

    public Integer findLatestMinScore(String schoolCode, String province, String subjectType) {
        List<AdmissionScoreVO> scores = listSchoolScores(schoolCode, province, subjectType, null, true);
        return scores.isEmpty() ? null : scores.get(0).getMinScore();
    }

    public Integer findLatestMinRank(String schoolCode, String province, String subjectType) {
        List<AdmissionScoreVO> scores = listSchoolScores(schoolCode, province, subjectType, null, true);
        return scores.isEmpty() ? null : scores.get(0).getMinRank();
    }

    private AdmissionScoreVO toVo(AdmissionScore row, Map<String, Integer> lineCache) {
        AdmissionScoreVO vo = new AdmissionScoreVO();
        vo.setYear(row.getYear());
        vo.setProvince(row.getProvince());
        vo.setSubjectType(row.getSubjectType());
        vo.setBatch(row.getBatch());
        vo.setMajorCode(row.getMajorCode());
        vo.setMajorName(row.getMajorName());
        vo.setMinScore(row.getMinScore());
        vo.setAvgScore(row.getAvgScore());
        vo.setMinRank(row.getMinRank());
        vo.setDataSource(row.getDataSource());

        Integer line = row.getProvinceLine();
        if (line == null) {
            line = lineCache.get(lineKey(row.getYear(), row.getProvince(), row.getSubjectType(), row.getBatch()));
        }
        vo.setProvinceLine(line);
        if (line != null && row.getMinScore() != null) {
            vo.setOverLineScore(row.getMinScore() - line);
        }
        return vo;
    }

    private Map<String, Integer> loadProvinceLines(String province, String subjectType) {
        List<ProvinceScoreLine> lines = provinceScoreLineMapper.selectList(
                new LambdaQueryWrapper<ProvinceScoreLine>()
                        .eq(ProvinceScoreLine::getProvince, province)
                        .eq(ProvinceScoreLine::getSubjectType, subjectType));

        Map<String, Integer> map = new HashMap<>();
        for (ProvinceScoreLine line : lines) {
            map.put(lineKey(line.getYear(), line.getProvince(), line.getSubjectType(), line.getBatch()), line.getScore());
        }
        return map;
    }

    private String lineKey(Integer year, String province, String subjectType, String batch) {
        return year + "|" + province + "|" + subjectType + "|" + batch;
    }
}
