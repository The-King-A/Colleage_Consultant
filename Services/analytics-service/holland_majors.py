"""霍兰德代码 → 专业门类推荐（规则引擎，与 Java 测评互补）。"""

HOLLAND_MAJOR_MAP = {
    "R": {
        "label": "现实型",
        "categories": ["机械类", "土木类", "材料类", "能源动力类", "农业工程类"],
        "keywords": ["工程", "制造", "建筑", "农学"],
    },
    "I": {
        "label": "研究型",
        "categories": ["数学类", "物理学类", "化学类", "生物科学类", "统计学类"],
        "keywords": ["科研", "实验", "数据", "分析"],
    },
    "A": {
        "label": "艺术型",
        "categories": ["设计学类", "美术学类", "音乐与舞蹈学类", "戏剧与影视学类"],
        "keywords": ["设计", "艺术", "传媒", "创意"],
    },
    "S": {
        "label": "社会型",
        "categories": ["教育学类", "心理学类", "社会学类", "护理学类"],
        "keywords": ["教育", "心理", "社工", "护理"],
    },
    "E": {
        "label": "企业型",
        "categories": ["工商管理类", "经济学类", "法学类", "新闻传播学类"],
        "keywords": ["管理", "营销", "金融", "法律"],
    },
    "C": {
        "label": "常规型",
        "categories": ["会计学", "财务管理", "信息管理与信息系统", "图书馆学"],
        "keywords": ["会计", "审计", "档案", "行政"],
    },
}


def recommend_by_holland(holland_code: str, limit: int = 8) -> dict:
    code = (holland_code or "").upper().strip()[:3]
    if not code or not all(c in HOLLAND_MAJOR_MAP for c in code):
        raise ValueError("请提供有效的霍兰德代码，如 IRC、SAE")

    primary = code[0]
    secondary = code[1] if len(code) > 1 else None

    picks = []
    seen = set()
    for ch in code:
        info = HOLLAND_MAJOR_MAP[ch]
        for cat in info["categories"]:
            if cat not in seen:
                seen.add(cat)
                picks.append({
                    "hollandType": ch,
                    "category": cat,
                    "label": info["label"],
                    "priority": "primary" if ch == primary else "secondary",
                })

    return {
        "hollandCode": code,
        "primaryType": primary,
        "primaryLabel": HOLLAND_MAJOR_MAP[primary]["label"],
        "secondaryType": secondary,
        "recommendations": picks[:limit],
        "keywords": list({
            kw
            for ch in code
            for kw in HOLLAND_MAJOR_MAP[ch]["keywords"]
        }),
        "stack": "Python FastAPI",
    }
