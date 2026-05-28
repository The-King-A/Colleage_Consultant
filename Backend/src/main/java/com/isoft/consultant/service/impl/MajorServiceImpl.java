package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.common.PageQueryUtil;
import com.isoft.consultant.dto.EmploymentTrendVO;
import com.isoft.consultant.dto.MajorCompareItemVO;
import com.isoft.consultant.dto.MajorDetailVO;
import com.isoft.consultant.dto.MajorHotTrendVO;
import com.isoft.consultant.dto.MajorListItemVO;
import com.isoft.consultant.dto.PageResult;
import com.isoft.consultant.entity.EmploymentTrend;
import com.isoft.consultant.entity.MajorHotRank;
import com.isoft.consultant.entity.MajorInfo;
import com.isoft.consultant.mapper.EmploymentTrendMapper;
import com.isoft.consultant.mapper.MajorHotRankMapper;
import com.isoft.consultant.mapper.MajorInfoMapper;
import com.isoft.consultant.service.MajorService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MajorServiceImpl implements MajorService {

    private static final List<Integer> TREND_YEARS = List.of(2021, 2022, 2023, 2024, 2025);

    private final MajorInfoMapper majorInfoMapper;
    private final MajorHotRankMapper majorHotRankMapper;
    private final EmploymentTrendMapper employmentTrendMapper;

    public MajorServiceImpl(
            MajorInfoMapper majorInfoMapper,
            MajorHotRankMapper majorHotRankMapper,
            EmploymentTrendMapper employmentTrendMapper) {
        this.majorInfoMapper = majorInfoMapper;
        this.majorHotRankMapper = majorHotRankMapper;
        this.employmentTrendMapper = employmentTrendMapper;
    }

    @Override
    public PageResult<MajorListItemVO> pageMajors(String keyword, String category, int page, int size) {
        LambdaQueryWrapper<MajorInfo> wrapper = new LambdaQueryWrapper<MajorInfo>()
                .eq(MajorInfo::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MajorInfo::getMajorName, keyword)
                    .or()
                    .like(MajorInfo::getMajorCode, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(MajorInfo::getMajorCategory, category);
        }

        int safePage = PageQueryUtil.safePage(page);
        int safeSize = PageQueryUtil.safeSize(size);
        long total = majorInfoMapper.selectCount(wrapper);

        wrapper.orderByDesc(MajorInfo::getHotIndex)
                .last(PageQueryUtil.mysqlLimit(safePage, safeSize));

        List<MajorListItemVO> records = majorInfoMapper.selectList(wrapper).stream()
                .map(this::toListItem)
                .collect(Collectors.toList());

        return new PageResult<>(total, safePage, safeSize, records);
    }

    @Override
    public MajorDetailVO getDetail(String majorCode) {
        MajorInfo major = majorInfoMapper.selectOne(
                new LambdaQueryWrapper<MajorInfo>()
                        .eq(MajorInfo::getMajorCode, majorCode)
                        .eq(MajorInfo::getStatus, 1));
        if (major == null) {
            throw new IllegalArgumentException("专业不存在: " + majorCode);
        }
        return toDetail(major);
    }

    @Override
    public List<MajorCompareItemVO> compare(List<String> majorCodes) {
        if (majorCodes == null || majorCodes.isEmpty()) {
            return List.of();
        }
        if (majorCodes.size() > 4) {
            majorCodes = majorCodes.subList(0, 4);
        }

        List<MajorCompareItemVO> result = new ArrayList<>();
        for (String code : majorCodes) {
            MajorInfo major = majorInfoMapper.selectOne(
                    new LambdaQueryWrapper<MajorInfo>()
                            .eq(MajorInfo::getMajorCode, code.trim())
                            .eq(MajorInfo::getStatus, 1));
            if (major == null) {
                continue;
            }
            result.add(buildCompareItem(major));
        }
        return result;
    }

    @Override
    public List<String> listCategories() {
        return majorInfoMapper.selectDistinctCategories();
    }

    @Override
    public List<MajorListItemVO> listOptions(String keyword, String codes, int limit) {
        int size = Math.min(Math.max(limit, 1), 200);
        LambdaQueryWrapper<MajorInfo> wrapper = new LambdaQueryWrapper<MajorInfo>()
                .eq(MajorInfo::getStatus, 1);

        if (StringUtils.hasText(codes)) {
            List<String> codeList = Arrays.stream(codes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (!codeList.isEmpty()) {
                wrapper.in(MajorInfo::getMajorCode, codeList);
            }
        } else if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MajorInfo::getMajorName, keyword)
                    .or()
                    .like(MajorInfo::getMajorCode, keyword)
                    .or()
                    .like(MajorInfo::getMajorCategory, keyword));
        }

        wrapper.orderByDesc(MajorInfo::getHotIndex).last("LIMIT " + size);
        return majorInfoMapper.selectList(wrapper).stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }

    private MajorCompareItemVO buildCompareItem(MajorInfo major) {
        MajorCompareItemVO vo = new MajorCompareItemVO();
        vo.setMajorCode(major.getMajorCode());
        vo.setMajorName(major.getMajorName());
        vo.setMajorCategory(major.getMajorCategory());
        vo.setMajorSubcategory(major.getMajorSubcategory());
        vo.setHotIndex(major.getHotIndex());
        vo.setEmploymentRate(major.getEmploymentRate());
        vo.setSalaryAvg(major.getSalaryAvg());
        vo.setSalary5year(major.getSalary5year());
        vo.setDegreeType(major.getDegreeType());
        vo.setStudyDuration(major.getStudyDuration());
        vo.setDifficultyLevel(major.getDifficultyLevel());
        vo.setDescription(major.getDescription());
        vo.setCareerDirection(major.getCareerDirection());
        vo.setGraduateDestinations(major.getGraduateDestinations());
        vo.setTypicalEmployers(splitTags(major.getTypicalEmployers()));
        vo.setTypicalCareers(splitTags(major.getTypicalCareers()));
        vo.setCourseList(major.getCourseList());
        vo.setHotTrends(loadHotTrends(major.getMajorCode()));
        vo.setEmploymentTrends(loadEmploymentTrends(major));
        return vo;
    }

    private List<MajorHotTrendVO> loadHotTrends(String majorCode) {
        List<MajorHotRank> rows = majorHotRankMapper.selectList(
                new LambdaQueryWrapper<MajorHotRank>()
                        .eq(MajorHotRank::getMajorCode, majorCode)
                        .in(MajorHotRank::getYear, TREND_YEARS)
                        .orderByAsc(MajorHotRank::getYear));
        return rows.stream().map(r -> {
            MajorHotTrendVO vo = new MajorHotTrendVO();
            vo.setYear(r.getYear());
            vo.setHotScore(r.getHotScore());
            vo.setRank(r.getRankValue());
            vo.setSearchVolume(r.getSearchVolume());
            vo.setApplyGrowth(r.getApplyGrowth());
            return vo;
        }).collect(Collectors.toList());
    }

    private List<EmploymentTrendVO> loadEmploymentTrends(MajorInfo major) {
        List<String> categories = List.of(
                major.getMajorSubcategory(),
                major.getMajorCategory(),
                "工学"
        );
        for (String cat : categories) {
            if (!StringUtils.hasText(cat)) {
                continue;
            }
            List<EmploymentTrend> rows = employmentTrendMapper.selectList(
                    new LambdaQueryWrapper<EmploymentTrend>()
                            .eq(EmploymentTrend::getMajorCategory, cat)
                            .in(EmploymentTrend::getYear, TREND_YEARS)
                            .orderByAsc(EmploymentTrend::getYear));
            if (!rows.isEmpty()) {
                return rows.stream().map(r -> {
                    EmploymentTrendVO vo = new EmploymentTrendVO();
                    vo.setYear(r.getYear());
                    vo.setEmploymentRate(r.getEmploymentRate());
                    vo.setAvgSalary(r.getAvgSalary());
                    vo.setJobOpenings(r.getJobOpenings());
                    vo.setGrowthRate(r.getGrowthRate());
                    vo.setTrendDirection(r.getTrendDirection());
                    return vo;
                }).collect(Collectors.toList());
            }
        }
        return List.of();
    }

    private MajorListItemVO toListItem(MajorInfo major) {
        MajorListItemVO vo = new MajorListItemVO();
        vo.setMajorCode(major.getMajorCode());
        vo.setMajorName(major.getMajorName());
        vo.setMajorCategory(major.getMajorCategory());
        vo.setMajorSubcategory(major.getMajorSubcategory());
        vo.setHotIndex(major.getHotIndex());
        vo.setEmploymentRate(major.getEmploymentRate());
        vo.setSalaryAvg(major.getSalaryAvg());
        return vo;
    }

    private MajorDetailVO toDetail(MajorInfo major) {
        MajorDetailVO vo = new MajorDetailVO();
        vo.setMajorCode(major.getMajorCode());
        vo.setMajorName(major.getMajorName());
        vo.setMajorCategory(major.getMajorCategory());
        vo.setMajorSubcategory(major.getMajorSubcategory());
        vo.setDegreeType(major.getDegreeType());
        vo.setStudyDuration(major.getStudyDuration());
        vo.setDescription(major.getDescription());
        vo.setCourseList(major.getCourseList());
        vo.setCareerDirection(major.getCareerDirection());
        vo.setGraduateDestinations(major.getGraduateDestinations());
        vo.setTypicalEmployers(splitTags(major.getTypicalEmployers()));
        vo.setTypicalCareers(splitTags(major.getTypicalCareers()));
        vo.setDifficultyLevel(major.getDifficultyLevel());
        vo.setHotIndex(major.getHotIndex());
        vo.setEmploymentRate(major.getEmploymentRate());
        vo.setSalaryAvg(major.getSalaryAvg());
        vo.setSalary5year(major.getSalary5year());
        return vo;
    }

    private List<String> splitTags(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split("[、,，;；|]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
