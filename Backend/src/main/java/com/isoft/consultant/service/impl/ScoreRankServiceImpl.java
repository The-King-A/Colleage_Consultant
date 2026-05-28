package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isoft.consultant.dto.RankEquivalentVO;
import com.isoft.consultant.dto.ScoreRankLookupVO;
import com.isoft.consultant.entity.ScoreSegment;
import com.isoft.consultant.mapper.ScoreSegmentMapper;
import com.isoft.consultant.service.ScoreRankService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreRankServiceImpl implements ScoreRankService {

    private static final int DEFAULT_YEAR = 2024;

    private final ScoreSegmentMapper scoreSegmentMapper;

    public ScoreRankServiceImpl(ScoreSegmentMapper scoreSegmentMapper) {
        this.scoreSegmentMapper = scoreSegmentMapper;
    }

    @Override
    public ScoreRankLookupVO lookup(String province, String subjectType, Integer score, Integer year) {
        if (!StringUtils.hasText(province) || score == null) {
            throw new IllegalArgumentException("请提供省份与分数");
        }
        if (score < 0 || score > 750) {
            throw new IllegalArgumentException("分数应在 0-750 之间");
        }
        String subj = StringUtils.hasText(subjectType) ? subjectType : "science";
        int y = year != null ? year : DEFAULT_YEAR;

        seedIfEmpty();

        ScoreSegment matched = findSegment(province, subj, y, score);
        if (matched == null) {
            ScoreRankLookupVO empty = new ScoreRankLookupVO();
            empty.setProvince(province);
            empty.setSubjectType(subj);
            empty.setYear(y);
            empty.setUserScore(score);
            empty.setTip("暂无该省「" + province + "」" + y + "年一分一段样本，请联系管理员导入或换省查询。");
            return empty;
        }

        int total = estimateTotalCandidates(province, subj, y);
        double beat = total > 0 && matched.getCumulativeRank() != null
                ? Math.round(1000.0 * (total - matched.getCumulativeRank()) / total) / 10.0
                : null;

        ScoreRankLookupVO vo = new ScoreRankLookupVO();
        vo.setProvince(province);
        vo.setSubjectType(subj);
        vo.setYear(y);
        vo.setUserScore(score);
        vo.setMatchedScore(matched.getScore());
        vo.setCumulativeRank(matched.getCumulativeRank());
        vo.setSegmentCount(matched.getSegmentCount());
        vo.setBeatPercent(beat);
        vo.setEquivalents(buildEquivalents(province, subj, y, matched.getCumulativeRank()));
        vo.setTip(buildTip(vo));
        return vo;
    }

    @Override
    public void seedIfEmpty() {
        Long count = scoreSegmentMapper.selectCount(null);
        if (count != null && count > 0) {
            return;
        }
        List<ScoreSegment> rows = new ArrayList<>();
        for (String prov : List.of("北京", "河南", "广东", "山东")) {
            rows.addAll(buildSampleSegments(prov, "science", 2024));
            rows.addAll(buildSampleSegments(prov, "science", 2023));
        }
        for (ScoreSegment row : rows) {
            scoreSegmentMapper.insert(row);
        }
    }

    private ScoreSegment findSegment(String province, String subjectType, int year, int score) {
        List<ScoreSegment> exact = scoreSegmentMapper.selectList(
                new LambdaQueryWrapper<ScoreSegment>()
                        .eq(ScoreSegment::getProvince, province)
                        .eq(ScoreSegment::getSubjectType, subjectType)
                        .eq(ScoreSegment::getYear, year)
                        .eq(ScoreSegment::getScore, score)
                        .last("LIMIT 1"));
        if (!exact.isEmpty()) {
            return exact.get(0);
        }
        List<ScoreSegment> floor = scoreSegmentMapper.selectList(
                new LambdaQueryWrapper<ScoreSegment>()
                        .eq(ScoreSegment::getProvince, province)
                        .eq(ScoreSegment::getSubjectType, subjectType)
                        .eq(ScoreSegment::getYear, year)
                        .le(ScoreSegment::getScore, score)
                        .orderByDesc(ScoreSegment::getScore)
                        .last("LIMIT 1"));
        return floor.isEmpty() ? null : floor.get(0);
    }

    private List<RankEquivalentVO> buildEquivalents(String province, String subjectType, int year, Integer rank) {
        if (rank == null) {
            return List.of();
        }
        List<Integer> years = scoreSegmentMapper.selectList(
                        new LambdaQueryWrapper<ScoreSegment>()
                                .eq(ScoreSegment::getProvince, province)
                                .eq(ScoreSegment::getSubjectType, subjectType)
                                .ne(ScoreSegment::getYear, year)
                                .select(ScoreSegment::getYear))
                .stream()
                .map(ScoreSegment::getYear)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .collect(Collectors.toList());

        List<RankEquivalentVO> list = new ArrayList<>();
        for (Integer y : years) {
            ScoreSegment closest = findClosestByRank(province, subjectType, y, rank);
            if (closest == null) {
                continue;
            }
            RankEquivalentVO eq = new RankEquivalentVO();
            eq.setYear(y);
            eq.setEquivalentScore(closest.getScore());
            eq.setCumulativeRankAtYear(closest.getCumulativeRank());
            eq.setNote("按累计位次接近换算，仅供参考");
            list.add(eq);
        }
        return list;
    }

    private ScoreSegment findClosestByRank(String province, String subjectType, int year, int targetRank) {
        List<ScoreSegment> rows = scoreSegmentMapper.selectList(
                new LambdaQueryWrapper<ScoreSegment>()
                        .eq(ScoreSegment::getProvince, province)
                        .eq(ScoreSegment::getSubjectType, subjectType)
                        .eq(ScoreSegment::getYear, year));
        if (rows.isEmpty()) {
            return null;
        }
        return rows.stream()
                .filter(r -> r.getCumulativeRank() != null)
                .min(Comparator.comparingInt(r -> Math.abs(r.getCumulativeRank() - targetRank)))
                .orElse(null);
    }

    private int estimateTotalCandidates(String province, String subjectType, int year) {
        ScoreSegment lowest = scoreSegmentMapper.selectOne(
                new LambdaQueryWrapper<ScoreSegment>()
                        .eq(ScoreSegment::getProvince, province)
                        .eq(ScoreSegment::getSubjectType, subjectType)
                        .eq(ScoreSegment::getYear, year)
                        .orderByAsc(ScoreSegment::getScore)
                        .last("LIMIT 1"));
        if (lowest == null || lowest.getCumulativeRank() == null) {
            return 120_000;
        }
        return lowest.getCumulativeRank() + (lowest.getSegmentCount() != null ? lowest.getSegmentCount() : 500);
    }

    private String buildTip(ScoreRankLookupVO vo) {
        if (vo.getCumulativeRank() == null) {
            return vo.getTip();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("您的分数 ").append(vo.getUserScore()).append(" 分");
        if (!vo.getUserScore().equals(vo.getMatchedScore())) {
            sb.append("（按 ").append(vo.getMatchedScore()).append(" 分段查表）");
        }
        sb.append("，对应累计位次约 ").append(vo.getCumulativeRank()).append(" 名。");
        if (vo.getBeatPercent() != null) {
            sb.append(" 约超过本省同科类 ").append(vo.getBeatPercent()).append("% 的考生（样本估算）。");
        }
        return sb.toString();
    }

    /** 生成单调递减的样本一分一段（每 5 分一档） */
    private List<ScoreSegment> buildSampleSegments(String province, String subjectType, int year) {
        List<ScoreSegment> list = new ArrayList<>();
        int baseOffset = Math.abs(province.hashCode() % 3000);
        for (int score = 400; score <= 700; score += 5) {
            ScoreSegment row = new ScoreSegment();
            row.setYear(year);
            row.setProvince(province);
            row.setSubjectType(subjectType);
            row.setScore(score);
            int rank = (750 - score) * 320 + baseOffset + (year == 2023 ? 800 : 0);
            row.setCumulativeRank(rank);
            row.setSegmentCount(40 + (score % 23) * 3);
            list.add(row);
        }
        return list;
    }
}
