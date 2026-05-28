# 全国本科院校数据导入说明

数据来源：教育部公布的《全国普通高等学校名单》（本科层次，约 1300 所）。  
项目已整理为 **1243 所** 本科院校（已去重、排除专科行）。

## 文件位置

| 文件 | 说明 |
|------|------|
| `sql/import_china_undergraduate_schools.sql` | **推荐**：一键导入，可重复执行（`INSERT IGNORE`） |
| `data/china_undergraduate_schools.csv` | CSV 格式，可供程序或 Excel 查看 |
| `data/moe_schools_raw.csv` | 教育部原始名单（含专科） |
| `scripts/generate_undergraduate_schools.py` | 从原始 CSV 重新生成上述文件 |

## 手动导入（推荐）

在 MySQL 中执行：

```sql
USE volunteer_assistant;
SOURCE E:/All_Projects_done/Consultant/Consultant/Backend/src/main/resources/sql/import_china_undergraduate_schools.sql;
```

或在 Navicat / DBeaver 中打开 `import_china_undergraduate_schools.sql` 运行。

导入后验证：

```sql
SELECT COUNT(*) FROM school_info WHERE status = 1;
SELECT school_name, location, city FROM school_info WHERE location = '北京' LIMIT 20;
```

## 启动时自动导入（可选，默认关闭）

本地已用 SQL 手动导库时，**不要**开启下列开关（默认均为 `false`）：

```yaml
consultant:
  data:
    run-sql-on-startup: false              # 不在启动时执行 schema.sql / 演示种子
    import-undergraduate-schools: false    # 不在启动时从 CSV 导院校
    seed-sample-admission-scores: false    # 不在启动时用 Java 生成录取样本
```

仅全新空库、且希望由程序自动建表/灌演示数据时，可临时将 `run-sql-on-startup` 设为 `true`。  
从 CSV 导全国本科需另设 `import-undergraduate-schools: true`（库中少于 1200 所才会执行）。

## 与旧种子数据的关系

- `volunteer_assistant_seed.sql`、`seed_more_data.sql` 为早期演示数据，**不必再执行**。
- 导入全国名单后，`school_code` 与教育部标识码后 5 位一致（如北大 `10001`）。
- 录取分数线仍由 `AdmissionScoreEnricher` 等组件按样本规则补全，非官方全量线。

## 重新生成数据

若更新了 `moe_schools_raw.csv`：

```bash
cd Backend
python scripts/generate_undergraduate_schools.py
```
