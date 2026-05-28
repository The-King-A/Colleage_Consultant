package com.isoft.consultant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.entity.MajorInfo;
import com.isoft.consultant.mapper.MajorInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 为专业库补充详细介绍、毕业生去向、典型单位与职业岗位（样本数据）。
 */
@Component
@Order(23)
public class MajorCareerEnricher implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MajorCareerEnricher.class);

    private final MajorInfoMapper majorInfoMapper;
    private final MajorInfoSchemaMigration majorInfoSchemaMigration;

    public MajorCareerEnricher(
            MajorInfoMapper majorInfoMapper,
            MajorInfoSchemaMigration majorInfoSchemaMigration) {
        this.majorInfoMapper = majorInfoMapper;
        this.majorInfoSchemaMigration = majorInfoSchemaMigration;
    }

    @Override
    public void run(String... args) {
        try {
            majorInfoSchemaMigration.ensureCareerColumns();
            int updated = enrichCareerProfiles();
            if (updated > 0) {
                log.info("专业就业去向信息已更新 {} 条", updated);
            }
        } catch (Exception e) {
            log.warn("专业就业去向扩充失败: {}", e.getMessage());
        }
    }

    private int enrichCareerProfiles() {
        List<MajorInfo> majors = majorInfoMapper.selectList(
                new LambdaQueryWrapper<MajorInfo>().eq(MajorInfo::getStatus, 1));
        int updated = 0;
        for (MajorInfo major : majors) {
            if (isRichProfile(major)) {
                continue;
            }
            MajorCareerProfileData.Profile profile = MajorCareerProfileData.resolve(
                    major.getMajorCode(),
                    major.getMajorName(),
                    major.getMajorCategory(),
                    major.getMajorSubcategory());
            major.setDescription(profile.description());
            if (!StringUtils.hasText(major.getCourseList())) {
                major.setCourseList(profile.courseList());
            }
            major.setCareerDirection(profile.careerDirection());
            major.setGraduateDestinations(profile.graduateDestinations());
            major.setTypicalEmployers(profile.typicalEmployers());
            major.setTypicalCareers(profile.typicalCareers());
            majorInfoMapper.updateById(major);
            updated++;
        }
        return updated;
    }

    private boolean isRichProfile(MajorInfo major) {
        return StringUtils.hasText(major.getGraduateDestinations())
                && major.getGraduateDestinations().length() > 40
                && StringUtils.hasText(major.getTypicalEmployers())
                && StringUtils.hasText(major.getTypicalCareers());
    }
}
