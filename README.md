# 智选未来 · 高考志愿助手（Consultant）

AI 驱动的志愿填报平台：院校库、冲稳保测算、填报向导、志愿方案与 AI 评审，多技术栈协同（Java + Vue + Python + Node）。

## 文档导航

| 文档 | 说明 |
|------|------|
| [项目总览.md](./项目总览.md) | 目录结构、模块职责、数据与部署要点 |
| [核心技术.md](./核心技术.md) | 技术栈、架构、API 与关键实现 |
| [用户使用指南.md](./用户使用指南.md) | 环境准备 → 启动 → 完整填报流程 |
| [答辩亮点总结.md](./答辩亮点总结.md) | 答辩用：创新点、演示流程、预设问答 |

## 快速启动

```bash
# 1. MySQL：创建库并导入（见用户使用指南）
# 2. Java 后端
cd Backend && mvn spring-boot:run

# 3. 前端
cd Front/Consultant && npm install && npm run dev

# 4. 可选：冲稳保 / 报告（对应页面需要）
cd Services/analytics-service && pip install -r requirements.txt && python main.py
cd Services/report-service && npm install && npm start
```

- 前端：<http://localhost:5173>
- 后端 API：<http://localhost:8080/api>
- Python 分析：<http://localhost:8001>
- Node 报告：<http://localhost:3001>

微服务说明见 [Services/README.md](./Services/README.md)。

## 上传 GitHub

上传前请阅读 **[上传GitHub指南.md](./上传GitHub指南.md)**。真实 API Key 放在 `application-local.yaml`（已加入 `.gitignore`），勿提交到仓库。
