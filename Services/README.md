# 扩展微服务（多技术栈）



与主工程 **Spring Boot + Vue** 配合，提供独立能力。



| 服务 | 技术栈 | 端口 | 功能 |

|------|--------|------|------|

| `analytics-service` | Python FastAPI | 8001 | 冲稳保测算、分数线洞察、霍兰德专业映射 |

| `report-service` | Node.js Express | 3001 | 志愿简报、霍兰德报告、院校对比表（统一 HTML 模板） |

| 主工程 `Backend` | Java Spring Boot | 8080 | 院校库、位次、向导、收藏、看板、**志愿方案**、录取样本补全、AI 问答/评审 |

## 前端页面与能力对应

| 页面 | 路由 | 依赖服务 |
|------|------|----------|
| 冲稳保测算 | `/matcher` | Python + Java（补全样本）+ Node（导出） |
| 分数线洞察 | `/insights` | Python |
| 位次换算 | `/rank` | Java |
| 填报向导 | `/wizard` | Java（需登录） |
| 我的志愿方案 | `/plans` | Java（需登录） |
| 地图选校 | `/school-map` | Java |
| 数据看板 | `/dashboard` | Java |
| 我的收藏 | `/favorites` | Java |
| 兴趣测评 | `/interest-test` | Java + Node（报告导出） |
| 院校对比 | `/compare` | Java + Node（表格式导出） |
| 智能问答 / 志愿推荐 | `/chat`、`/recommend` | Java（DeepSeek + Redis） |



## 启动



### 1. Python 分析服务



```bash

cd Services/analytics-service

pip install -r requirements.txt

python main.py

```



**API 示例：**



- `POST /api/v1/admission/match` — 冲稳保测算

- `POST /api/v1/admission/insights` — 录取分数分布洞察

- `POST /api/v1/holland/major-map` — 霍兰德代码 → 专业门类



环境变量（可选）：`DB_HOST` `DB_USER` `DB_PASSWORD` `DB_NAME`



### 2. Node 报告服务



```bash

cd Services/report-service

npm install

npm start

```



**API 示例：**



- `POST /api/v1/report/volunteer-brief` — 冲稳保简报

- `POST /api/v1/report/holland-brief` — 霍兰德测评报告

- `POST /api/v1/report/school-compare` — 院校对比表



模板集中在 `templates.js`，避免各 Vue 页面重复拼接 HTML。



### 3. 主工程



后端 `8080`、前端 `5173` 照常启动。前端已通过 Vite 代理：



- `/analytics` → `http://localhost:8001`

- `/report-api` → `http://localhost:3001`



**志愿方案表：** 若数据库为旧库，请执行：



```bash

mysql -u root -p volunteer_assistant < Backend/src/main/resources/sql/volunteer_plan.sql

```



未启动微服务时，对应页面会提示服务不可用，不影响院校库等核心功能。



## 去重说明



- 霍兰德报告、冲稳保简报、院校对比导出均走 **Node 统一模板**，不再在各页面内联拼接 HTML。

- 冲稳保「冲档为空」时，Python 会启用相对分档；**分数线洞察**页可查看样本分布原因。
- **补全样本**：`POST /api/schools/ensure-matcher-data`（Java）。同省只补本地线；跨省只向目标生源省扩展，避免全量 28 省插入超时。


