# Backend（Spring Boot）

主后端服务，默认端口 **8080**，API 前缀 **`/api`**。

## 启动

```bash
mvn spring-boot:run
```

需提前准备：MySQL `volunteer_assistant`、Redis、有效的 DeepSeek / Embedding API 配置（见 `src/main/resources/application.yaml`）。

## 文档

完整说明请以仓库根目录为准：

- [../项目总览.md](../项目总览.md)
- [../核心技术.md](../核心技术.md)
- [../用户使用指南.md](../用户使用指南.md)

## SQL

`src/main/resources/sql/` — 建表、院校导入、志愿方案与一分一段表增量脚本。
