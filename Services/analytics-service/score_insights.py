"""录取分数分布洞察（帮助理解样本线扎堆、冲档为空等原因）。"""

from collections import Counter

from db import get_conn


def _fetch_min_scores(score_province: str, subject_type: str, years: list[int], school_province: str | None):
    placeholders = ",".join(["%s"] * len(years))
    sql = f"""
        SELECT a.min_score, s.school_code, s.location, s.city
        FROM admission_score a
        INNER JOIN school_info s ON s.school_code = a.school_code
        WHERE s.status = 1
          AND a.province = %s
          AND a.subject_type = %s
          AND a.year IN ({placeholders})
          AND (a.major_code IS NULL OR a.major_code = '')
          AND a.min_score IS NOT NULL
    """
    params = [score_province, subject_type, *years]
    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            rows = cur.fetchall()

    if not school_province:
        return [int(r["min_score"]) for r in rows]

    from province_resolver import prefectures_in_province, resolve_school_province

    prefs = set(prefectures_in_province(school_province))
    scores = []
    for r in rows:
        loc = r["location"] or ""
        city = r["city"] or ""
        resolved = resolve_school_province(loc, city)
        if resolved == school_province or loc in prefs:
            scores.append(int(r["min_score"]))
    return scores


def _histogram(scores: list[int], step: int = 10) -> list[dict]:
    if not scores:
        return []
    lo = (min(scores) // step) * step
    hi = ((max(scores) // step) + 1) * step
    buckets = Counter()
    for s in scores:
        b = (s // step) * step
        buckets[b] += 1
    return [{"from": k, "to": k + step - 1, "count": buckets[k]} for k in sorted(buckets.keys())]


def _percentile(sorted_scores: list[int], p: float) -> int | None:
    if not sorted_scores:
        return None
    idx = int(round((len(sorted_scores) - 1) * p))
    return sorted_scores[max(0, min(idx, len(sorted_scores) - 1))]


def get_score_insights(
    score_province: str,
    subject_type: str,
    school_province: str | None = None,
    user_score: int | None = None,
    year: int | None = None,
) -> dict:
    years = [year] if year else [2024, 2023, 2022]
    scores = _fetch_min_scores(score_province, subject_type, years, school_province)
    if not scores:
        return {
            "scoreProvince": score_province,
            "schoolProvince": school_province,
            "subjectType": subject_type,
            "dataYear": years[0],
            "sampleCount": 0,
            "histogram": [],
            "stats": None,
            "userPosition": None,
            "tips": ["暂无该条件下的录取样本，请更换生源省或科类。"],
        }

    sorted_scores = sorted(scores)
    n = len(sorted_scores)
    spread = sorted_scores[-1] - sorted_scores[0]
    top_scores = Counter(sorted_scores).most_common(5)

    user_pos = None
    tips = []
    if user_score is not None:
        below = sum(1 for s in sorted_scores if s <= user_score)
        above_line = sum(1 for s in sorted_scores if s > user_score)
        user_pos = {
            "score": user_score,
            "percentile": round(100 * below / n, 1),
            "schoolsAboveLine": above_line,
            "schoolsAtOrBelow": below,
        }
        if above_line == 0:
            tips.append("你的分数高于样本中全部院校最低线，绝对规则下「冲」档可能为空，系统会启用相对分档。")
        if spread < 100:
            tips.append(f"样本最低分跨度仅 {spread} 分，线差易扎堆，建议结合「分数线洞察」理解冲稳保结果。")

    score_clusters = ", ".join(f"{s}分×{c}" for s, c in top_scores[:3])
    if score_clusters:
        tips.append(f"常见最低分簇：{score_clusters}。")

    return {
        "scoreProvince": score_province,
        "schoolProvince": school_province,
        "subjectType": subject_type,
        "dataYear": years[0],
        "sampleCount": n,
        "histogram": _histogram(sorted_scores),
        "stats": {
            "min": sorted_scores[0],
            "max": sorted_scores[-1],
            "mean": round(sum(sorted_scores) / n, 1),
            "median": _percentile(sorted_scores, 0.5),
            "p25": _percentile(sorted_scores, 0.25),
            "p75": _percentile(sorted_scores, 0.75),
            "spread": spread,
        },
        "userPosition": user_pos,
        "tips": tips,
    }
