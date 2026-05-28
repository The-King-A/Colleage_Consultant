package com.isoft.consultant.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 省级行政区标准排序（院校所在地、生源省等下拉列表）。
 */
public final class ProvinceOrder {

    private static final List<String> ORDER = List.of(
            "北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
            "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
            "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州",
            "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆"
    );

    private static final Map<String, Integer> INDEX = new HashMap<>();

    static {
        for (int i = 0; i < ORDER.size(); i++) {
            INDEX.put(ORDER.get(i), i);
        }
    }

    private ProvinceOrder() {
    }

    public static boolean isProvince(String name) {
        return name != null && INDEX.containsKey(name);
    }

    public static List<String> allProvinces() {
        return ORDER;
    }

    public static List<String> sort(List<String> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<String> sorted = new ArrayList<>(items);
        sorted.sort(Comparator
                .comparingInt((String p) -> INDEX.getOrDefault(p, Integer.MAX_VALUE))
                .thenComparing(Comparator.naturalOrder()));
        return sorted;
    }
}
