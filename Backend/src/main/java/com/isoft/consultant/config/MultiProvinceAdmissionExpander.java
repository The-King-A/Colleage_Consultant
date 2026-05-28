package com.isoft.consultant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.entity.AdmissionScore;
import com.isoft.consultant.entity.ProvinceScoreLine;
import com.isoft.consultant.mapper.AdmissionScoreMapper;
import com.isoft.consultant.mapper.ProvinceScoreLineMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 以河南省为基准，向其他生源省份复制近五年院校级录取样本数据。
 */
@Component
@Order(22)
public class MultiProvinceAdmissionExpander implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MultiProvinceAdmissionExpander.class);
    private static final String BASE_PROVINCE = "河南";
    private static final List<Integer> YEARS = List.of(2021, 2022, 2023, 2024, 2025);
    private static final String DATA_SOURCE = "平台样本数据（多省份扩展）";

    /** 相对河南省理科院校录取线的分数调整（样本） */
    private static final Map<String, Integer> SCIENCE_SCORE_OFFSET = Map.ofEntries(
            Map.entry("北京", 12),
            Map.entry("天津", 10),
            Map.entry("上海", 14),
            Map.entry("浙江", 8),
            Map.entry("江苏", 6),
            Map.entry("山东", 2),
            Map.entry("广东", 5),
            Map.entry("福建", 4),
            Map.entry("湖北", 1),
            Map.entry("湖南", 0),
            Map.entry("安徽", 1),
            Map.entry("江西", 2),
            Map.entry("河北", 3),
            Map.entry("山西", 4),
            Map.entry("陕西", -2),
            Map.entry("四川", 3),
            Map.entry("重庆", 2),
            Map.entry("辽宁", 5),
            Map.entry("吉林", 6),
            Map.entry("黑龙江", 7),
            Map.entry("广西", 4),
            Map.entry("云南", 5),
            Map.entry("贵州", 6),
            Map.entry("海南", 3)
    );

    private static final List<String> TARGET_PROVINCES = List.of(
            "北京", "天津", "河北", "山西", "辽宁", "吉林", "黑龙江",
            "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "湖北", "湖南", "广东", "广西", "海南",
            "重庆", "四川", "贵州", "云南", "陕西"
    );

    private final AdmissionScoreMapper admissionScoreMapper;
    private final ProvinceScoreLineMapper provinceScoreLineMapper;

    @Value("${consultant.data.seed-sample-admission-scores:false}")
    private boolean seedOnStartup;

    public MultiProvinceAdmissionExpander(
            AdmissionScoreMapper admissionScoreMapper,
            ProvinceScoreLineMapper provinceScoreLineMapper) {
        this.admissionScoreMapper = admissionScoreMapper;
        this.provinceScoreLineMapper = provinceScoreLineMapper;
    }

    @Override
    public void run(String... args) {
        if (!seedOnStartup) {
            return;
        }
        if (isAlreadyExpanded()) {
            log.debug("多省份录取样本已存在，跳过扩展");
            return;
        }
        try {
            ensureProvinceLines();
            int inserted = expandFromHenan("science");
            inserted += expandFromHenan("liberal");
            if (inserted > 0) {
                log.info("多省份录取数据扩展完成，新增 {} 条", inserted);
            }
        } catch (Exception e) {
            log.warn("多省份录取扩展失败: {}", e.getMessage());
        }
    }

    private void ensureProvinceLines() {
        int[] henanScience1 = {518, 509, 514, 511, 509};
        int[] henanScience2 = {400, 405, 409, 407, 405};
        int[] henanLiberal1 = {558, 527, 547, 521, 516};
        int[] henanLiberal2 = {466, 445, 465, 428, 422};

        for (String province : TARGET_PROVINCES) {
            int off = SCIENCE_SCORE_OFFSET.getOrDefault(province, 3);
            for (int i = 0; i < YEARS.size(); i++) {
                saveLine(province, "science", "本科一批", YEARS.get(i), henanScience1[i] + off);
                saveLine(province, "science", "本科二批", YEARS.get(i), henanScience2[i] + off);
                saveLine(province, "liberal", "本科一批", YEARS.get(i), henanLiberal1[i] + off - 5);
                saveLine(province, "liberal", "本科二批", YEARS.get(i), henanLiberal2[i] + off - 5);
            }
        }
    }

    private void saveLine(String province, String subject, String batch, int year, int score) {
        long exists = provinceScoreLineMapper.selectCount(
                new LambdaQueryWrapper<ProvinceScoreLine>()
                        .eq(ProvinceScoreLine::getProvince, province)
                        .eq(ProvinceScoreLine::getSubjectType, subject)
                        .eq(ProvinceScoreLine::getBatch, batch)
                        .eq(ProvinceScoreLine::getYear, year));
        if (exists > 0) {
            return;
        }
        ProvinceScoreLine line = new ProvinceScoreLine();
        line.setProvince(province);
        line.setSubjectType(subject);
        line.setBatch(batch);
        line.setYear(year);
        line.setScore(score);
        provinceScoreLineMapper.insert(line);
    }

    private boolean isAlreadyExpanded() {
        return admissionScoreMapper.selectCount(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getProvince, "辽宁")
                        .eq(AdmissionScore::getDataSource, DATA_SOURCE)) > 0;
    }

    private int expandFromHenan(String subjectType) {
        List<AdmissionScore> baseRows = admissionScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getProvince, BASE_PROVINCE)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or()
                                .eq(AdmissionScore::getMajorCode, "")));

        int inserted = 0;
        for (String province : TARGET_PROVINCES) {
            int offset = SCIENCE_SCORE_OFFSET.getOrDefault(province, 3);
            if ("liberal".equals(subjectType)) {
                offset = offset - 5;
            }
            for (AdmissionScore base : baseRows) {
                if (exists(province, subjectType, base)) {
                    continue;
                }
                AdmissionScore row = new AdmissionScore();
                row.setSchoolCode(base.getSchoolCode());
                row.setSchoolName(base.getSchoolName());
                row.setYear(base.getYear());
                row.setProvince(province);
                row.setSubjectType(subjectType);
                row.setBatch(base.getBatch());
                int score = base.getMinScore() + offset;
                row.setMinScore(score);
                row.setAvgScore(score + 3);
                row.setMinRank(estimateRank(score));
                row.setDataSource(DATA_SOURCE);
                admissionScoreMapper.insert(row);
                inserted++;
            }
        }
        return inserted;
    }

    private boolean exists(String province, String subjectType, AdmissionScore base) {
        return admissionScoreMapper.selectCount(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getSchoolCode, base.getSchoolCode())
                        .eq(AdmissionScore::getProvince, province)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .eq(AdmissionScore::getBatch, base.getBatch())
                        .eq(AdmissionScore::getYear, base.getYear())
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or()
                                .eq(AdmissionScore::getMajorCode, ""))) > 0;
    }

    private int estimateRank(int score) {
        return Math.max(50, (750 - score) * 1200);
    }
}
