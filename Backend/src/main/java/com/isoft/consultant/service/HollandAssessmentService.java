package com.isoft.consultant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isoft.consultant.dto.HollandQuestionVO;
import com.isoft.consultant.dto.HollandResultVO;
import com.isoft.consultant.dto.HollandSubmitRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 霍兰德 RIASEC 兴趣测评：本地计分，结果稳定可复现。
 */
@Service
public class HollandAssessmentService {

    private static final Map<String, String> DIMENSION_NAMES = Map.of(
            "R", "现实型",
            "I", "研究型",
            "A", "艺术型",
            "S", "社会型",
            "E", "企业型",
            "C", "常规型");

    private static final Map<String, String> DIMENSION_DESC = Map.of(
            "R", "偏好动手实践、具体任务与工程技术",
            "I", "偏好分析推理、科学研究与探索未知",
            "A", "偏好创意表达、审美设计与自由发挥",
            "S", "偏好助人沟通、教育服务与团队协作",
            "E", "偏好组织领导、商业影响与目标达成",
            "C", "偏好条理规范、数据处理与细致执行");

    private static final Map<String, List<String>> MAJORS_BY_DIMENSION = Map.of(
            "R", List.of(
                    "机械设计制造及其自动化", "电气工程及其自动化", "土木工程",
                    "车辆工程", "自动化", "材料科学与工程", "交通运输", "建筑学"),
            "I", List.of(
                    "计算机科学与技术", "软件工程", "人工智能", "数据科学与大数据技术",
                    "数学与应用数学", "物理学", "生物科学", "电子信息工程", "统计学"),
            "A", List.of(
                    "视觉传达设计", "环境设计", "数字媒体艺术", "广告学",
                    "广播电视编导", "汉语言文学", "新闻学", "音乐学", "动画"),
            "S", List.of(
                    "学前教育", "小学教育", "教育学", "社会工作", "护理学",
                    "心理学", "人力资源管理", "康复治疗学", "中医学"),
            "E", List.of(
                    "工商管理", "市场营销", "国际经济与贸易", "金融学",
                    "会计学", "电子商务", "法学", "旅游管理", "物流管理"),
            "C", List.of(
                    "会计学", "财务管理", "审计学", "信息管理与信息系统",
                    "图书馆学", "档案学", "行政管理", "公共事业管理", "税收学"));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<QuestionItem> questions = List.of();

    @PostConstruct
    void loadQuestions() {
        try (InputStream in = new ClassPathResource("holland_questions.json").getInputStream()) {
            questions = objectMapper.readValue(in, new TypeReference<List<QuestionItem>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("加载霍兰德题库失败", e);
        }
    }

    public List<HollandQuestionVO> listQuestions() {
        return questions.stream().map(q -> {
            HollandQuestionVO vo = new HollandQuestionVO();
            vo.setId(q.id);
            vo.setDimension(q.dimension);
            vo.setDimensionName(DIMENSION_NAMES.get(q.dimension));
            vo.setText(q.text);
            return vo;
        }).collect(Collectors.toList());
    }

    public HollandResultVO submit(HollandSubmitRequest request) {
        if (request == null || request.getAnswers() == null) {
            throw new IllegalArgumentException("请提交完整作答");
        }
        Map<Integer, Integer> answerMap = request.getAnswers().stream()
                .filter(a -> a.getQuestionId() != null && a.getScore() != null)
                .collect(Collectors.toMap(
                        HollandSubmitRequest.AnswerItem::getQuestionId,
                        HollandSubmitRequest.AnswerItem::getScore,
                        (a, b) -> b));

        if (answerMap.size() != questions.size()) {
            throw new IllegalArgumentException("请完成全部 " + questions.size() + " 道题目");
        }

        Map<String, Integer> rawScores = new LinkedHashMap<>();
        for (String dim : List.of("R", "I", "A", "S", "E", "C")) {
            rawScores.put(dim, 0);
        }
        for (QuestionItem q : questions) {
            int score = answerMap.get(q.id);
            if (score < 1 || score > 5) {
                throw new IllegalArgumentException("题目 " + q.id + " 分值无效");
            }
            rawScores.merge(q.dimension, score, Integer::sum);
        }

        Map<String, Integer> percentScores = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : rawScores.entrySet()) {
            int max = 25;
            percentScores.put(e.getKey(), Math.min(100, Math.round(e.getValue() * 100f / max)));
        }

        List<Map.Entry<String, Integer>> ranked = rawScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        String hollandCode = ranked.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining());

