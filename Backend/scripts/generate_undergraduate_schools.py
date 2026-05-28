#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从教育部全国普通高等学校名单 CSV 生成本科院校导入文件。"""
import csv
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "src/main/resources/data/moe_schools_raw.csv"
OUT_CSV = ROOT / "src/main/resources/data/china_undergraduate_schools.csv"
OUT_SQL = ROOT / "src/main/resources/sql/import_china_undergraduate_schools.sql"

# 985 / 211 / 双一流（常见名单，用于标记）
S985 = {
    "北京大学", "中国人民大学", "清华大学", "北京航空航天大学", "北京理工大学",
    "中国农业大学", "北京师范大学", "中央民族大学", "南开大学", "天津大学",
    "大连理工大学", "吉林大学", "哈尔滨工业大学", "复旦大学", "同济大学",
    "上海交通大学", "华东师范大学", "南京大学", "东南大学", "浙江大学",
    "中国科学技术大学", "厦门大学", "山东大学", "中国海洋大学", "武汉大学",
    "华中科技大学", "湖南大学", "中南大学", "中山大学", "华南理工大学",
    "四川大学", "重庆大学", "电子科技大学", "西安交通大学", "西北工业大学",
    "兰州大学", "国防科技大学", "东北大学", "郑州大学", "云南大学",
    "新疆大学", "海南大学", "宁夏大学", "青海大学", "西藏大学",
    "内蒙古大学", "广西大学", "贵州大学", "石河子大学", "中国石油大学（北京）",
    "中国地质大学（北京）", "中国矿业大学（北京）",
}
S211 = S985 | {
    "北京交通大学", "北京工业大学", "北京科技大学", "北京化工大学", "北京邮电大学",
    "北京林业大学", "北京中医药大学", "北京外国语大学", "中国传媒大学", "中央财经大学",
    "对外经济贸易大学", "北京体育大学", "中央音乐学院", "中国政法大学", "华北电力大学",
    "天津医科大学", "河北工业大学", "太原理工大学", "内蒙古大学", "辽宁大学",
    "大连海事大学", "延边大学", "东北师范大学", "哈尔滨工程大学", "东北农业大学",
    "东北林业大学", "华东理工大学", "东华大学", "上海财经大学", "上海大学",
    "苏州大学", "南京航空航天大学", "南京理工大学", "中国矿业大学", "河海大学",
    "江南大学", "南京农业大学", "中国药科大学", "南京师范大学", "安徽大学",
    "合肥工业大学", "福州大学", "南昌大学", "郑州大学", "武汉理工大学",
    "华中农业大学", "华中师范大学", "中南财经政法大学", "湖南师范大学",
    "暨南大学", "华南师范大学", "广西大学", "西南交通大学", "西南财经大学",
    "四川农业大学", "西南大学", "贵州大学", "云南大学", "西北大学",
    "西安电子科技大学", "长安大学", "陕西师范大学", "青海大学", "宁夏大学",
    "新疆大学", "石河子大学", "第二军医大学", "第四军医大学",
    "北京协和医学院", "首都医科大学", "中国医科大学", "哈尔滨医科大学",
    "上海中医药大学", "南京医科大学", "广州医科大学", "南方医科大学",
}


def parse_location(raw: str) -> tuple[str, str]:
    raw = (raw or "").strip()
    if not raw:
        return "未知", "未知"
    # 北京市、天津市等
    m = re.match(r"^(.+?市)$", raw)
    if m:
        name = m.group(1)
        short = name[:-1] if name.endswith("市") else name
        return short, name
    return raw, raw


def school_code(moe_code: str) -> str:
    digits = re.sub(r"\D", "", moe_code or "")
    if len(digits) >= 5:
        return digits[-5:]
    return digits.zfill(5) if digits else "00000"


