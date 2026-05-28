package com.isoft.consultant.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.entity.AdmissionScore;
import com.isoft.consultant.entity.ProvinceScoreLine;
import com.isoft.consultant.entity.SchoolInfo;
import com.isoft.consultant.mapper.AdmissionScoreMapper;
import com.isoft.consultant.mapper.ProvinceScoreLineMapper;
import com.isoft.consultant.mapper.SchoolInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 启动时补全河南省理科/文科近五年（2021-2025）院校级录取分数线。
 * 已有年份作锚点，缺失年份按趋势推算（样本数据，仅供演示）。
 */
@Component
@Order(20)
public class AdmissionScoreEnricher implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdmissionScoreEnricher.class);
    private static final String PROVINCE = "河南";
    private static final String DATA_SOURCE = "平台样本数据（近五年补全）";
    private static final List<Integer> YEARS = List.of(2021, 2022, 2023, 2024, 2025);

    /** 理科每年相对 2023 年的分数偏移（无锚点数据时用） */
    private static final Map<Integer, Integer> SCIENCE_YEAR_DELTA = Map.of(
            2021, 4, 2022, 2, 2023, 0, 2024, -1, 2025, 1);

    /** 文科每年相对 2023 年的分数偏移 */
    private static final Map<Integer, Integer> LIBERAL_YEAR_DELTA = Map.of(
            2021, 5, 2022, 3, 2023, 0, 2024, -2, 2025, 1);

    private final SchoolInfoMapper schoolInfoMapper;
    private final AdmissionScoreMapper admissionScoreMapper;
    private final ProvinceScoreLineMapper provinceScoreLineMapper;

    @Value("${consultant.data.seed-sample-admission-scores:false}")
    private boolean seedOnStartup;

    public AdmissionScoreEnricher(
            SchoolInfoMapper schoolInfoMapper,
            AdmissionScoreMapper admissionScoreMapper,
            ProvinceScoreLineMapper provinceScoreLineMapper) {
        this.schoolInfoMapper = schoolInfoMapper;
        this.admissionScoreMapper = admissionScoreMapper;
        this.provinceScoreLineMapper = provinceScoreLineMapper;
    }

    @Override
    public void run(String... args) {
        if (!seedOnStartup) {
            return;
        }
        try {
            ensureProvinceLines();
            int inserted = enrichSchoolLevelScores("science", "本科一批", SCIENCE_YEAR_DELTA);
            inserted += enrichSchoolLevelScores("liberal", "本科一批", LIBERAL_YEAR_DELTA);
            inserted += enrichFallbackScience();
            if (inserted > 0) {
                log.info("近五年录取数据补全完成，新增 {} 条院校级记录", inserted);
            } else {
                log.debug("近五年录取数据已齐全，无需补全");
            }
        } catch (Exception e) {
            log.warn("近五年录取数据补全失败: {}", e.getMessage());
        }
    }

    private void ensureProvinceLines() {
        Map<String, int[]> scienceBatches = Map.of(
                "本科一批", new int[]{518, 509, 514, 511, 509},
                "本科二批", new int[]{400, 405, 409, 407, 405}
        );
        Map<String, int[]> liberalBatches = Map.of(
                "本科一批", new int[]{558, 527, 547, 521, 516},
                "本科二批", new int[]{466, 445, 465, 428, 422}
        );

        for (int i = 0; i < YEARS.size(); i++) {
            int year = YEARS.get(i);
            saveLineIfAbsent(year, "science", "本科一批", scienceBatches.get("本科一批")[i]);
            saveLineIfAbsent(year, "science", "本科二批", scienceBatches.get("本科二批")[i]);
            saveLineIfAbsent(year, "liberal", "本科一批", liberalBatches.get("本科一批")[i]);
            saveLineIfAbsent(year, "liberal", "本科二批", liberalBatches.get("本科二批")[i]);
        }
    }

    private void saveLineIfAbsent(int year, String subjectType, String batch, int score) {
        long exists = provinceScoreLineMapper.selectCount(
                new LambdaQueryWrapper<ProvinceScoreLine>()
                        .eq(ProvinceScoreLine::getYear, year)
                        .eq(ProvinceScoreLine::getProvince, PROVINCE)
                        .eq(ProvinceScoreLine::getSubjectType, subjectType)
                        .eq(ProvinceScoreLine::getBatch, batch));
        if (exists > 0) {
            return;
        }
        ProvinceScoreLine line = new ProvinceScoreLine();
        line.setYear(year);
        line.setProvince(PROVINCE);
        line.setSubjectType(subjectType);
        line.setBatch(batch);
        line.setScore(score);
        provinceScoreLineMapper.insert(line);
    }

    private int enrichSchoolLevelScores(String subjectType, String defaultBatch, Map<Integer, Integer> yearDelta) {
        List<SchoolInfo> schools = schoolInfoMapper.selectList(
                new LambdaQueryWrapper<SchoolInfo>().eq(SchoolInfo::getStatus, 1));
        int inserted = 0;

        for (SchoolInfo school : schools) {
            List<AdmissionScore> anchors = listSchoolLevel(school.getSchoolCode(), subjectType);
            if (anchors.isEmpty()) {
                anchors = deriveAnchorsFromMajors(school.getSchoolCode(), subjectType);
            }
            if (anchors.isEmpty()) {
                continue;
            }

            AdmissionScore anchor = pickAnchor(anchors);
            String batch = anchor.getBatch() != null ? anchor.getBatch() : defaultBatch;
            int anchorYear = anchor.getYear();
            int anchorScore = anchor.getMinScore();
            int anchorRank = anchor.getMinRank() != null ? anchor.getMinRank() : estimateRank(anchorScore);

            for (Integer year : YEARS) {
                if (hasSchoolYear(school.getSchoolCode(), subjectType, batch, year)) {
                    continue;
                }
                int score = interpolateScore(anchorYear, anchorScore, year, yearDelta);
                int rank = interpolateRank(anchorYear, anchorRank, year, score, anchorScore);
                admissionScoreMapper.insert(buildRow(school, subjectType, batch, year, score, rank));
                inserted++;
            }
        }
        return inserted;
    }

    /** 仍无任何理科院校线时，按层次生成近五年样本数据 */
    private int enrichFallbackScience() {
        List<SchoolInfo> schools = schoolInfoMapper.selectList(
                new LambdaQueryWrapper<SchoolInfo>().eq(SchoolInfo::getStatus, 1));
        int inserted = 0;
        for (SchoolInfo school : schools) {
            if (hasAnySchoolLevel(school.getSchoolCode(), "science")) {
                continue;
            }
            String batch = resolveBatch(school);
            int base = tierBaseScore(school);
            for (Integer year : YEARS) {
                if (hasSchoolYear(school.getSchoolCode(), "science", batch, year)) {
                    continue;
                }
                int score = base + SCIENCE_YEAR_DELTA.getOrDefault(year, 0);
                admissionScoreMapper.insert(buildRow(school, "science", batch, year, score, estimateRank(score)));
                inserted++;
            }
        }
        return inserted;
    }

    private String resolveBatch(SchoolInfo school) {
        if ("民办".equals(school.getSchoolNature())) {
            return "本科二批";
        }
        if ((school.getIs985() != null && school.getIs985() == 1)
                || (school.getIs211() != null && school.getIs211() == 1)
                || (school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1)) {
            return "本科一批";
        }
        String type = school.getSchoolType() == null ? "" : school.getSchoolType();
        if (type.contains("二本") || type.contains("普通本科")) {
            return "本科二批";
        }
        return "本科一批";
    }

    private List<AdmissionScore> listSchoolLevel(String schoolCode, String subjectType) {
        return admissionScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getSchoolCode, schoolCode)
                        .eq(AdmissionScore::getProvince, PROVINCE)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or()
                                .eq(AdmissionScore::getMajorCode, ""))
                        .orderByDesc(AdmissionScore::getYear));
    }

    /** 无院校级记录时，按每年专业录取最低分汇总为院校线 */
    private List<AdmissionScore> deriveAnchorsFromMajors(String schoolCode, String subjectType) {
        List<AdmissionScore> majors = admissionScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getSchoolCode, schoolCode)
                        .eq(AdmissionScore::getProvince, PROVINCE)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .isNotNull(AdmissionScore::getMajorCode)
                        .ne(AdmissionScore::getMajorCode, ""));

        return majors.stream()
                .collect(Collectors.groupingBy(AdmissionScore::getYear))
                .entrySet().stream()
                .map(e -> {
                    AdmissionScore min = e.getValue().stream()
                            .min((a, b) -> Integer.compare(a.getMinScore(), b.getMinScore()))
                            .orElse(null);
                    if (min == null) {
                        return null;
                    }
                    AdmissionScore row = new AdmissionScore();
                    row.setSchoolCode(min.getSchoolCode());
                    row.setSchoolName(min.getSchoolName());
                    row.setYear(e.getKey());
                    row.setProvince(min.getProvince());
                    row.setSubjectType(min.getSubjectType());
                    row.setBatch(min.getBatch());
                    row.setMinScore(min.getMinScore());
                    row.setMinRank(min.getMinRank());
                    return row;
                })
                .filter(row -> row != null)
                .sorted((a, b) -> b.getYear().compareTo(a.getYear()))
                .collect(Collectors.toList());
    }

    private boolean hasAnySchoolLevel(String schoolCode, String subjectType) {
        return admissionScoreMapper.selectCount(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getSchoolCode, schoolCode)
                        .eq(AdmissionScore::getProvince, PROVINCE)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or()
                                .eq(AdmissionScore::getMajorCode, ""))) > 0;
    }

    private boolean hasSchoolYear(String schoolCode, String subjectType, String batch, int year) {
        return admissionScoreMapper.selectCount(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getSchoolCode, schoolCode)
                        .eq(AdmissionScore::getProvince, PROVINCE)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .eq(AdmissionScore::getBatch, batch)
                        .eq(AdmissionScore::getYear, year)
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or()
                                .eq(AdmissionScore::getMajorCode, ""))) > 0;
    }

    private AdmissionScore pickAnchor(List<AdmissionScore> anchors) {
        Map<Integer, AdmissionScore> byYear = anchors.stream()
                .collect(Collectors.toMap(AdmissionScore::getYear, a -> a, (a, b) -> a));
        for (int y : List.of(2023, 2024, 2025, 2022, 2021)) {
            if (byYear.containsKey(y)) {
                return byYear.get(y);
            }
        }
        return anchors.get(0);
    }

    private int interpolateScore(int anchorYear, int anchorScore, int targetYear, Map<Integer, Integer> yearDelta) {
        int anchorDelta = yearDelta.getOrDefault(anchorYear, 0);
        int targetDelta = yearDelta.getOrDefault(targetYear, 0);
        return anchorScore - anchorDelta + targetDelta;
    }

    private int interpolateRank(int anchorYear, int anchorRank, int targetYear, int targetScore, int anchorScore) {
        int scoreDiff = targetScore - anchorScore;
        // 分数每高 1 分，位次约提前 800（粗略）
        int rank = (int) (anchorRank - scoreDiff * 800L);
        return Math.max(rank, 50);
    }

    private int estimateRank(int score) {
        return Math.max(50, (750 - score) * 1200);
    }

    private int tierBaseScore(SchoolInfo school) {
        if (school.getIs985() != null && school.getIs985() == 1) {
            return 620;
        }
        if (school.getIs211() != null && school.getIs211() == 1) {
            return 580;
        }
        if (school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1) {
            return 560;
        }
        if ("民办".equals(school.getSchoolNature())) {
            return 430;
        }
        return 510;
    }

    private AdmissionScore buildRow(
            SchoolInfo school, String subjectType, String batch, int year, int minScore, int minRank) {
        AdmissionScore row = new AdmissionScore();
        row.setSchoolCode(school.getSchoolCode());
        row.setSchoolName(school.getSchoolName());
        row.setYear(year);
        row.setProvince(PROVINCE);
        row.setSubjectType(subjectType);
        row.setBatch(batch);
        row.setMinScore(minScore);
        row.setAvgScore(minScore + 3);
        row.setMinRank(minRank);
        row.setDataSource(DATA_SOURCE);
        return row;
    }

}
