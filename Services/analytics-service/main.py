from typing import Any, Optional

from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, model_validator

from holland_majors import recommend_by_holland
from matcher import match_schools
from score_insights import get_score_insights

app = FastAPI(
    title="志愿分析服务",
    description="Python FastAPI · 冲稳保测算、分数线洞察、霍兰德专业映射",
    version="1.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class InsightsRequest(BaseModel):
    model_config = {"extra": "ignore"}

    scoreProvince: Optional[str] = None
    schoolProvince: Optional[str] = None
    subjectType: Optional[str] = Field(default="science")
    subject_type: Optional[str] = None
    score: Optional[int] = None
    year: Optional[int] = None
    province: Optional[str] = None

    @model_validator(mode="before")
    @classmethod
    def normalize(cls, data: Any) -> Any:
        if not isinstance(data, dict):
            return data
        d = dict(data)
        if not d.get("scoreProvince") and d.get("province"):
            d["scoreProvince"] = d["province"]
        if not d.get("subjectType") and d.get("subject_type"):
            d["subjectType"] = d["subject_type"]
        if d.get("score") is not None and d.get("score") != "":
            d["score"] = int(float(d["score"]))
        return d

    @model_validator(mode="after")
    def validate_required(self):
        if not self.scoreProvince or not str(self.scoreProvince).strip():
            raise ValueError("请提供 scoreProvince 或 province（录取生源省）")
        if not self.subjectType:
            self.subjectType = "science"
        return self


class HollandRequest(BaseModel):
    hollandCode: str = Field(..., min_length=2, max_length=6)
    limit: Optional[int] = Field(default=8, ge=1, le=20)


class MatchRequest(BaseModel):
    """兼容 schoolProvince / province、subjectType / subject_type 等多种前端字段名"""

    model_config = {"extra": "ignore"}

    schoolProvince: Optional[str] = None
    scoreProvince: Optional[str] = None
    subjectType: Optional[str] = Field(default="science")
    subject_type: Optional[str] = None
    score: Optional[int] = None
    year: Optional[int] = None
    province: Optional[str] = None

    @model_validator(mode="before")
    @classmethod
    def normalize_fields(cls, data: Any) -> Any:
        if not isinstance(data, dict):
            return data
        d = dict(data)
        if not d.get("schoolProvince") and d.get("province"):
            d["schoolProvince"] = d["province"]
        if not d.get("subjectType") and d.get("subject_type"):
            d["subjectType"] = d["subject_type"]
        if d.get("score") is not None and d.get("score") != "":
            d["score"] = int(float(d["score"]))
        return d

    @model_validator(mode="after")
    def validate_required(self):
        if not self.schoolProvince or not str(self.schoolProvince).strip():
            raise ValueError("请提供 schoolProvince 或 province（院校所在地）")
        if self.score is None:
            raise ValueError("请提供 score（高考分数）")
        if self.score < 0 or self.score > 750:
            raise ValueError("score 应在 0-750 之间")
        if not self.subjectType:
            self.subjectType = "science"
        return self


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    errors = exc.errors()
    msg = "; ".join(
        f"{'.'.join(str(x) for x in e.get('loc', []))}: {e.get('msg')}" for e in errors
    )
    return JSONResponse(
        status_code=422,
        content={"detail": msg or "请求参数无效", "errors": errors},
    )


@app.get("/health")
def health():
    return {"status": "ok", "service": "analytics-service", "stack": "Python FastAPI"}


@app.post("/api/v1/admission/insights")
def admission_insights(req: InsightsRequest):
    try:
        score_prov = (req.scoreProvince or "").strip()
        school_prov = (req.schoolProvince or "").strip() or None
        return get_score_insights(
            score_province=score_prov,
            subject_type=req.subjectType or "science",
            school_province=school_prov,
            user_score=req.score,
            year=req.year,
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e)) from e


@app.post("/api/v1/holland/major-map")
def holland_major_map(req: HollandRequest):
    try:
        return recommend_by_holland(req.hollandCode, req.limit or 8)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e


@app.post("/api/v1/admission/match")
def admission_match(req: MatchRequest):
    try:
        school_prov = (req.schoolProvince or "").strip()
        score_prov = (req.scoreProvince or school_prov).strip()
        return match_schools(
            school_province=school_prov,
            score_province=score_prov,
            subject_type=req.subjectType or "science",
            user_score=req.score,
            year=req.year,
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e)) from e


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("main:app", host="0.0.0.0", port=8001, reload=True)
