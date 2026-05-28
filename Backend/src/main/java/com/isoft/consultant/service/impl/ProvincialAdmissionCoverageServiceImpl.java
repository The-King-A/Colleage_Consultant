package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.common.CityProvinceResolver;
import com.isoft.consultant.common.ProvinceOrder;
import com.isoft.consultant.entity.AdmissionScore;
import com.isoft.consultant.entity.ProvinceScoreLine;
import com.isoft.consultant.entity.SchoolInfo;
import com.isoft.consultant.mapper.AdmissionScoreMapper;
import com.isoft.consultant.mapper.ProvinceScoreLineMapper;
import com.isoft.consultant.mapper.SchoolInfoMapper;
import com.isoft.consultant.service.ProvincialAdmissionCoverageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProvincialAdmissionCoverageServiceImpl implements ProvincialAdmissionCoverageService {

    private static final Logger log = LoggerFactory.getLogger(ProvincialAdmissionCoverageServiceImpl.class);
    private static final List<Integer> YEARS = List.of(2021, 2022, 2023, 2024, 2025);
    private static final String DATA_SOURCE = "平台样本数据（省内覆盖）";
    private static final String CROSS_DATA_SOURCE = "平台样本数据（多省份扩展）";

    private static final Map<String, Integer> CROSS_OFFSET = Map.ofEntries(
            Map.entry("北京", 12), Map.entry("天津", 10), Map.entry("上海", 14),
            Map.entry("浙江", 8), Map.entry("江苏", 6), Map.entry("山东", 2),
            Map.entry("广东", 5), Map.entry("福建", 4), Map.entry("湖北", 1),
            Map.entry("湖南", 0), Map.entry("安徽", 1), Map.entry("江西", 2),
            Map.entry("河北", 3), Map.entry("山西", 4), Map.entry("陕西", -2),
            Map.entry("四川", 3), Map.entry("重庆", 2), Map.entry("辽宁", 5),
            Map.entry("吉林", 6), Map.entry("黑龙江", 7), Map.entry("广西", 4),
            Map.entry("云南", 5), Map.entry("贵州", 6), Map.entry("海南", 3),
            Map.entry("甘肃", 2), Map.entry("宁夏", 3), Map.entry("内蒙古", 4),
            Map.entry("新疆", 5), Map.entry("河南", 0)
    );

    private static final Map<Integer, Integer> SCIENCE_YEAR_DELTA = Map.of(
            2021, 4, 2022, 2, 2023, 0, 2024, -1, 2025, 1);
    private static final Map<Integer, Integer> LIBERAL_YEAR_DELTA = Map.of(
            2021, 5, 2022, 3, 2023, 0, 2024, -2, 2025, 1);

    private final SchoolInfoMapper schoolInfoMapper;
    private final AdmissionScoreMapper admissionScoreMapper;
    private final ProvinceScoreLineMapper provinceScoreLineMapper;
    private final CityProvinceResolver cityProvinceResolver;

    public ProvincialAdmissionCoverageServiceImpl(
            SchoolInfoMapper schoolInfoMapper,
            AdmissionScoreMapper admissionScoreMapper,
            ProvinceScoreLineMapper provinceScoreLineMapper,
            CityProvinceResolver cityProvinceResolver) {
        this.schoolInfoMapper = schoolInfoMapper;
        this.admissionScoreMapper = admissionScoreMapper;
        this.provinceScoreLineMapper = provinceScoreLineMapper;
        this.cityProvinceResolver = cityProvinceResolver;
    }

    @Override
    public int ensureLocalCoverage(String province) {
        List<String> targets = StringUtils.hasText(province)
                ? List.of(province.trim())
                : ProvinceOrder.allProvinces();

        List<SchoolInfo> allSchools = schoolInfoMapper.selectList(
                new LambdaQueryWrapper<SchoolInfo>().eq(SchoolInfo::getStatus, 1));

        int inserted = 0;
        for (String prov : targets) {
            ensureProvinceLines(prov);
            int batch1 = getBatchLine(prov, "science", "本科一批", 2024);
            int batch2 = getBatchLine(prov, "science", "本科二批", 2024);
            int provInserted = 0;

            Set<String> scienceKeys = loadExistingSchoolYearKeys(prov, "science");
            Set<String> liberalKeys = loadExistingSchoolYearKeys(prov, "liberal");
            for (SchoolInfo school : allSchools) {
                String schoolProv = cityProvinceResolver.resolveProvince(school.getLocation(), school.getCity());
                if (!prov.equals(schoolProv)) {
                    continue;
                }
                provInserted += ensureForSchool(
                        school, prov, "science", batch1, batch2, SCIENCE_YEAR_DELTA, scienceKeys);
                provInserted += ensureForSchool(
                        school, prov, "liberal", batch1 - 40, batch2 - 40, LIBERAL_YEAR_DELTA, liberalKeys);
            }
            inserted += provInserted;
            log.info("省内录取样本补全：{} 新增 {} 条", prov, provInserted);
        }
        return inserted;
    }

    @Override
    public int ensureMatcherPair(String schoolProvince, String scoreProvince) {
        if (!StringUtils.hasText(schoolProvince) || !StringUtils.hasText(scoreProvince)) {
            return ensureLocalCoverage(schoolProvince);
        }
        String school = schoolProvince.trim();
        String score = scoreProvince.trim();
        int n = ensureLocalCoverage(school);
        if (!school.equals(score)) {
            n += expandCrossToTarget(school, score);
        }
        return n;
    }

    @Override
    public int ensureFullMatcherCoverage(String province) {
        return ensureMatcherPair(province, province);
    }

    /** 仅将 base 省样本复制到单个目标生源省（冲稳保页一次只选一对省） */
    private int expandCrossToTarget(String baseProvince, String targetProvince) {
        if (baseProvince.equals(targetProvince)) {
            return 0;
        }
        int inserted = 0;
        inserted += expandCrossForBaseToTarget(baseProvince, targetProvince, "science");
        inserted += expandCrossForBaseToTarget(baseProvince, targetProvince, "liberal");
        log.info("跨省录取样本：{} -> {} 新增 {} 条", baseProvince, targetProvince, inserted);
        return inserted;
    }

    private int expandCrossForBaseToTarget(String baseProvince, String targetProvince, String subjectType) {
        List<AdmissionScore> baseRows = admissionScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionScore>()
                        .eq(AdmissionScore::getProvince, baseProvince)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or().eq(AdmissionScore::getMajorCode, "")));
        if (baseRows.isEmpty()) {
            return 0;
        }
        int offset = CROSS_OFFSET.getOrDefault(targetProvince, 3);
        if ("liberal".equals(subjectType)) {
            offset -= 5;
        }
        int baseOff = CROSS_OFFSET.getOrDefault(baseProvince, 0);
        int delta = offset - baseOff;

        Set<String> existing = loadExistingSchoolYearKeys(targetProvince, subjectType);
        List<AdmissionScore> pending = new ArrayList<>();
        for (AdmissionScore base : baseRows) {
            String key = crossKey(base);
            if (existing.contains(key)) {
                continue;
            }
            int score = base.getMinScore() + delta;
            AdmissionScore row = new AdmissionScore();
            row.setSchoolCode(base.getSchoolCode());
            row.setSchoolName(base.getSchoolName());
            row.setYear(base.getYear());
            row.setProvince(targetProvince);
            row.setSubjectType(subjectType);
            row.setBatch(base.getBatch());
            row.setMinScore(score);
            row.setAvgScore(score + 3);
            row.setMinRank(Math.max(50, (750 - score) * 1200));
            row.setDataSource(CROSS_DATA_SOURCE);
            pending.add(row);
            existing.add(key);
        }
        for (AdmissionScore row : pending) {
            admissionScoreMapper.insert(row);
        }
        return pending.size();
    }

    private Set<String> loadExistingSchoolYearKeys(String province, String subjectType) {
        List<AdmissionScore> rows = admissionScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionScore>()
                        .select(AdmissionScore::getSchoolCode, AdmissionScore::getYear,
                                AdmissionScore::getBatch, AdmissionScore::getSubjectType)
                        .eq(AdmissionScore::getProvince, province)
                        .eq(AdmissionScore::getSubjectType, subjectType)
                        .and(w -> w.isNull(AdmissionScore::getMajorCode)
                                .or().eq(AdmissionScore::getMajorCode, "")));
        Set<String> keys = new HashSet<>(rows.size() * 2);
        for (AdmissionScore row : rows) {
            keys.add(crossKey(row));
        }
        return keys;
    }

    private static String crossKey(AdmissionScore row) {
        return row.getSchoolCode() + "|" + row.getYear() + "|" + row.getBatch();
    }

    private int ensureForSchool(
            SchoolInfo school,
            String scoreProvince,
            String subjectType,
            int batch1Base,
            int batch2Base,
            Map<Integer, Integer> yearDelta,
            Set<String> existingKeys) {
        String code = school.getSchoolCode();
        String prefix = code + "|";
        if (existingKeys.stream().anyMatch(k -> k.startsWith(prefix))) {
            return 0;
        }
        String batch = resolveBatch(school);
        int base = tierBaseScore(school, batch1Base, batch2Base);
        int inserted = 0;
        for (Integer year : YEARS) {
            String yearKey = code + "|" + year + "|" + batch;
            if (existingKeys.contains(yearKey)) {
                continue;
            }
            int score = base + yearDelta.getOrDefault(year, 0);
            admissionScoreMapper.insert(buildRow(school, scoreProvince, subjectType, batch, year, score));
            existingKeys.add(yearKey);
            inserted++;
        }
        return inserted;
    }

    private void ensureProvinceLines(String province) {
        int[] s1 = {518, 509, 514, 511, 509};
        int[] s2 = {400, 405, 409, 407, 405};
        int[] l1 = {558, 527, 547, 521, 516};
        int[] l2 = {466, 445, 465, 428, 422};
        int off = provinceOffset(province);

        for (int i = 0; i < YEARS.size(); i++) {
            int year = YEARS.get(i);
            saveLineIfAbsent(province, year, "science", "本科一批", s1[i] + off);
            saveLineIfAbsent(province, year, "science", "本科二批", s2[i] + off);
            saveLineIfAbsent(province, year, "liberal", "本科一批", l1[i] + off - 5);
            saveLineIfAbsent(province, year, "liberal", "本科二批", l2[i] + off - 5);
        }
    }

    private int provinceOffset(String province) {
        Map<String, Integer> offsets = Map.ofEntries(
                Map.entry("北京", 12), Map.entry("上海", 14), Map.entry("天津", 10),
                Map.entry("浙江", 8), Map.entry("江苏", 6), Map.entry("山东", 2),
                Map.entry("河南", 0), Map.entry("河北", 3), Map.entry("湖北", 1),
                Map.entry("湖南", 0), Map.entry("广东", 5), Map.entry("福建", 4),
                Map.entry("四川", 3), Map.entry("陕西", -2), Map.entry("辽宁", 5),
                Map.entry("吉林", 6), Map.entry("黑龙江", 7), Map.entry("云南", 5),
                Map.entry("贵州", 6), Map.entry("广西", 4), Map.entry("海南", 3),
                Map.entry("重庆", 2), Map.entry("江西", 2), Map.entry("安徽", 1),
                Map.entry("山西", 4), Map.entry("甘肃", 2), Map.entry("宁夏", 3),
                Map.entry("青海", 1), Map.entry("内蒙古", 4), Map.entry("新疆", 5),
                Map.entry("西藏", -5));
        return offsets.getOrDefault(province, 2);
    }

    private void saveLineIfAbsent(String province, int year, String subject, String batch, int score) {
        long exists = provinceScoreLineMapper.selectCount(
                new LambdaQueryWrapper<ProvinceScoreLine>()
                        .eq(ProvinceScoreLine::getProvince, province)
                        .eq(ProvinceScoreLine::getYear, year)
                        .eq(ProvinceScoreLine::getSubjectType, subject)
                        .eq(ProvinceScoreLine::getBatch, batch));
        if (exists > 0) {
            return;
        }
        ProvinceScoreLine line = new ProvinceScoreLine();
        line.setYear(year);
        line.setProvince(province);
        line.setSubjectType(subject);
        line.setBatch(batch);
        line.setScore(score);
        provinceScoreLineMapper.insert(line);
    }

    private int getBatchLine(String province, String subject, String batch, int year) {
        ProvinceScoreLine line = provinceScoreLineMapper.selectOne(
                new LambdaQueryWrapper<ProvinceScoreLine>()
                        .eq(ProvinceScoreLine::getProvince, province)
                        .eq(ProvinceScoreLine::getSubjectType, subject)
                        .eq(ProvinceScoreLine::getBatch, batch)
                        .eq(ProvinceScoreLine::getYear, year)
                        .last("LIMIT 1"));
        if (line != null && line.getScore() != null) {
            return line.getScore();
        }
        return "本科一批".equals(batch) ? 511 : 407;
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
        return "本科二批";
    }

    private int tierBaseScore(SchoolInfo school, int batch1Base, int batch2Base) {
        if (school.getIs985() != null && school.getIs985() == 1) {
            return batch1Base + 95;
        }
        if (school.getIs211() != null && school.getIs211() == 1) {
            return batch1Base + 55;
        }
        if (school.getIsDoubleFirst() != null && school.getIsDoubleFirst() == 1) {
            return batch1Base + 35;
        }
        if ("民办".equals(school.getSchoolNature())) {
            return batch2Base - 15;
        }
        return batch2Base + 25;
    }

    private AdmissionScore buildRow(
            SchoolInfo school, String province, String subjectType, String batch, int year, int minScore) {
        AdmissionScore row = new AdmissionScore();
        row.setSchoolCode(school.getSchoolCode());
        row.setSchoolName(school.getSchoolName());
        row.setYear(year);
        row.setProvince(province);
        row.setSubjectType(subjectType);
        row.setBatch(batch);
        row.setMinScore(minScore);
        row.setAvgScore(minScore + 3);
        row.setMinRank(Math.max(50, (750 - minScore) * 1200));
        row.setDataSource(DATA_SOURCE);
        return row;
    }
}
