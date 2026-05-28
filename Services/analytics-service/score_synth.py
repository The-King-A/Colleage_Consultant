"""当库内录取线不足时，按院校层次与省控线合成样本（与 Java 补全逻辑一致）。"""

from db import get_conn
from province_resolver import prefectures_in_province, resolve_school_province

YEARS = [2024, 2023, 2022, 2021]
SCIENCE_YEAR_DELTA = {2021: 4, 2022: 2, 2023: 0, 2024: -1}
LIBERAL_YEAR_DELTA = {2021: 5, 2022: 3, 2023: 0, 2024: -2}

PROVINCE_OFFSET = {
    "北京": 12, "天津": 10, "上海": 14, "浙江": 8, "江苏": 6, "山东": 2,
    "河南": 0, "河北": 3, "湖北": 1, "湖南": 0, "广东": 5, "福建": 4,
    "四川": 3, "陕西": -2, "辽宁": 5, "吉林": 6, "黑龙江": 7,
}


def _province_line(score_province: str, subject_type: str, batch: str, year: int) -> int | None:
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT score FROM province_score_line
                WHERE province = %s AND subject_type = %s AND batch = %s AND year = %s
                LIMIT 1
                """,
                (score_province, subject_type, batch, year),
            )
            row = cur.fetchone()
            if row:
                return int(row["score"])
    off = PROVINCE_OFFSET.get(score_province, 2)
    base1 = 511 + off
    base2 = 407 + off
    if batch == "本科一批":
        return base1 + SCIENCE_YEAR_DELTA.get(year, 0) if subject_type == "science" else base1 - 45
    return base2 + SCIENCE_YEAR_DELTA.get(year, 0)


def _tier_base(school: dict, batch1: int, batch2: int) -> int:
    if school.get("is_985"):
        return batch1 + 95
    if school.get("is_211"):
        return batch1 + 55
    if school.get("is_double_first"):
        return batch1 + 35
    nature = school.get("school_nature") or ""
    if "民办" in str(nature):
        return batch2 - 15
    return batch2 + 25


def _resolve_batch(school: dict) -> str:
    if school.get("is_985") or school.get("is_211") or school.get("is_double_first"):
        return "本科一批"
    nature = school.get("school_nature") or ""
    if "民办" in str(nature):
        return "本科二批"
    return "本科二批"


def _fetch_schools_in_province(school_province: str) -> list[dict]:
    prefs = set(prefectures_in_province(school_province))
    sql = """
        SELECT school_code, school_name, location, city,
               is_985, is_211, is_double_first, school_nature
        FROM school_info
        WHERE status = 1
    """
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(sql)
            rows = cur.fetchall()
    out = []
    for row in rows:
        loc = row["location"] or ""
        city = row["city"] or ""
        resolved = resolve_school_province(loc, city)
        if resolved == school_province or loc in prefs:
            out.append(row)
    return out


def synthesize_rows(
    school_province: str,
    score_province: str,
    subject_type: str,
    years: list[int],
    existing_codes: set[str],
) -> list[dict]:
    """为省内院校合成缺失的录取线（仅补 existing_codes 中没有的）。"""
    schools = _fetch_schools_in_province(school_province)
    if not schools:
        return []

    year_delta = SCIENCE_YEAR_DELTA if subject_type == "science" else LIBERAL_YEAR_DELTA
    cross_delta = PROVINCE_OFFSET.get(score_province, 3) - PROVINCE_OFFSET.get(school_province, 0)
    if subject_type == "liberal":
        cross_delta -= 5

    rows = []
    for school in schools:
        code = school["school_code"]
        if code in existing_codes:
            continue
        batch = _resolve_batch(school)
        b1 = _province_line(score_province, subject_type, "本科一批", years[0]) or 511
        b2 = _province_line(score_province, subject_type, "本科二批", years[0]) or 407
        base = _tier_base(school, b1, b2) + cross_delta

        year = years[0]
        min_score = base + year_delta.get(year, 0)
        loc = resolve_school_province(school["location"], school["city"])
        rows.append({
            "school_code": code,
            "school_name": school["school_name"],
            "location": loc,
            "city": school["city"],
            "is_985": school.get("is_985"),
            "is_211": school.get("is_211"),
            "is_double_first": school.get("is_double_first"),
            "min_score": min_score,
            "min_rank": max(50, (750 - min_score) * 1200),
            "batch": batch,
            "year": year,
        })
    return rows
