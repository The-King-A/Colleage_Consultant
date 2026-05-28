"""基于历年最低分的冲稳保分层（规则引擎，可复现）。"""

from db import get_conn
from province_resolver import prefectures_in_province, resolve_school_province
from score_synth import synthesize_rows

DEFAULT_YEARS = [2024, 2023, 2022, 2021]
MIN_CANDIDATES = 25


def is_valid_candidate(score_diff: int) -> bool:
    """纳入测算池的线差范围（样本数据可能扎堆，范围放宽）"""
    return -50 <= score_diff <= 150


def classify_tier_absolute(score_diff: int) -> str | None:
    """绝对线差分档：考生分低于院校线为冲"""
    if not is_valid_candidate(score_diff):
        return None
    if score_diff < 0:
        return "rush"
    if score_diff <= 35:
        return "stable"
    return "safe"


MIN_PER_TIER = 1


def _tag_tier(items: list[dict], tier: str) -> list[dict]:
    out = []
    for c in items:
        item = dict(c)
        item["tier"] = tier
        out.append(item)
    return out


def _three_way_sizes(n: int, limit_per_tier: int) -> tuple[int, int, int]:
    """冲/稳/保每档至少 1 所，且不超过 limit_per_tier。"""
    cap = max(MIN_PER_TIER, limit_per_tier)
    if n <= 0:
        return 0, 0, 0
    if n == 1:
        return 1, 1, 1
    if n == 2:
        return 1, 1, 1
    r = max(MIN_PER_TIER, n // 3)
    b = max(MIN_PER_TIER, n // 3)
    s = max(MIN_PER_TIER, n - r - b)
    r = min(cap, r)
    b = min(cap, b)
    s = min(cap, max(MIN_PER_TIER, n - r - b))
    # 若 cap 截断后稳档为 0，从最大档挪 1 所到中间
    while r + s + b > n:
        if s > MIN_PER_TIER:
            s -= 1
        elif b > MIN_PER_TIER:
            b -= 1
        elif r > MIN_PER_TIER:
            r -= 1
        else:
            break
    while r + s + b < n:
        if s < cap:
            s += 1
        elif r < cap:
            r += 1
        elif b < cap:
            b += 1
        else:
            break
    return r, s, b


def _partition_guaranteed(candidates: list[dict], limit_per_tier: int) -> tuple[list, list, list]:
    """
    按线差从难到易三等分，保证冲/稳/保每档至少 MIN_PER_TIER 所（候选≥3 时不出现空档）。
    线差越小（负得越多）越难 → 冲；线差越大越容易 → 保。
    """
    ordered = sorted(
        candidates,
        key=lambda x: (x["scoreDiff"], x["minScore"]),
    )
    n = len(ordered)
    if n == 0:
        return [], [], []
    if n == 1:
        one = ordered[0]
        return (
            _tag_tier([one], "rush"),
            _tag_tier([dict(one)], "stable"),
            _tag_tier([dict(one)], "safe"),
        )
    if n == 2:
        harder, easier = ordered[0], ordered[1]
        return (
            _tag_tier([harder], "rush"),
            _tag_tier([dict(easier)], "stable"),
            _tag_tier([dict(easier)], "safe"),
        )

    r_n, s_n, b_n = _three_way_sizes(n, limit_per_tier)
    i1 = r_n
    i2 = r_n + s_n
    return (
        _tag_tier(ordered[:i1], "rush"),
        _tag_tier(ordered[i1:i2], "stable"),
        _tag_tier(ordered[i2 : i2 + b_n], "safe"),
    )


def _assign_tiers_absolute(candidates: list[dict], limit_per_tier: int) -> tuple[list, list, list]:
    rush = _tag_tier(
        sorted([c for c in candidates if c["scoreDiff"] < 0], key=lambda x: x["scoreDiff"])[
            :limit_per_tier
        ],
        "rush",
    )
    stable = _tag_tier(
        sorted(
            [c for c in candidates if 0 <= c["scoreDiff"] <= 35],
            key=lambda x: abs(x["scoreDiff"]),
        )[:limit_per_tier],
        "stable",
    )
    safe = _tag_tier(
        sorted(
            [c for c in candidates if c["scoreDiff"] > 35],
            key=lambda x: -x["scoreDiff"],
        )[:limit_per_tier],
        "safe",
    )
    return rush, stable, safe


def _assign_tiers(candidates: list[dict], limit_per_tier: int) -> tuple[list, list, list, str]:
    if not candidates:
        return [], [], [], "absolute"

    min_scores = [c["minScore"] for c in candidates]
    spread = max(min_scores) - min(min_scores) if min_scores else 0
    rush_abs = [c for c in candidates if c["scoreDiff"] < 0]

    use_relative = len(candidates) >= 3 and (len(rush_abs) == 0 or spread < 100)
    if use_relative:
        rush, stable, safe = _partition_guaranteed(candidates, limit_per_tier)
        return rush, stable, safe, "relative"

    rush, stable, safe = _assign_tiers_absolute(candidates, limit_per_tier)
    if len(candidates) >= MIN_PER_TIER * 3 and (
        len(rush) == 0 or len(stable) == 0 or len(safe) == 0
    ):
        rush, stable, safe = _partition_guaranteed(candidates, limit_per_tier)
        return rush, stable, safe, "balanced"

    if len(candidates) >= 1 and (len(rush) == 0 or len(stable) == 0 or len(safe) == 0):
        rush, stable, safe = _partition_guaranteed(candidates, limit_per_tier)
        return rush, stable, safe, "balanced"

    return rush, stable, safe, "absolute"


def _fetch_rows(score_province: str, subject_type: str, years: list[int]):
    placeholders = ",".join(["%s"] * len(years))
    sql = f"""
        SELECT s.school_code, s.school_name, s.location, s.city,
               s.is_985, s.is_211, s.is_double_first,
               a.min_score, a.min_rank, a.batch, a.year
        FROM school_info s
        INNER JOIN admission_score a ON s.school_code = a.school_code
        WHERE s.status = 1
          AND a.province = %s
          AND a.subject_type = %s
          AND a.year IN ({placeholders})
          AND (a.major_code IS NULL OR a.major_code = '')
          AND a.min_score IS NOT NULL
        ORDER BY a.year DESC, a.min_score ASC
    """
    params = [score_province, subject_type, *years]
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            return cur.fetchall()


def match_schools(
    school_province: str,
    subject_type: str,
    user_score: int,
    score_province: str | None = None,
    year: int | None = None,
    limit_per_tier: int = 20,
) -> dict:
    score_province = score_province or school_province
    years = [year] if year else DEFAULT_YEARS
    rows = _fetch_rows(score_province, subject_type, years)
    prefs = set(prefectures_in_province(school_province))

    def in_school_province(row: dict) -> bool:
        loc = row.get("location") or ""
        city = row.get("city") or ""
        resolved = resolve_school_province(loc, city)
        loc_short = loc.replace("市", "") if loc else ""
        return resolved == school_province or loc in prefs or loc_short in prefs

    # 同一院校取最近一年，且仅保留目标省院校
    by_school: dict[str, dict] = {}
    for row in rows:
        if not in_school_province(row):
            continue
        code = row["school_code"]
        if code not in by_school:
            by_school[code] = row

    used_synth = False
    if len(by_school) < MIN_CANDIDATES:
        synth = synthesize_rows(
            school_province, score_province, subject_type, years, set(by_school.keys())
        )
        for row in synth:
            by_school[row["school_code"]] = row
        used_synth = len(synth) > 0

    data_source_note = "synthetic" if used_synth else "database"
    candidates: list[dict] = []

    for row in by_school.values():
        loc = row.get("location") or ""
        city = row.get("city") or ""
        resolved = resolve_school_province(loc, city)

        diff = user_score - int(row["min_score"])
        if not is_valid_candidate(diff):
            continue

        candidates.append({
            "schoolCode": row["school_code"],
            "schoolName": row["school_name"],
            "location": resolved,
            "city": city,
            "is985": bool(row.get("is_985")),
            "is211": bool(row.get("is_211")),
            "isDoubleFirst": bool(row.get("is_double_first")),
            "minScore": row["min_score"],
            "minRank": row.get("min_rank"),
            "batch": row.get("batch"),
            "year": row["year"],
            "scoreDiff": diff,
        })

    rush, stable, safe, tier_mode = _assign_tiers(candidates, limit_per_tier)
    # 兜底：候选≥3 时绝不让任一档为空
    if len(candidates) >= 3 and (
        len(rush) == 0 or len(stable) == 0 or len(safe) == 0
    ):
        rush, stable, safe = _partition_guaranteed(candidates, limit_per_tier)
        tier_mode = "balanced"

    return {
        "schoolProvince": school_province,
        "scoreProvince": score_province,
        "subjectType": subject_type,
        "userScore": user_score,
        "dataYear": years[0],
        "tierMode": tier_mode,
        "rush": rush,
        "stable": stable,
        "safe": safe,
        "summary": {
            "rushCount": len(rush),
            "stableCount": len(stable),
            "safeCount": len(safe),
            "totalMatched": len(candidates),
            "tierMode": tier_mode,
            "dataSource": data_source_note,
        },
    }
