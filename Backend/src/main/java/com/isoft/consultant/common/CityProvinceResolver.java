package com.isoft.consultant.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将院校库中的地级市 {@code location} 解析为省级行政区，用于省份筛选。
 */
@Component
public class CityProvinceResolver {

    private static final Pattern SECTION = Pattern.compile("^(.+?)(?:省|自治区|特别行政区)?（\\d+所）");
    private static final Pattern MUNICIPALITY = Pattern.compile("^(.+?)市$");

    private final Map<String, String> prefectureToProvince = new HashMap<>();
    private final Map<String, List<String>> provinceToPrefectures = new HashMap<>();

    @PostConstruct
    void loadFromMoeList() {
        try {
            ClassPathResource resource = new ClassPathResource("data/moe_schools_raw.csv");
            if (!resource.exists()) {
                return;
            }
            String currentProvince = null;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!StringUtils.hasText(line)) {
                        continue;
                    }
                    String sectionProvince = parseSectionProvince(line);
                    if (sectionProvince != null) {
                        currentProvince = sectionProvince;
                        registerProvinceAlias(currentProvince);
                        continue;
                    }
                    if (currentProvince == null || !Character.isDigit(line.charAt(0))) {
                        continue;
                    }
                    String[] parts = splitCsvLine(line);
                    if (parts.length < 5) {
                        continue;
                    }
                    String prefecture = parsePrefectureShort(parts[4].trim());
                    if (StringUtils.hasText(prefecture)) {
                        registerMapping(prefecture, currentProvince);
                    }
                }
            }
        } catch (Exception ignored) {
            // 映射加载失败时仍可用省份名直接匹配
        }
    }

    /** 解析院校所在省（用于展示与筛选） */
    public String resolveProvince(String location, String city) {
        if (!StringUtils.hasText(location) && !StringUtils.hasText(city)) {
            return "未知";
        }
        if (StringUtils.hasText(location)) {
            if (ProvinceOrder.isProvince(location)) {
                return location;
            }
            String mapped = prefectureToProvince.get(location);
            if (mapped != null) {
                return mapped;
            }
        }
        if (StringUtils.hasText(city)) {
            String shortName = parsePrefectureShort(city);
            String mapped = prefectureToProvince.get(shortName);
            if (mapped != null) {
                return mapped;
            }
            if (ProvinceOrder.isProvince(shortName)) {
                return shortName;
            }
        }
        return StringUtils.hasText(location) ? location : "未知";
    }

    /** 某省下所有可用于 {@code location} 字段精确匹配的取值 */
    public List<String> locationValuesForProvince(String province) {
        if (!StringUtils.hasText(province)) {
            return List.of();
        }
        Set<String> values = new HashSet<>();
        values.add(province);
        List<String> prefectures = provinceToPrefectures.getOrDefault(province, List.of());
        values.addAll(prefectures);
        return new ArrayList<>(values);
    }

    private void registerProvinceAlias(String province) {
        prefectureToProvince.putIfAbsent(province, province);
        provinceToPrefectures.computeIfAbsent(province, k -> new ArrayList<>());
    }

    private void registerMapping(String prefecture, String province) {
        prefectureToProvince.put(prefecture, province);
        List<String> list = provinceToPrefectures.computeIfAbsent(province, k -> new ArrayList<>());
        if (!list.contains(prefecture)) {
            list.add(prefecture);
        }
        registerProvinceAlias(province);
    }

    private String parseSectionProvince(String line) {
        String first = line.split(",", 2)[0].trim();
        Matcher m = SECTION.matcher(first);
        if (!m.matches()) {
            return null;
        }
        return normalizeProvinceName(m.group(1));
    }

    private String normalizeProvinceName(String raw) {
        String s = raw.trim();
        if (s.startsWith("内蒙古")) {
            return "内蒙古";
        }
        if (s.startsWith("广西")) {
            return "广西";
        }
        if (s.startsWith("西藏")) {
            return "西藏";
        }
        if (s.startsWith("宁夏")) {
            return "宁夏";
        }
        if (s.startsWith("新疆")) {
            return "新疆";
        }
        if (s.endsWith("市")) {
            return s.substring(0, s.length() - 1);
        }
        if (s.endsWith("省")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String parsePrefectureShort(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        Matcher m = MUNICIPALITY.matcher(raw);
        if (m.matches()) {
            return m.group(1);
        }
        return raw;
    }

    private String[] splitCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (c == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur = new StringBuilder();
                continue;
            }
            cur.append(c);
        }
        cols.add(cur.toString());
        return cols.toArray(String[]::new);
    }
}
