# 上传 GitHub 指南（密钥安全）

## 已做的处理

| 文件 | 说明 |
|------|------|
| `.gitignore` | 忽略 `application-local.yaml`、`.env`、`node_modules`、`target` 等 |
| `application.yaml` | 仓库内**无真实密钥**，使用环境变量占位 |
| `application.yaml.example` | 配置模板，可提交 |
| `application-local.yaml` | **仅本机**，含真实配置，已被忽略，不会上传 |

## 上传前自检（必做）

在项目根目录执行（PowerShell）：

```powershell
# 应无输出；若有输出说明仍含疑似密钥，上传前需处理
git grep -i "sk-" -- ':!*.example' ':!上传GitHub指南.md' 2>$null
git grep -i "tvly-" -- ':!*.example' 2>$null
```

或手动确认 **不要提交**：

- `Backend/src/main/resources/application-local.yaml`
- 任何 `.env` 文件（除 `.env.example`）
- `Backend/target/`、`node_modules/`

## 首次上传到 GitHub

```bash
cd E:\All_Projects_done\Consultant\Consultant

git init
git add .
git status
# 确认列表中没有 application-local.yaml、.env

git commit -m "Initial commit: 高考志愿助手 Consultant"
git branch -M main
git remote add origin https://github.com/你的用户名/你的仓库名.git
git push -u origin main
```

## 克隆后他人如何配置

1. 复制后端配置：
   ```bash
   copy Backend\src\main\resources\application.yaml.example Backend\src\main\resources\application-local.yaml
   ```
2. 编辑 `application-local.yaml`，填入自己的 DeepSeek、SiliconFlow、MySQL、JWT、Tavily 密钥。
3. Python 分析服务：
   ```bash
   copy Services\analytics-service\.env.example Services\analytics-service\.env
   ```
   编辑 `.env` 中的 `DB_PASSWORD` 等。

也可使用环境变量（见 `application.yaml.example` 中的 `${DEEPSEEK_API_KEY}` 等名称）。

## 重要：密钥曾出现在对话/旧文件中

若你曾在未脱敏的 `application.yaml` 中提交过仓库，或密钥出现在聊天记录里，建议在各平台 **作废并重新生成**：

- [DeepSeek](https://platform.deepseek.com/) API Key
- [SiliconFlow](https://siliconflow.cn/) API Key
- [Tavily](https://tavily.com/) API Key
- MySQL 密码、JWT Secret

GitHub 公开仓库一旦被爬取，旧密钥仍有泄露风险。
