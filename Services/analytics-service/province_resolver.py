"""地级市 -> 省级行政区（与 Java CityProvinceResolver 同源逻辑）。"""

import re
from pathlib import Path

PREFECTURE_TO_PROVINCE: dict[str, str] = {}
PROVINCE_PREFECTURES: dict[str, set[str]] = {}


def _normalize_section(raw: str) -> str | None:
    first = raw.split(",", 1)[0].strip()
    m = re.match(r"^(.+?)(?:省|自治区|特别行政区)?（\d+所）", first)
    if not m:
        return None
    s = m.group(1).strip()
    if s.startswith("内蒙古"):
        return "内蒙古"
    if s.startswith("广西"):
        return "广西"
    if s.startswith("西藏"):
        return "西藏"
    if s.startswith("宁夏"):
        return "宁夏"
    if s.startswith("新疆"):
        return "新疆"
    if s.endswith("市"):
        return s[:-1]
    if s.endswith("省"):
        return s[:-1]
    return s


def _prefecture_short(raw: str) -> str:
    raw = (raw or "").strip()
    m = re.match(r"^(.+?)市$", raw)
    return m.group(1) if m else raw


def _load_moe_mapping():
    global PREFECTURE_TO_PROVINCE, PROVINCE_PREFECTURES
    if PREFECTURE_TO_PROVINCE:
        return
    path = Path(__file__).resolve().parents[2] / "Backend/src/main/resources/data/moe_schools_raw.csv"
    if not path.exists():
        _bootstrap_fallback()
        return
    current = None
    try:
        text = path.read_text(encoding="utf-8-sig")
        for line in text.splitlines():
            line = line.strip()
            if not line:
                continue
            sec = _normalize_section(line)
            if sec:
                current = sec
                PROVINCE_PREFECTURES.setdefault(sec, set())
                PROVINCE_PREFECTURES[sec].add(sec)
                continue
            if current and line and line[0].isdigit():
                parts = line.split(",")
                if len(parts) >= 5:
                    pref = _prefecture_short(parts[4].strip())
                    if pref:
                        PREFECTURE_TO_PROVINCE[pref] = current
                        PROVINCE_PREFECTURES[current].add(pref)
    except Exception:
        _bootstrap_fallback()


def _bootstrap_fallback():
    """无 MOE 文件时的最小映射"""
    for p in [
        "北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江",
        "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
        "广东", "广西", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海",
        "内蒙古", "西藏", "宁夏", "新疆",
    ]:
        PROVINCE_PREFECTURES[p] = {p}
        PREFECTURE_TO_PROVINCE[p] = p
    extras = {
        "南京": "江苏", "苏州": "江苏", "无锡": "江苏", "常州": "江苏",
        "广州": "广东", "深圳": "广东", "珠海": "广东",
        "杭州": "浙江", "宁波": "浙江",
        "武汉": "湖北", "成都": "四川", "西安": "陕西",
        "桂林": "广西", "三亚": "海南", "三明": "福建",
        "郑州": "河南", "开封": "河南", "洛阳": "河南", "新乡": "河南",
        "安阳": "河南", "焦作": "河南", "平顶山": "河南", "南阳": "河南",
        "信阳": "河南", "周口": "河南", "驻马店": "河南", "商丘": "河南",
        "许昌": "河南", "漯河": "河南", "濮阳": "河南", "鹤壁": "河南",
        "三门峡": "河南", "济源": "河南",
    }
    for city, prov in extras.items():
        PREFECTURE_TO_PROVINCE[city] = prov
        PROVINCE_PREFECTURES.setdefault(prov, set()).add(city)


def resolve_school_province(location: str | None, city: str | None) -> str:
    _load_moe_mapping()
    if location and location in PROVINCE_PREFECTURES:
        return location
    if location and location in PREFECTURE_TO_PROVINCE:
        return PREFECTURE_TO_PROVINCE[location]
    if city:
        short = _prefecture_short(city)
        if short in PREFECTURE_TO_PROVINCE:
            return PREFECTURE_TO_PROVINCE[short]
    return location or "未知"


def prefectures_in_province(province: str) -> list[str]:
    _load_moe_mapping()
    prefs = PROVINCE_PREFECTURES.get(province, {province})
    return list(prefs)

_load_moe_mapping()
