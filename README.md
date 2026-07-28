# NewDream

个人AI伴侣/角色扮演/小说阅读平台 · 个人学习版

> ⚠️ 本应用为个人学习项目，仅限个人使用。所有代码均为原创，未分发任何第三方应用的代码或素材。

## 功能

- **📖 小说阅读** - 多分支AI续写阅读器（支持TXT/EPUB）
- **💬 角色聊天** - SillyTavern兼容的角色卡聊天系统
- **💕 AI伴侣** - 拥有主动消息、互动道具的AI伴侣系统
- **🎭 VN剧场** - 视觉小说演出模式（VN Theater）
- **🤖 智能体** - 内置Agent技能系统（角色卡创作、AI续写、配图提示词等）
- **🔧 多服务商API** - 支持OpenAI兼容、Gemini、Anthropic等多种AI服务商

## 快速开始

### 从GitHub Actions下载APK

1. 进入仓库的 **Actions** 页面
2. 选择最新的 **Build APK** workflow
3. 下载 **NewDream-Debug** artifact
4. 安装到Android设备（最低API 26 / Android 8.0）

### 自行编译

```bash
# Linux/macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

APK 生成在 `app/build/outputs/apk/debug/`

## 首次使用

1. 打开应用，进入 **设置 → 模型供应商**
2. 添加你的API服务商（OpenAI兼容格式）
3. 填入 API Host（如 `https://api.openai.com/v1`）和 API Key
4. 点击「测试连接」验证
5. 添加需要的模型
6. 开始使用各项功能

### 推荐免费/低价API选项

| 服务商 | 类型 | Host示例 | 备注 |
|--------|------|----------|------|
| OpenAI | 官方 | https://api.openai.com/v1 | 付费 |
| Google Gemini | 官方 | https://generativelanguage.googleapis.com/v1beta | 免费额度 |
| DeepSeek | 兼容 | https://api.deepseek.com/v1 | 低价 |
| 本地 | Ollama | http://localhost:11434/v1 | 免费，需本地运行 |
| 中转站 | 兼容 | 各种第三方代理 | 按量计费 |

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **导航**: Navigation Compose
- **数据**: DataStore Preferences
- **网络**: OkHttp
- **序列化**: Kotlinx Serialization
- **最低SDK**: Android 8.0 (API 26)
- **目标SDK**: Android 15 (API 35)

## 项目结构

```
app/src/main/java/app/newdream/
├── NewDreamApp.kt          # Application类
├── MainActivity.kt         # 主Activity + 导航
├── data/
│   ├── api/ApiService.kt   # API服务（OpenAI兼容）
│   ├── local/AppSettings.kt # 本地存储（DataStore）
│   └── model/Models.kt     # 数据模型
├── ui/
│   ├── theme/              # 主题/颜色
│   ├── components/         # 通用组件
│   └── screens/
│       ├── home/           # 首页
│       ├── reader/         # 小说阅读
│       ├── chat/           # 角色聊天
│       ├── companion/      # AI伴侣
│       ├── vn/             # VN剧场
│       ├── agent/          # 智能体
│       └── settings/       # 设置/API配置
└── agent/
    └── skills/             # Agent技能（可扩展）
```

## 免责声明

本应用为个人学习项目，仅供学习和技术研究使用。
- 所有AI功能需自行配置API Key
- 内容生成由第三方AI服务商完成
- 不收集任何用户数据
- 不包含任何形式的广告或付费墙
