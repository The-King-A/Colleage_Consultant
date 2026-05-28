# PDF 中文字体（可选）

AI 方案评审导出 PDF 时，服务会按以下顺序查找字体：

1. 本目录下的 `NotoSansSC-Regular.ttf`（推荐，可跨平台）
2. Windows：`C:/Windows/Fonts/msyh.ttc`（微软雅黑）等
3. Linux / macOS 常见系统字体路径

若导出报错「未找到可用的中文字体」，请下载 [Noto Sans SC](https://fonts.google.com/noto/specimen/Noto+Sans+SC) Regular 字体文件，重命名为 `NotoSansSC-Regular.ttf` 放在此目录后重启后端。
