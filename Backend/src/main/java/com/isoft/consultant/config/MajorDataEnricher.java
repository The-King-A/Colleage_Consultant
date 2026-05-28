package com.isoft.consultant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.entity.EmploymentTrend;
import com.isoft.consultant.entity.MajorHotRank;
import com.isoft.consultant.entity.MajorInfo;
import com.isoft.consultant.mapper.EmploymentTrendMapper;
import com.isoft.consultant.mapper.MajorHotRankMapper;
import com.isoft.consultant.mapper.MajorInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 扩充专业库、近五年热度与门类就业趋势（样本数据）。
 */
@Component
@Order(21)
public class MajorDataEnricher implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MajorDataEnricher.class);
    private static final List<Integer> YEARS = List.of(2021, 2022, 2023, 2024, 2025);

    private final MajorInfoMapper majorInfoMapper;
    private final MajorHotRankMapper majorHotRankMapper;
    private final EmploymentTrendMapper employmentTrendMapper;

    public MajorDataEnricher(
            MajorInfoMapper majorInfoMapper,
            MajorHotRankMapper majorHotRankMapper,
            EmploymentTrendMapper employmentTrendMapper) {
        this.majorInfoMapper = majorInfoMapper;
        this.majorHotRankMapper = majorHotRankMapper;
        this.employmentTrendMapper = employmentTrendMapper;
    }

    @Override
    public void run(String... args) {
        try {
            int majors = seedMajors();
            int hot = seedHotRanks();
            int emp = seedEmploymentTrends();
            if (majors + hot + emp > 0) {
                log.info("专业数据扩充：新增专业 {} 个，热度记录 {} 条，就业趋势 {} 条", majors, hot, emp);
            }
        } catch (Exception e) {
            log.warn("专业数据扩充失败: {}", e.getMessage());
        }
    }

    private int seedMajors() {
        String[][] rows = {
                {"081001", "土木工程", "工学", "土木类", "工学学士", "85", "94.5", "11000"},
                {"082502", "环境工程", "工学", "环境科学与工程类", "工学学士", "78", "93.0", "10500"},
                {"080601", "电气工程及其自动化", "工学", "电气类", "工学学士", "88", "96.0", "13000"},
                {"080701", "电子信息工程", "工学", "电子信息类", "工学学士", "90", "94.0", "14000"},
                {"080702", "通信工程", "工学", "电子信息类", "工学学士", "86", "93.5", "13500"},
                {"080703", "数据科学与大数据技术", "工学", "计算机类", "工学学士", "95", "93.0", "15500"},
                {"080704", "微电子科学与工程", "工学", "电子信息类", "工学学士", "89", "92.5", "14500"},
                {"080705", "光电信息科学与工程", "工学", "电子信息类", "工学学士", "82", "92.0", "12500"},
                {"080206", "机械工程", "工学", "机械类", "工学学士", "80", "95.5", "11500"},
                {"080202", "机械设计制造及其自动化", "工学", "机械类", "工学学士", "83", "95.0", "11800"},
                {"120201", "工商管理", "管理学", "工商管理类", "管理学学士", "84", "91.0", "10000"},
                {"120202", "市场营销", "管理学", "工商管理类", "管理学学士", "79", "90.5", "9500"},
                {"120203K", "会计学", "管理学", "工商管理类", "管理学学士", "82", "93.2", "10000"},
                {"120204", "财务管理", "管理学", "工商管理类", "管理学学士", "81", "92.8", "9800"},
                {"120801", "电子商务", "管理学", "电子商务类", "管理学学士", "87", "91.5", "12000"},
                {"020302", "金融工程", "经济学", "金融学类", "经济学学士", "86", "90.0", "13500"},
                {"020401", "国际经济与贸易", "经济学", "经济与贸易类", "经济学学士", "75", "88.5", "10500"},
                {"030302", "社会工作", "法学", "社会学类", "法学学士", "72", "87.0", "9000"},
                {"040101", "教育学", "教育学", "教育学类", "教育学学士", "76", "92.0", "8800"},
                {"040201", "体育教育", "教育学", "体育学类", "教育学学士", "70", "89.0", "8500"},
                {"050201", "英语", "文学", "外国语言文学类", "文学学士", "78", "88.0", "9500"},
                {"050207", "日语", "文学", "外国语言文学类", "文学学士", "68", "86.5", "9000"},
                {"070101", "数学与应用数学", "理学", "数学类", "理学学士", "74", "90.0", "10200"},
                {"070102", "信息与计算科学", "理学", "数学类", "理学学士", "80", "91.5", "12500"},
                {"071001", "生物科学", "理学", "生物科学类", "理学学士", "72", "88.0", "9500"},
                {"071002", "生物技术", "理学", "生物科学类", "理学学士", "75", "89.5", "9800"},
                {"080401", "材料科学与工程", "工学", "材料类", "工学学士", "77", "92.0", "11000"},
                {"080910T", "网络空间安全", "工学", "计算机类", "工学学士", "94", "94.5", "16500"},
                {"080911TK", "保密技术", "工学", "计算机类", "工学学士", "83", "93.0", "14000"},
                {"081002", "建筑学", "工学", "建筑类", "工学学士", "81", "93.5", "11500"},
                {"082801", "建筑环境与能源应用工程", "工学", "土木类", "工学学士", "76", "94.0", "10800"},
                {"100701", "药学", "医学", "药学类", "医学学士", "80", "92.5", "10500"},
                {"101101", "护理学", "医学", "护理学类", "医学学士", "78", "97.0", "9500"},
                {"120601", "物流管理", "管理学", "物流管理与工程类", "管理学学士", "77", "91.0", "9800"},
                {"130508", "数字媒体艺术", "艺术学", "设计学类", "艺术学学士", "88", "88.5", "11500"},
                {"080803T", "机器人工程", "工学", "自动化类", "工学学士", "92", "94.0", "15000"},
                {"080213T", "智能制造工程", "工学", "机械类", "工学学士", "90", "95.0", "13800"},
                {"020101", "经济学", "经济学", "经济学类", "经济学学士", "79", "89.0", "11200"},
                {"050303", "广告学", "文学", "新闻传播学类", "文学学士", "81", "87.5", "10500"},
        };

        int inserted = 0;
        for (String[] r : rows) {
            if (majorInfoMapper.selectCount(new LambdaQueryWrapper<MajorInfo>()
                    .eq(MajorInfo::getMajorCode, r[0])) > 0) {
                continue;
            }
            MajorInfo m = new MajorInfo();
            m.setMajorCode(r[0]);
            m.setMajorName(r[1]);
            m.setMajorCategory(r[2]);
            m.setMajorSubcategory(r[3]);
            m.setDegreeType(r[4]);
            m.setStudyDuration(4);
            m.setHotIndex(Integer.parseInt(r[5]));
            m.setEmploymentRate(new BigDecimal(r[6]));
            m.setSalaryAvg(Integer.parseInt(r[7]));
            m.setSalary5year(Integer.parseInt(r[7]) + 2500);
            m.setDifficultyLevel("medium");
            m.setDescription(r[1] + "专业培养具备扎实理论基础与实践能力的人才，毕业生可在相关行业从事技术、管理等工作。");
            m.setCareerDirection("企事业单位、科研院所、政府部门等");
            m.setStatus(1);
            majorInfoMapper.insert(m);
            inserted++;
        }
        return inserted;
    }

    private int seedHotRanks() {
        List<MajorInfo> majors = majorInfoMapper.selectList(
                new LambdaQueryWrapper<MajorInfo>().eq(MajorInfo::getStatus, 1));
        int inserted = 0;
        for (MajorInfo major : majors) {
            int baseHot = major.getHotIndex() != null ? major.getHotIndex() : 75;
            for (int i = 0; i < YEARS.size(); i++) {
                int year = YEARS.get(i);
                if (majorHotRankMapper.selectCount(new LambdaQueryWrapper<MajorHotRank>()
                        .eq(MajorHotRank::getMajorCode, major.getMajorCode())
                        .eq(MajorHotRank::getYear, year)) > 0) {
                    continue;
                }
                MajorHotRank row = new MajorHotRank();
                row.setMajorCode(major.getMajorCode());
                row.setMajorName(major.getMajorName());
                row.setYear(year);
                int hot = baseHot - (2023 - year) + i;
                row.setHotScore(BigDecimal.valueOf(Math.min(99, Math.max(60, hot))));
                row.setRankValue(100 - hot + (year - 2021));
                row.setSearchVolume(800000 + hot * 15000 + year * 20000);
                row.setApplyGrowth(BigDecimal.valueOf(5 + i * 2.5));
                majorHotRankMapper.insert(row);
                inserted++;
            }
        }
        return inserted;
    }

    private int seedEmploymentTrends() {
        String[] categories = {
                "工学", "计算机类", "电子信息类", "机械类", "电气类", "土木类", "材料类",
                "管理学", "工商管理类", "经济学", "金融学类", "法学", "医学", "临床医学类",
                "教育学", "文学", "理学", "艺术学", "自动化类"
        };
        int inserted = 0;
        for (String cat : categories) {
            int baseSalary = cat.contains("计算机") || cat.contains("电子信息") ? 15000
                    : cat.contains("医学") ? 12000 : cat.contains("金融") ? 13500 : 11000;
            for (int i = 0; i < YEARS.size(); i++) {
                int year = YEARS.get(i);
                if (employmentTrendMapper.selectCount(new LambdaQueryWrapper<EmploymentTrend>()
                        .eq(EmploymentTrend::getMajorCategory, cat)
                        .eq(EmploymentTrend::getYear, year)) > 0) {
                    continue;
                }
                EmploymentTrend t = new EmploymentTrend();
                t.setMajorCategory(cat);
                t.setYear(year);
                t.setEmploymentRate(BigDecimal.valueOf(88 + i * 1.2).setScale(1, RoundingMode.HALF_UP));
                t.setAvgSalary(baseSalary + i * 400);
                t.setJobOpenings(40 + i * 8);
                t.setGrowthRate(BigDecimal.valueOf(6 + i * 1.5));
                t.setTrendDirection("up");
                employmentTrendMapper.insert(t);
                inserted++;
            }
        }
        return inserted;
    }
}
