package com.isoft.consultant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isoft.consultant.dto.SaveVolunteerPlanRequest;
import com.isoft.consultant.dto.VolunteerPlanVO;
import com.isoft.consultant.entity.VolunteerPlan;
import com.isoft.consultant.mapper.VolunteerPlanMapper;
import com.isoft.consultant.service.VolunteerPlanService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VolunteerPlanServiceImpl implements VolunteerPlanService {

    private final VolunteerPlanMapper volunteerPlanMapper;
    private final ObjectMapper objectMapper;

    public VolunteerPlanServiceImpl(VolunteerPlanMapper volunteerPlanMapper, ObjectMapper objectMapper) {
        this.volunteerPlanMapper = volunteerPlanMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<VolunteerPlanVO> list(Long userId) {
        return volunteerPlanMapper.selectList(
                        new LambdaQueryWrapper<VolunteerPlan>()
                                .eq(VolunteerPlan::getUserId, userId)
                                .orderByDesc(VolunteerPlan::getUpdateTime))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    public VolunteerPlanVO get(Long userId, Long id) {
        VolunteerPlan row = requireOwned(userId, id);
        return toVo(row);
    }

    @Override
    public Long save(Long userId, SaveVolunteerPlanRequest req) {
        if (!StringUtils.hasText(req.getTitle())) {
            throw new IllegalArgumentException("请填写方案名称");
        }
        VolunteerPlan plan = new VolunteerPlan();
        plan.setUserId(userId);
        plan.setTitle(req.getTitle().trim());
        plan.setSchoolProvince(req.getSchoolProvince());
        plan.setScoreProvince(req.getScoreProvince());
        plan.setSubjectType(req.getSubjectType());
        plan.setUserScore(req.getUserScore());
        plan.setPlanJson(req.getPlanJson());
        plan.setNote(req.getNote());
        volunteerPlanMapper.insert(plan);
        return plan.getId();
    }

    @Override
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        volunteerPlanMapper.deleteById(id);
    }

    @Override
    public String buildReviewSummary(Long userId, Long id) {
        VolunteerPlan row = requireOwned(userId, id);
        StringBuilder sb = new StringBuilder();
        sb.append("方案名称：").append(row.getTitle()).append('\n');
        sb.append("院校所在地：").append(nullToDash(row.getSchoolProvince()));
        sb.append("，录取生源省：").append(nullToDash(row.getScoreProvince()));
        sb.append("，科类：").append(nullToDash(row.getSubjectType()));
        sb.append("，考生分：").append(row.getUserScore() != null ? row.getUserScore() : "-").append('\n');
        if (StringUtils.hasText(row.getNote())) {
            sb.append("备注：").append(row.getNote()).append('\n');
        }
        if (!StringUtils.hasText(row.getPlanJson())) {
            sb.append("（无测算明细 JSON）");
            return sb.toString();
        }
        try {
            JsonNode root = objectMapper.readTree(row.getPlanJson());
            String tierMode = root.path("tierMode").asText("");
            if (StringUtils.hasText(tierMode)) {
                sb.append("分档模式：").append("relative".equals(tierMode) ? "相对分档（样本线集中）" : "绝对线差").append('\n');
            }
            JsonNode summary = root.path("summary");
            if (!summary.isMissingNode()) {
                sb.append("冲 ").append(summary.path("rushCount").asInt(0));
                sb.append(" 所，稳 ").append(summary.path("stableCount").asInt(0));
                sb.append(" 所，保 ").append(summary.path("safeCount").asInt(0)).append(" 所。\n");
            }
            appendSchoolList(sb, "冲", root.path("rush"));
            appendSchoolList(sb, "稳", root.path("stable"));
            appendSchoolList(sb, "保", root.path("safe"));
        } catch (Exception e) {
            sb.append("方案 JSON 解析失败。");
        }
        return sb.toString();
    }

    private void appendSchoolList(StringBuilder sb, String label, JsonNode arr) {
        if (!arr.isArray() || arr.isEmpty()) {
            return;
        }
        sb.append(label).append("档示例（最多5所）：");
        int n = Math.min(5, arr.size());
        for (int i = 0; i < n; i++) {
            JsonNode s = arr.get(i);
            if (i > 0) {
                sb.append("；");
            }
            sb.append(s.path("schoolName").asText("未知校"));
            if (s.has("minScore")) {
                sb.append(" 最低").append(s.path("minScore").asInt()).append("分");
            }
            if (s.has("scoreDiff")) {
                sb.append(" 线差").append(s.path("scoreDiff").asInt());
            }
        }
        sb.append('\n');
    }

    private String nullToDash(String v) {
        return StringUtils.hasText(v) ? v : "-";
    }

    private VolunteerPlan requireOwned(Long userId, Long id) {
        VolunteerPlan row = volunteerPlanMapper.selectById(id);
        if (row == null || !userId.equals(row.getUserId())) {
            throw new IllegalArgumentException("方案不存在");
        }
        return row;
    }

    private VolunteerPlanVO toVo(VolunteerPlan row) {
        VolunteerPlanVO vo = new VolunteerPlanVO();
        vo.setId(row.getId());
        vo.setTitle(row.getTitle());
        vo.setSchoolProvince(row.getSchoolProvince());
        vo.setScoreProvince(row.getScoreProvince());
        vo.setSubjectType(row.getSubjectType());
        vo.setUserScore(row.getUserScore());
        vo.setPlanJson(row.getPlanJson());
        vo.setNote(row.getNote());
        vo.setCreateTime(row.getCreateTime());
        vo.setUpdateTime(row.getUpdateTime());
        fillCounts(vo, row.getPlanJson());
        return vo;
    }

    private void fillCounts(VolunteerPlanVO vo, String planJson) {
        if (!StringUtils.hasText(planJson)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(planJson);
            JsonNode summary = root.path("summary");
            if (!summary.isMissingNode()) {
                vo.setRushCount(summary.path("rushCount").asInt(0));
                vo.setStableCount(summary.path("stableCount").asInt(0));
                vo.setSafeCount(summary.path("safeCount").asInt(0));
                return;
            }
            vo.setRushCount(root.path("rush").size());
            vo.setStableCount(root.path("stable").size());
            vo.setSafeCount(root.path("safe").size());
        } catch (Exception ignored) {
            // 非 JSON 时忽略计数
        }
    }
}