        String dominant = ranked.get(0).getKey();
        String second = ranked.size() > 1 ? ranked.get(1).getKey() : dominant;

        HollandResultVO result = new HollandResultVO();
        result.setHollandCode(hollandCode);
        result.setDominantType(DIMENSION_NAMES.get(dominant));
        result.setDimensionScores(percentScores);
        result.setDimensionNames(new LinkedHashMap<>(DIMENSION_NAMES));
        result.setPersonalitySummary(buildSummary(hollandCode, dominant, second, percentScores));
        result.setRecommendedMajors(recommendMajors(ranked));
        result.setMajorMatches(buildMajorMatches(ranked, result.getRecommendedMajors()));
        return result;
    }

    public String buildInterpretPrompt(HollandResultVO result) {
        StringBuilder sb = new StringBuilder();
        sb.append("霍兰德代码：").append(result.getHollandCode()).append("\n");
        sb.append("主导类型：").append(result.getDominantType()).append("\n");
        sb.append("各维度百分制得分：\n");
        result.getDimensionScores().forEach((k, v) ->
                sb.append("  ").append(DIMENSION_NAMES.get(k)).append("(").append(k).append("): ")
                        .append(v).append("\n"));
        sb.append("推荐专业：").append(String.join("、", result.getRecommendedMajors())).append("\n");
        sb.append("系统摘要：").append(result.getPersonalitySummary());
        return sb.toString();
    }

    private String buildSummary(String code, String top, String second, Map<String, Integer> scores) {
        return String.format(
                "您的霍兰德兴趣代码为 %s，主导类型为「%s」与「%s」。%s；%s。以下专业推荐基于 RIASEC 模型与常见专业映射，供志愿填报参考。",
                code,
                DIMENSION_NAMES.get(top),
                DIMENSION_NAMES.get(second),
                DIMENSION_DESC.get(top),
                DIMENSION_DESC.get(second));
    }

    private List<String> recommendMajors(List<Map.Entry<String, Integer>> ranked) {
        Set<String> majors = new LinkedHashSet<>();
        for (Map.Entry<String, Integer> e : ranked) {
            for (String major : MAJORS_BY_DIMENSION.getOrDefault(e.getKey(), List.of())) {
                majors.add(major);
                if (majors.size() >= 8) {
                    return new ArrayList<>(majors);
                }
            }
        }
        return new ArrayList<>(majors);
    }

    private List<HollandResultVO.MajorMatchVO> buildMajorMatches(
            List<Map.Entry<String, Integer>> ranked,
            List<String> majors) {
        List<HollandResultVO.MajorMatchVO> list = new ArrayList<>();
        int base = 92;
        for (int i = 0; i < majors.size(); i++) {
            String major = majors.get(i);
            String dim = findDimensionForMajor(major, ranked);
            HollandResultVO.MajorMatchVO vo = new HollandResultVO.MajorMatchVO();
            vo.setMajorName(major);
            vo.setCategory(DIMENSION_NAMES.getOrDefault(dim, "综合"));
            vo.setMatchPercent(Math.max(70, base - i * 4));
            vo.setReason("与您的「" + DIMENSION_NAMES.get(dim) + "」兴趣匹配度较高");
            list.add(vo);
        }
        return list;
    }

    private String findDimensionForMajor(String major, List<Map.Entry<String, Integer>> ranked) {
        for (Map.Entry<String, Integer> e : ranked) {
            if (MAJORS_BY_DIMENSION.getOrDefault(e.getKey(), List.of()).contains(major)) {
                return e.getKey();
            }
        }
        return ranked.get(0).getKey();
    }

    public static class QuestionItem {
        public int id;
        public String dimension;
        public String text;
    }
}