def parse_section_province(line: str) -> str | None:
    first = line.split(",", 1)[0].strip()
    m = re.match(r"^(.+?)(?:省|自治区|特别行政区)?（\d+所）", first)
    if not m:
        return None
    raw = m.group(1)
    if raw.startswith("内蒙古"):
        return "内蒙古"
    if raw.startswith("广西"):
        return "广西"
    if raw.startswith("西藏"):
        return "西藏"
    if raw.startswith("宁夏"):
        return "宁夏"
    if raw.startswith("新疆"):
        return "新疆"
    if raw.endswith("市"):
        return raw[:-1]
    if raw.endswith("省"):
        return raw[:-1]
    return raw


def parse_rows(text: str) -> list[dict]:
    rows = []
    buf = []
    current_province = None
    for line in text.splitlines():
        if not line.strip():
            continue
        section = parse_section_province(line)
        if section:
            current_province = section
            buf = []
            continue
        buf.append(line)
        joined = "\n".join(buf)
        try:
            parsed = next(csv.reader([joined]))
        except Exception:
            continue
        if len(parsed) < 6:
            continue
        if not parsed[0].strip().isdigit():
            buf = []
            continue
        buf = []
        name = parsed[1].strip().replace("\n", "")
        moe = parsed[2].strip()
        level = parsed[5].strip() if len(parsed) > 5 else ""
        remark = parsed[6].strip() if len(parsed) > 6 else ""
        if level != "本科":
            continue
        _, city = parse_location(parsed[4].strip())
        province = current_province or "未知"
        nature = "民办" if "民办" in remark else "公办"
        code = school_code(moe)
        rows.append({
            "school_code": code,
            "school_name": name,
            "location": province,
            "city": city,
            "school_nature": nature,
            "school_level": "本科",
            "is985": 1 if name in S985 else 0,
            "is211": 1 if name in S211 else 0,
            "is_double_first": 1 if name in S985 or name in S211 else 0,
            "description": f"{name}是教育部公布的普通高等学校（本科），位于{province}·{city}。",
        })
    return rows


def main():
    if not RAW.exists():
        print(f"缺少原始文件: {RAW}")
        print("请从教育部名单下载 CSV 保存为 moe_schools_raw.csv")
        return 1
    text = RAW.read_text(encoding="utf-8-sig")
    schools = parse_rows(text)
    # 按 school_code 去重
    seen = {}
    for s in schools:
        seen[s["school_code"]] = s
    schools = list(seen.values())
    schools.sort(key=lambda x: (x["location"], x["school_name"]))

    OUT_CSV.parent.mkdir(parents=True, exist_ok=True)
    fields = [
        "school_code", "school_name", "location", "city", "school_nature",
        "school_level", "is985", "is211", "is_double_first", "description",
    ]
    with OUT_CSV.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        w.writerows(schools)

    sql_lines = [
        "-- 全国普通高等学校本科名单（教育部公布，平台整理导入）",
        "-- 可重复执行：依赖 school_code 唯一键，重复请改用 INSERT IGNORE 或先清空",
        "USE volunteer_assistant;",
        "",
    ]
    batch = []
    for s in schools:
        desc = s["description"].replace("'", "''")
        name = s["school_name"].replace("'", "''")
        batch.append(
            f"('{s['school_code']}','{name}','本科','{s['school_nature']}','{s['location']}','{s['city']}',"
            f"{s['is985']},{s['is211']},{s['is_double_first']},'{desc}',1)"
        )
    sql_lines.append(
        "INSERT IGNORE INTO school_info "
        "(school_code,school_name,school_type,school_nature,location,city,is_985,is_211,is_double_first,description,status) VALUES"
    )
    chunk = 80
    for i in range(0, len(batch), chunk):
        part = batch[i : i + chunk]
        sql_lines.append(",\n".join(part) + (";" if i + chunk >= len(batch) else ","))

    OUT_SQL.write_text("\n".join(sql_lines), encoding="utf-8")
    print(f"本科院校 {len(schools)} 所")
    print(f"CSV -> {OUT_CSV}")
    print(f"SQL -> {OUT_SQL}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
