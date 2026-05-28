package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.common.CityGeoResolver;
import com.isoft.consultant.common.CityProvinceResolver;
import com.isoft.consultant.common.PageQueryUtil;
import com.isoft.consultant.common.ProvinceOrder;
import com.isoft.consultant.dto.AdmissionScoreVO;
import com.isoft.consultant.dto.PageResult;
import com.isoft.consultant.dto.SchoolCompareItemVO;
import com.isoft.consultant.dto.SchoolDetailVO;
import com.isoft.consultant.dto.SchoolListItemVO;
import com.isoft.consultant.dto.SchoolMapMarkerVO;
import com.isoft.consultant.dto.SchoolMapMarkersVO;
import com.isoft.consultant.entity.SchoolInfo;
import com.isoft.consultant.mapper.AdmissionScoreMapper;
import com.isoft.consultant.mapper.SchoolInfoMapper;
import com.isoft.consultant.service.SchoolService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SchoolServiceImpl implements SchoolService {

    private static final String DEFAULT_SCORE_PROVINCE = "河南";
    private static final String DEFAULT_SUBJECT_TYPE = "science";

    private static final List<String> FALLBACK_SCORE_PROVINCES = List.of(
            "河南", "北京", "天津", "河北", "山西", "辽宁", "吉林", "黑龙江",
            "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "湖北", "湖南", "广东", "广西", "海南",
            "重庆", "四川", "贵州", "云南", "陕西"
    );

    private final SchoolInfoMapper schoolInfoMapper;
    private final AdmissionScoreMapper admissionScoreMapper;
    private final AdmissionScoreHelper admissionScoreHelper;
    private final CityProvinceResolver cityProvinceResolver;
    private final CityGeoResolver cityGeoResolver;

    public SchoolServiceImpl(
            SchoolInfoMapper schoolInfoMapper,
            AdmissionScoreMapper admissionScoreMapper,
            AdmissionScoreHelper admissionScoreHelper,
            CityProvinceResolver cityProvinceResolver,
            CityGeoResolver cityGeoResolver) {
        this.schoolInfoMapper = schoolInfoMapper;
        this.admissionScoreMapper = admissionScoreMapper;
        this.admissionScoreHelper = admissionScoreHelper;
        this.cityProvinceResolver = cityProvinceResolver;
        this.cityGeoResolver = cityGeoResolver;
    }

    @Override
    public PageResult<SchoolListItemVO> pageSchools(
            String keyword,
            String location,
            Boolean is985,
            Boolean is211,
            String scoreProvince,
            String subjectType,
            int page,
            int size) {

        String province = normalizeProvince(scoreProvince);
        String subject = normalizeSubject(subjectType);

        LambdaQueryWrapper<SchoolInfo> wrapper = new LambdaQueryWrapper<SchoolInfo>()
                .eq(SchoolInfo::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> {
                w.like(SchoolInfo::getSchoolName, keyword)
                        .or()
                        .like(SchoolInfo::getSchoolCode, keyword)
                        .or()
                        .like(SchoolInfo::getCity, keyword);
                if (ProvinceOrder.isProvince(keyword)) {
                    w.or().in(SchoolInfo::getLocation,
                            cityProvinceResolver.locationValuesForProvince(keyword));
                }
            });
        }
        if (StringUtils.hasText(location)) {
            applyProvinceFilter(wrapper, location);
        }
        if (Boolean.TRUE.equals(is985)) {
            wrapper.eq(SchoolInfo::getIs985, 1);
        }
        if (Boolean.TRUE.equals(is211)) {
            wrapper.eq(SchoolInfo::getIs211, 1);
        }

        int safePage = PageQueryUtil.safePage(page);
        int safeSize = PageQueryUtil.safeSize(size);
        long total = schoolInfoMapper.selectCount(wrapper);

        wrapper.orderByDesc(SchoolInfo::getIs985)
                .orderByDesc(SchoolInfo::getIs211)
                .orderByAsc(SchoolInfo::getSchoolName)
                .last(PageQueryUtil.mysqlLimit(safePage, safeSize));

        List<SchoolListItemVO> records = schoolInfoMapper.selectList(wrapper).stream()
                .map(s -> toListItem(s, province, subject))
                .collect(Collectors.toList());

        return new PageResult<>(total, safePage, safeSize, records);
    }

    @Override
    public SchoolDetailVO getDetail(String schoolCode, String scoreProvince, String subjectType) {
        SchoolInfo school = findByCode(schoolCode);
        String province = normalizeProvince(scoreProvince);
        String subject = normalizeSubject(subjectType);

        SchoolDetailVO vo = toDetail(school);
        vo.setAdmissionScores(admissionScoreHelper.listSchoolScores(
                schoolCode, province, subject, null, true));
        return vo;
    }

    @Override
    public List<SchoolCompareItemVO> compare(
            List<String> schoolCodes,
            String scoreProvince,
            String subjectType,
            List<Integer> years) {

        if (schoolCodes == null || schoolCodes.isEmpty()) {
            return List.of();
        }
        if (schoolCodes.size() > 4) {
            schoolCodes = schoolCodes.subList(0, 4);
        }

        String province = normalizeProvince(scoreProvince);
        String subject = normalizeSubject(subjectType);
        List<Integer> yearList = years == null || years.isEmpty()
                ? List.of(2025, 2024, 2023, 2022, 2021)
                : years;

        List<SchoolCompareItemVO> result = new ArrayList<>();
        for (String code : schoolCodes) {
            SchoolInfo school = findByCode(code.trim());
            SchoolCompareItemVO item = new SchoolCompareItemVO();
            item.setSchoolCode(school.getSchoolCode());
            item.setSchoolName(school.getSchoolName());
            item.setLocation(resolveSchoolProvince(school));
            item.setCity(school.getCity());
            item.setSchoolType(school.getSchoolType());
            item.setIs985(school.getIs985() != null && school.getIs985() == 1);
            item.setIs211(school.getIs211() != null && school.getIs211() == 1);
            item.setIsDoubleFirst(school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1);
            item.setDescription(truncate(school.getDescription(), 200));
            List<AdmissionScoreVO> scores = admissionScoreHelper.listSchoolScores(
                    code, province, subject, yearList, true);
            item.setScores(scores);
            if (!scores.isEmpty()) {
                AdmissionScoreVO latest = scores.get(0);
                item.setLatestYear(latest.getYear());
                item.setLatestMinScore(latest.getMinScore());
                item.setLatestMinRank(latest.getMinRank());
                item.setLatestOverLineScore(latest.getOverLineScore());
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<String> listLocations() {
        Set<String> provinces = new LinkedHashSet<>();
        for (String loc : schoolInfoMapper.selectDistinctLocations()) {
            provinces.add(cityProvinceResolver.resolveProvince(loc, null));
        }
        provinces.remove("未知");
        return ProvinceOrder.sort(new ArrayList<>(provinces));
    }

    @Override
    public SchoolMapMarkersVO listMapMarkers(
            String keyword,
            String location,
            Boolean is985,
            Boolean is211,
            String scoreProvince,
            String subjectType,
            int limit) {

        int size = Math.min(Math.max(limit, 1), 800);
        String province = normalizeProvince(scoreProvince);
        String subject = normalizeSubject(subjectType);

        LambdaQueryWrapper<SchoolInfo> wrapper = new LambdaQueryWrapper<SchoolInfo>()
                .eq(SchoolInfo::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SchoolInfo::getSchoolName, keyword)
                    .or().like(SchoolInfo::getCity, keyword)
                    .or().like(SchoolInfo::getLocation, keyword));
        }
        if (StringUtils.hasText(location)) {
            applyProvinceFilter(wrapper, location);
        }
        if (Boolean.TRUE.equals(is985)) {
            wrapper.eq(SchoolInfo::getIs985, 1);
        }
        if (Boolean.TRUE.equals(is211)) {
            wrapper.eq(SchoolInfo::getIs211, 1);
        }

        wrapper.orderByDesc(SchoolInfo::getIs985)
                .orderByDesc(SchoolInfo::getIs211)
                .last("LIMIT " + size);

        List<SchoolInfo> schools = schoolInfoMapper.selectList(wrapper);
        Map<String, Integer> cityCounter = new HashMap<>();
        List<SchoolMapMarkerVO> markers = new ArrayList<>();
        int skipped = 0;

        for (SchoolInfo school : schools) {
            String schoolProvince = resolveSchoolProvince(school);
            double[] base = cityGeoResolver.resolve(
                    school.getLocation(), school.getCity(), schoolProvince);
            if (base == null) {
                skipped++;
                continue;
            }
            String cityKey = schoolProvince + "|" + (StringUtils.hasText(school.getCity())
                    ? school.getCity() : school.getLocation());
            int idx = cityCounter.merge(cityKey, 1, Integer::sum) - 1;
            double[] coords = cityGeoResolver.offset(base, idx);

            SchoolMapMarkerVO m = new SchoolMapMarkerVO();
            m.setSchoolCode(school.getSchoolCode());
            m.setSchoolName(school.getSchoolName());
            m.setProvince(schoolProvince);
            m.setCity(school.getCity());
            m.setLocation(school.getLocation());
            m.setLat(coords[0]);
            m.setLng(coords[1]);
            m.setIs985(school.getIs985() != null && school.getIs985() == 1);
            m.setIs211(school.getIs211() != null && school.getIs211() == 1);
            m.setIsDoubleFirst(school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1);
            m.setLatestMinScore(admissionScoreHelper.findLatestMinScore(
                    school.getSchoolCode(), province, subject));
            m.setLatestMinRank(admissionScoreHelper.findLatestMinRank(
                    school.getSchoolCode(), province, subject));
            markers.add(m);
        }

        SchoolMapMarkersVO result = new SchoolMapMarkersVO();
        result.setMarkers(markers);
        result.setTotalQueried(schools.size());
        result.setMappedCount(markers.size());
        result.setSkippedNoCoords(skipped);
        return result;
    }

    @Override
    public List<String> listScoreProvinces() {
        List<String> fromDb = admissionScoreMapper.selectDistinctProvinces();
        if (fromDb != null && !fromDb.isEmpty()) {
            return ProvinceOrder.sort(fromDb);
        }
        return FALLBACK_SCORE_PROVINCES;
    }

    @Override
    public List<SchoolListItemVO> listOptions(
            String keyword,
            String codes,
            String location,
            String scoreProvince,
            String subjectType,
            int limit) {
        int size = Math.min(Math.max(limit, 1), 100);
        LambdaQueryWrapper<SchoolInfo> wrapper = new LambdaQueryWrapper<SchoolInfo>()
                .eq(SchoolInfo::getStatus, 1);

        if (StringUtils.hasText(codes)) {
            List<String> codeList = java.util.Arrays.stream(codes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (!codeList.isEmpty()) {
                wrapper.in(SchoolInfo::getSchoolCode, codeList);
            }
        } else {
            if (StringUtils.hasText(location)) {
                applyProvinceFilter(wrapper, location);
            }
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> {
                    w.like(SchoolInfo::getSchoolName, keyword)
                            .or()
                            .like(SchoolInfo::getCity, keyword)
                            .or()
                            .like(SchoolInfo::getLocation, keyword);
                    if (ProvinceOrder.isProvince(keyword)) {
                        w.or().in(SchoolInfo::getLocation,
                                cityProvinceResolver.locationValuesForProvince(keyword));
                    }
                });
            }
        }

        wrapper.orderByDesc(SchoolInfo::getIs985)
                .orderByDesc(SchoolInfo::getIs211)
                .last("LIMIT " + size);

        String province = normalizeProvince(scoreProvince);
        String subject = normalizeSubject(subjectType);
        return schoolInfoMapper.selectList(wrapper).stream()
                .map(s -> toListItem(s, province, subject))
                .collect(Collectors.toList());
    }

    private SchoolInfo findByCode(String schoolCode) {
        SchoolInfo school = schoolInfoMapper.selectOne(
                new LambdaQueryWrapper<SchoolInfo>()
                        .eq(SchoolInfo::getSchoolCode, schoolCode)
                        .eq(SchoolInfo::getStatus, 1));
        if (school == null) {
            throw new IllegalArgumentException("院校不存在: " + schoolCode);
        }
        return school;
    }

    private SchoolListItemVO toListItem(SchoolInfo school, String province, String subject) {
        SchoolListItemVO vo = new SchoolListItemVO();
        vo.setSchoolCode(school.getSchoolCode());
        vo.setSchoolName(school.getSchoolName());
        vo.setSchoolType(school.getSchoolType());
        vo.setLocation(resolveSchoolProvince(school));
        vo.setCity(school.getCity());
        vo.setIs985(school.getIs985() != null && school.getIs985() == 1);
        vo.setIs211(school.getIs211() != null && school.getIs211() == 1);
        vo.setIsDoubleFirst(school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1);
        vo.setLatestMinScore(admissionScoreHelper.findLatestMinScore(school.getSchoolCode(), province, subject));
        vo.setLatestMinRank(admissionScoreHelper.findLatestMinRank(school.getSchoolCode(), province, subject));
        return vo;
    }

    private SchoolDetailVO toDetail(SchoolInfo school) {
        SchoolDetailVO vo = new SchoolDetailVO();
        vo.setSchoolCode(school.getSchoolCode());
        vo.setSchoolName(school.getSchoolName());
        vo.setSchoolType(school.getSchoolType());
        vo.setSchoolNature(school.getSchoolNature());
        vo.setLocation(resolveSchoolProvince(school));
        vo.setCity(school.getCity());
        vo.setWebsite(school.getWebsite());
        vo.setDescription(school.getDescription());
        vo.setIs985(school.getIs985() != null && school.getIs985() == 1);
        vo.setIs211(school.getIs211() != null && school.getIs211() == 1);
        vo.setIsDoubleFirst(school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1);
        vo.setFoundedYear(school.getFoundedYear());
        return vo;
    }

    private void applyProvinceFilter(LambdaQueryWrapper<SchoolInfo> wrapper, String province) {
        List<String> locationValues = cityProvinceResolver.locationValuesForProvince(province);
        if (locationValues.isEmpty()) {
            wrapper.eq(SchoolInfo::getLocation, province);
        } else {
            wrapper.in(SchoolInfo::getLocation, locationValues);
        }
    }

    private String resolveSchoolProvince(SchoolInfo school) {
        return cityProvinceResolver.resolveProvince(school.getLocation(), school.getCity());
    }

    private String normalizeProvince(String province) {
        return StringUtils.hasText(province) ? province : DEFAULT_SCORE_PROVINCE;
    }

    private String normalizeSubject(String subjectType) {
        return StringUtils.hasText(subjectType) ? subjectType : DEFAULT_SUBJECT_TYPE;
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
