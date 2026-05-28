package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.dto.DashboardStatsVO;
import com.isoft.consultant.entity.MajorInfo;
import com.isoft.consultant.entity.SchoolInfo;
import com.isoft.consultant.mapper.AdmissionScoreMapper;
import com.isoft.consultant.mapper.MajorInfoMapper;
import com.isoft.consultant.mapper.SchoolInfoMapper;
import com.isoft.consultant.service.StatsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {

    private final SchoolInfoMapper schoolInfoMapper;
    private final MajorInfoMapper majorInfoMapper;
    private final AdmissionScoreMapper admissionScoreMapper;
    private final JdbcTemplate jdbcTemplate;

    public StatsServiceImpl(
            SchoolInfoMapper schoolInfoMapper,
            MajorInfoMapper majorInfoMapper,
            AdmissionScoreMapper admissionScoreMapper,
            JdbcTemplate jdbcTemplate) {
        this.schoolInfoMapper = schoolInfoMapper;
        this.majorInfoMapper = majorInfoMapper;
        this.admissionScoreMapper = admissionScoreMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setSchoolCount(schoolInfoMapper.selectCount(
                new LambdaQueryWrapper<SchoolInfo>().eq(SchoolInfo::getStatus, 1)));
        vo.setMajorCount(majorInfoMapper.selectCount(
                new LambdaQueryWrapper<MajorInfo>().eq(MajorInfo::getStatus, 1)));
        vo.setAdmissionRecordCount(admissionScoreMapper.selectCount(null));
        vo.setProvinceWithScoreCount(countDistinctProvinces());

        List<Map<String, Object>> hot = jdbcTemplate.queryForList(
                "SELECT major_name AS name, MAX(hot_score) AS hotScore "
                        + "FROM major_hot_rank WHERE year = 2024 GROUP BY major_name "
                        + "ORDER BY hotScore DESC LIMIT 8");
        vo.setTopHotMajors(hot);

        List<Map<String, Object>> byProv = jdbcTemplate.queryForList(
                "SELECT location AS province, COUNT(*) AS cnt FROM school_info "
                        + "WHERE status = 1 AND location IS NOT NULL "
                        + "GROUP BY location ORDER BY cnt DESC LIMIT 12");
        vo.setSchoolsByProvince(byProv);

        return vo;
    }

    private long countDistinctProvinces() {
        Long n = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT province) FROM admission_score", Long.class);
        return n != null ? n : 0;
    }
}
