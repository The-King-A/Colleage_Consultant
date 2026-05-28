package com.isoft.consultant.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 地级市 / 省份 → 经纬度（用于地图选校，无 PostGIS 时的轻量方案）。
 */
@Component
public class CityGeoResolver {

    private final Map<String, double[]> cityCoords = new HashMap<>();
    private final Map<String, double[]> provinceCoords = new HashMap<>();
    private final ObjectMapper objectMapper;

    public CityGeoResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void load() {
        try {
            ClassPathResource resource = new ClassPathResource("data/city_geo.json");
            if (!resource.exists()) {
                return;
            }
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            loadSection(root.path("cities"), cityCoords);
            loadSection(root.path("provinces"), provinceCoords);
        } catch (Exception ignored) {
            // 坐标库加载失败时地图选校将跳过无坐标院校
        }
    }

    private void loadSection(JsonNode node, Map<String, double[]> target) {
        if (!node.isObject()) {
            return;
        }
        node.fields().forEachRemaining(entry -> {
            JsonNode arr = entry.getValue();
            if (arr.isArray() && arr.size() >= 2) {
                target.put(entry.getKey(), new double[] { arr.get(0).asDouble(), arr.get(1).asDouble() });
                String shortName = entry.getKey().replace("市", "");
                if (!shortName.equals(entry.getKey())) {
                    target.putIfAbsent(shortName, target.get(entry.getKey()));
                }
            }
        });
    }

    /**
     * @return [lat, lng] 或 null
     */
    public double[] resolve(String location, String city, String province) {
        if (StringUtils.hasText(city)) {
            double[] c = lookup(cityCoords, city);
            if (c != null) {
                return c;
            }
        }
        if (StringUtils.hasText(location)) {
            double[] loc = lookup(cityCoords, location);
            if (loc != null) {
                return loc;
            }
            if (ProvinceOrder.isProvince(location)) {
                return provinceCoords.get(location);
            }
        }
        if (StringUtils.hasText(province)) {
            return provinceCoords.get(province);
        }
        return null;
    }

    /** 同城多校时螺旋偏移，避免标记完全重叠 */
    public double[] offset(double[] base, int index) {
        if (base == null || index <= 0) {
            return base;
        }
        double angle = index * 0.85;
        double r = 0.015 + index * 0.006;
        return new double[] {
            base[0] + r * Math.sin(angle),
            base[1] + r * Math.cos(angle)
        };
    }

    private double[] lookup(Map<String, double[]> map, String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        String key = name.trim();
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (key.endsWith("市")) {
            return map.get(key.substring(0, key.length() - 1));
        }
        return map.get(key + "市");
    }
}
