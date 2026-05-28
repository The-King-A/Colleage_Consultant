package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isoft.consultant.dto.CompleteWizardStepRequest;
import com.isoft.consultant.dto.WizardProgressVO;
import com.isoft.consultant.dto.WizardStepDetailVO;
import com.isoft.consultant.dto.WizardStepStatusVO;
import com.isoft.consultant.entity.FavoriteSchool;
import com.isoft.consultant.entity.VolunteerPlan;
import com.isoft.consultant.entity.VolunteerWizardProgress;
import com.isoft.consultant.mapper.FavoriteSchoolMapper;
import com.isoft.consultant.mapper.VolunteerPlanMapper;
import com.isoft.consultant.mapper.VolunteerWizardProgressMapper;
import com.isoft.consultant.service.VolunteerWizardService;
import com.isoft.consultant.wizard.WizardStep;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VolunteerWizardServiceImpl implements VolunteerWizardService {

    private final VolunteerWizardProgressMapper wizardMapper;
    private final FavoriteSchoolMapper favoriteSchoolMapper;
    private final VolunteerPlanMapper volunteerPlanMapper;
    private final ObjectMapper objectMapper;

    public VolunteerWizardServiceImpl(
            VolunteerWizardProgressMapper wizardMapper,
            FavoriteSchoolMapper favoriteSchoolMapper,
            VolunteerPlanMapper volunteerPlanMapper,
            ObjectMapper objectMapper) {
        this.wizardMapper = wizardMapper;
        this.favoriteSchoolMapper = favoriteSchoolMapper;
        this.volunteerPlanMapper = volunteerPlanMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public WizardProgressVO getProgress(Long userId) {
        VolunteerWizardProgress row = loadOrCreate(userId);
        syncFavoriteCount(row);
        syncPlanSnapshot(row);
        return toVo(row);
    }

    @Override
    public WizardProgressVO completeStep(Long userId, CompleteWizardStepRequest request) {
        if (request.getMarkComplete() == null) {
            request.setMarkComplete(true);
        }
        if (request.getAdvance() == null) {
            request.setAdvance(true);
        }
        return applyStep(userId, request);
    }

    @Override
    public WizardProgressVO saveStep(Long userId, CompleteWizardStepRequest request) {
        if (request.getMarkComplete() == null) {
            request.setMarkComplete(false);
        }
        if (request.getAdvance() == null) {
            request.setAdvance(false);
        }
        return applyStep(userId, request);
    }

    private WizardProgressVO applyStep(Long userId, CompleteWizardStepRequest request) {
        VolunteerWizardProgress row = loadOrCreate(userId);

        if (StringUtils.hasText(request.getSetCurrentStep())) {
            row.setCurrentStep(request.getSetCurrentStep().trim().toUpperCase());
            applyFields(row, request, null);
            mergeMeta(row, request);
            wizardMapper.updateById(row);
            syncFavoriteCount(row);
            syncPlanSnapshot(row);
            return toVo(row);
        }

        if (!StringUtils.hasText(request.getStep())) {
            throw new IllegalArgumentException("请指定步骤 step");
        }
        WizardStep step;
        try {
            step = WizardStep.valueOf(request.getStep().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效步骤: " + request.getStep());
        }

        Set<String> done = readCompleted(row);
        applyFields(row, request, step);
        mergeMeta(row, request);

        if (Boolean.TRUE.equals(request.getMarkComplete())) {
            done.add(step.name());
            row.setCompletedSteps(writeCompleted(done));
        }

        if (Boolean.TRUE.equals(request.getAdvance())) {
            row.setCurrentStep(step.next().name());
        }

        wizardMapper.updateById(row);
        syncFavoriteCount(row);
        syncPlanSnapshot(row);
        return toVo(loadOrCreate(userId));
    }

    private void applyFields(VolunteerWizardProgress row, CompleteWizardStepRequest request, WizardStep step) {
        if (request.getUserScore() != null) {
            row.setUserScore(request.getUserScore());
        }
        if (StringUtils.hasText(request.getScoreProvince())) {
            row.setScoreProvince(request.getScoreProvince());
        }
        if (StringUtils.hasText(request.getSubjectType())) {
            row.setSubjectType(request.getSubjectType());
        }
        if (StringUtils.hasText(request.getSchoolProvince())) {
            row.setSchoolProvince(request.getSchoolProvince());
        }
        if (StringUtils.hasText(request.getHollandCode())) {
            row.setHollandCode(request.getHollandCode());
        }
        if (request.getLastPlanId() != null) {
            row.setLastPlanId(request.getLastPlanId());
        }
    }

    private void mergeMeta(VolunteerWizardProgress row, CompleteWizardStepRequest request) {
        Map<String, Object> meta = readMeta(row);
        if (StringUtils.hasText(request.getHollandDominantType())) {
            meta.put("hollandDominantType", request.getHollandDominantType());
        }
        if (StringUtils.hasText(request.getLastPlanTitle())) {
            meta.put("lastPlanTitle", request.getLastPlanTitle());
        }
        if (request.getFavoriteCount() != null) {
            meta.put("favoriteCount", request.getFavoriteCount());
        }
        if (request.getMatchRushCount() != null) {
            meta.put("matchRushCount", request.getMatchRushCount());
        }
        if (request.getMatchStableCount() != null) {
            meta.put("matchStableCount", request.getMatchStableCount());
        }
        if (request.getMatchSafeCount() != null) {
            meta.put("matchSafeCount", request.getMatchSafeCount());
        }
        row.setMetaJson(writeMeta(meta));
    }

    private void syncFavoriteCount(VolunteerWizardProgress row) {
        Long count = favoriteSchoolMapper.selectCount(
                new LambdaQueryWrapper<FavoriteSchool>().eq(FavoriteSchool::getUserId, row.getUserId()));
        Map<String, Object> meta = readMeta(row);
        meta.put("favoriteCount", count != null ? count.intValue() : 0);
        row.setMetaJson(writeMeta(meta));
        wizardMapper.updateById(row);
    }

    private void syncPlanSnapshot(VolunteerWizardProgress row) {
        if (row.getLastPlanId() == null) {
            return;
        }
        VolunteerPlan plan = volunteerPlanMapper.selectById(row.getLastPlanId());
        if (plan == null || !row.getUserId().equals(plan.getUserId())) {
            return;
        }
        Map<String, Object> meta = readMeta(row);
        meta.put("lastPlanTitle", plan.getTitle());
        if (StringUtils.hasText(plan.getSchoolProvince())) {
            row.setSchoolProvince(plan.getSchoolProvince());
        }
        if (StringUtils.hasText(plan.getScoreProvince())) {
            row.setScoreProvince(plan.getScoreProvince());
        }
        if (StringUtils.hasText(plan.getSubjectType())) {
            row.setSubjectType(plan.getSubjectType());
        }
        if (plan.getUserScore() != null) {
            row.setUserScore(plan.getUserScore());
        }
        fillMatchCountsFromPlanJson(meta, plan.getPlanJson());
        row.setMetaJson(writeMeta(meta));
        wizardMapper.updateById(row);
    }

    private void fillMatchCountsFromPlanJson(Map<String, Object> meta, String planJson) {
        if (!StringUtils.hasText(planJson)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(planJson);
            JsonNode summary = root.path("summary");
            if (!summary.isMissingNode()) {
                meta.put("matchRushCount", summary.path("rushCount").asInt(0));
                meta.put("matchStableCount", summary.path("stableCount").asInt(0));
                meta.put("matchSafeCount", summary.path("safeCount").asInt(0));
            } else {
                meta.put("matchRushCount", root.path("rush").size());
                meta.put("matchStableCount", root.path("stable").size());
                meta.put("matchSafeCount", root.path("safe").size());
            }
        } catch (Exception ignored) {
            // ignore
        }
    }

    private VolunteerWizardProgress loadOrCreate(Long userId) {
        VolunteerWizardProgress row = wizardMapper.selectOne(
                new LambdaQueryWrapper<VolunteerWizardProgress>()
                        .eq(VolunteerWizardProgress::getUserId, userId));
        if (row != null) {
            return row;
        }
        row = new VolunteerWizardProgress();
        row.setUserId(userId);
        row.setCurrentStep(WizardStep.ENTER_SCORE.name());
        row.setCompletedSteps("[]");
        row.setMetaJson("{}");
        wizardMapper.insert(row);
        return row;
    }

    private WizardProgressVO toVo(VolunteerWizardProgress row) {
        Map<String, Object> meta = readMeta(row);
        Set<String> done = readCompleted(row);
        WizardStep current = parseStep(row.getCurrentStep());

        List<WizardStepStatusVO> steps = new ArrayList<>();
        List<WizardStepDetailVO> details = new ArrayList<>();
        for (WizardStep s : WizardStep.flow()) {
            boolean completed = done.contains(s.name());
            boolean isCurrent = s == current;

            WizardStepStatusVO st = new WizardStepStatusVO();
            st.setStep(s.name());
            st.setLabel(s.getLabel());
            st.setOrder(s.getOrder());
            st.setCompleted(completed);
            st.setCurrent(isCurrent);
            steps.add(st);

            details.add(buildStepDetail(s, row, meta, completed, isCurrent));
        }

        int completedCount = (int) done.size();
        int total = WizardStep.flow().size();
        int percent = total == 0 ? 0 : Math.min(100, completedCount * 100 / total);

        WizardProgressVO vo = new WizardProgressVO();
        vo.setCurrentStep(current.name());
        vo.setProgressPercent(percent);
        vo.setSteps(steps);
        vo.setStepDetails(details);
        vo.setUserScore(row.getUserScore());
        vo.setScoreProvince(row.getScoreProvince());
        vo.setSubjectType(row.getSubjectType());
        vo.setSchoolProvince(row.getSchoolProvince());
        vo.setHollandCode(row.getHollandCode());
        vo.setHollandDominantType(str(meta.get("hollandDominantType")));
        vo.setLastPlanId(row.getLastPlanId());
        vo.setLastPlanTitle(str(meta.get("lastPlanTitle")));
        vo.setFavoriteCount(intVal(meta.get("favoriteCount")));
        vo.setMatchRushCount(intVal(meta.get("matchRushCount")));
        vo.setMatchStableCount(intVal(meta.get("matchStableCount")));
        vo.setMatchSafeCount(intVal(meta.get("matchSafeCount")));
        return vo;
    }

    private WizardStepDetailVO buildStepDetail(
            WizardStep step, VolunteerWizardProgress row, Map<String, Object> meta,
            boolean completed, boolean current) {
        WizardStepDetailVO d = new WizardStepDetailVO();
        d.setStep(step.name());
        d.setLabel(step.getLabel());
        d.setOrder(step.getOrder());
        d.setCompleted(completed);
        d.setCurrent(current);

        List<String> summary = new ArrayList<>();
        Map<String, Object> data = new LinkedHashMap<>();

        switch (step) {
            case ENTER_SCORE -> {
                if (row.getUserScore() != null) {
                    data.put("userScore", row.getUserScore());
                    data.put("scoreProvince", row.getScoreProvince());
                    data.put("subjectType", row.getSubjectType() != null ? row.getSubjectType() : "science");
                    summary.add("生源省：" + nullToDash(row.getScoreProvince()));
                    summary.add("科类：" + subjectLabel(row.getSubjectType()));
                    summary.add("高考分数：" + row.getUserScore() + " 分");
                }
            }
            case INTEREST_TEST -> {
                if (StringUtils.hasText(row.getHollandCode())) {
                    data.put("hollandCode", row.getHollandCode());
                    data.put("hollandDominantType", meta.get("hollandDominantType"));
                    summary.add("霍兰德代码：" + row.getHollandCode());
                    if (meta.get("hollandDominantType") != null) {
                        summary.add("主导类型：" + meta.get("hollandDominantType"));
                    }
                }
            }
            case MATCH -> {
                boolean hasMatch = row.getUserScore() != null || StringUtils.hasText(row.getSchoolProvince());
                if (hasMatch) {
                    data.put("userScore", row.getUserScore());
                    data.put("scoreProvince", row.getScoreProvince());
                    data.put("subjectType", row.getSubjectType());
                    data.put("schoolProvince", row.getSchoolProvince());
                    data.put("matchRushCount", meta.get("matchRushCount"));
                    data.put("matchStableCount", meta.get("matchStableCount"));
                    data.put("matchSafeCount", meta.get("matchSafeCount"));
                    summary.add("院校省：" + nullToDash(row.getSchoolProvince()));
                    summary.add("生源省：" + nullToDash(row.getScoreProvince()) + " · " + row.getUserScore() + " 分");
                    if (meta.get("matchRushCount") != null) {
                        summary.add(String.format("测算：冲 %s / 稳 %s / 保 %s",
                                meta.get("matchRushCount"), meta.get("matchStableCount"), meta.get("matchSafeCount")));
                    } else {
                        summary.add("尚未保存测算结果摘要，请前往冲稳保测算");
                    }
                }
            }
            case FAVORITES -> {
                int fc = intVal(meta.get("favoriteCount"));
                data.put("favoriteCount", fc);
                summary.add("已收藏院校：" + fc + " 所");
            }
            case FINALIZE -> {
                if (row.getLastPlanId() != null) {
                    data.put("lastPlanId", row.getLastPlanId());
                    data.put("lastPlanTitle", meta.get("lastPlanTitle"));
                    summary.add("方案：" + nullToDash(str(meta.get("lastPlanTitle"))));
                    summary.add("方案 ID：" + row.getLastPlanId());
                }
            }
            default -> { }
        }

        d.setData(data);
        d.setSummary(summary);
        d.setHasData(!summary.isEmpty());
        return d;
    }

    private WizardStep parseStep(String name) {
        try {
            return WizardStep.valueOf(name);
        } catch (Exception e) {
            return WizardStep.ENTER_SCORE;
        }
    }

    private String subjectLabel(String subjectType) {
        return "liberal".equals(subjectType) ? "文科" : "理科";
    }

    private String nullToDash(String v) {
        return StringUtils.hasText(v) ? v : "未填写";
    }

    private String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer intVal(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Set<String> readCompleted(VolunteerWizardProgress row) {
        if (!StringUtils.hasText(row.getCompletedSteps())) {
            return new HashSet<>();
        }
        try {
            List<String> list = objectMapper.readValue(
                    row.getCompletedSteps(), new TypeReference<List<String>>() {});
            return new HashSet<>(list);
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private String writeCompleted(Set<String> done) {
        try {
            return objectMapper.writeValueAsString(new ArrayList<>(done));
        } catch (Exception e) {
            return "[]";
        }
    }

    private Map<String, Object> readMeta(VolunteerWizardProgress row) {
        if (!StringUtils.hasText(row.getMetaJson())) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(row.getMetaJson(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String writeMeta(Map<String, Object> meta) {
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (Exception e) {
            return "{}";
        }
    }
}
