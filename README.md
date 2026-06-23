# 🍡 绿豆糕 — 校园 AI 智能助手

> 张家口学院明湖校区吉祥物「丸丸」— 基于通义千问大模型的智能校园问答系统

一个亲切可爱的 AI 校园学姐，帮助学生和老师解答校园问题。支持知识库问答（RAG）、工具调用（天气/搜索/PDF 生成）、多轮对话记忆和用户认证。

---

## ✨ 功能特性

- **🤖 AI 智能对话** — 基于阿里云 DashScope（通义千问 qwen3-max），支持流式 SSE 输出
- **📚 RAG 知识库** — 加载校园文档（学生手册、校园知识等），基于向量检索增强问答
- **🔧 工具调用** — 联网搜索、网页抓取、天气查询（高德 API）、PDF 生成、文件操作
- **💬 多轮对话** — MySQL 持久化会话记忆，支持对话历史查看与清除
- **🔐 JWT 认证** — 用户注册/登录，会话隔离
- **🎨 现代前端** — Vue 3 + TypeScript + Element Plus，响应式聊天界面

---

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.7 + Java 21 |
| AI 引擎 | Spring AI Alibaba + DashScope（通义千问） |
| 数据库 | MySQL 8.0+ |
| 向量存储 | DashScope 云 Embedding + 向量检索 |
| 前端 | Vue 3 + TypeScript + Vite + Element Plus |
| 文档 | SpringDoc OpenAPI + Knife4j |
| 容器化 | Docker |

---

## 🚀 快速开始

### 1. 环境要求

- **JDK 21+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **阿里云 DashScope API Key**（[申请地址](https://dashscope.console.aliyun.com/)）
- **Node.js 18+**（仅前端开发）

### 2. 克隆项目

```bash
git clone https://github.com/Lee12624/agent-ga-zjku.git
cd agent-ga-zjku
```

### 3. 数据库配置

创建 MySQL 数据库：

```sql
CREATE DATABASE tmp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 配置文件

复制配置模板并填入你的真实值：

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

编辑 `application-local.yml`：

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-your-actual-api-key     # 通义千问 API Key
  datasource:
    username: your-db-username
    password: your-db-password

search-api:
  api-key: your-search-api-key            # 搜索 API Key（可选）
```

> ⚠️ `application-local.yml` 已在 `.gitignore` 中，不会被提交到 Git。

### 5. 启动后端

```bash
# 开发环境（使用 application-local.yml）
mvn spring-boot:run

# 或打包运行
mvn clean package -DskipTests
java -jar target/agent-ga-zjku-0.0.1-SNAPSHOT.jar
```

服务启动后访问：
- **API 文档（Swagger）**：http://localhost:8123/api/swagger-ui.html
- **API 文档（Knife4j）**：http://localhost:8123/api/doc.html
- **健康检查**：http://localhost:8123/api/actuator/health

### 6. 启动前端（可选）

```bash
cd agent-ga-frontend-zjku
npm install
npm run dev
```

---

## 🔌 API 接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET  | `/api/auth/me` | 获取当前用户信息 |

### AI 对话接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/ai/campus_app/chat/rag/stream` | RAG 知识库流式对话（SSE） |
| GET  | `/api/ai/campus_app/chat/tool/stream` | 工具调用流式对话（SSE） |
| GET  | `/api/ai/chat/history/database` | 获取对话历史 |
| GET  | `/api/ai/chat/history/database/list` | 获取会话列表 |
| POST | `/api/ai/chat/history/database/add` | 添加消息到对话历史 |
| DELETE | `/api/ai/chat/history/database` | 清除指定对话历史 |
| DELETE | `/api/ai/chat/history/database/clearAll` | 清除所有对话历史 |
| GET  | `/api/ai/download/pdf` | 下载生成的 PDF 文件 |

---

## 🧠 核心架构

```
┌─────────────┐     ┌──────────────────────────────────┐
│   Vue 3     │────▶│  Spring Boot (AiController)      │
│   Frontend  │     │  /api/ai/*                       │
└─────────────┘     └──────────┬───────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
   ┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
   │  CampusApp   │   │  RAG Advisor │   │  Tool Callbacks  │
   │  对话管理     │   │  知识检索     │   │  工具调用          │
   └──────┬───────┘   └──────┬───────┘   └────────┬─────────┘
          │                  │                     │
          ▼                  ▼                     ▼
   ┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
   │  ChatMemory  │   │  DashScope   │   │  Weather/Search  │
   │  (MySQL)     │   │  Embedding   │   │  PDF/File/Scrape │
   └──────────────┘   └──────────────┘   └──────────────────┘
```

---

## 📁 项目结构

```
agent-ga-zjku/
├── src/main/java/com/lee/agentgazjku/
│   ├── app/CampusApp.java          # 核心对话应用
│   ├── auth/                       # JWT 认证（Filter/Service/Hasher）
│   ├── chatmemory/                 # MySQL 会话记忆实现
│   ├── config/                     # CORS、ChatMemory 配置
│   ├── controller/                 # REST 控制器（Ai/Auth）
│   ├── rag/                        # RAG 知识库（文档加载/向量存储/查询增强）
│   ├── repository/                 # 用户数据仓库
│   ├── service/                    # 认证服务
│   └── tools/                      # AI 工具（天气/搜索/抓取/PDF/文件）
├── src/main/resources/
│   ├── application.yml             # 主配置（含占位符，可提交）
│   ├── application-local.yml.example  # 本地配置模板
│   └── document/                   # RAG 知识文档
├── agent-ga-frontend-zjku/         # Vue 3 前端项目
├── Dockerfile                      # Docker 构建文件
└── pom.xml                         # Maven 配置
```

---

## 🐳 Docker 部署

```bash
# 构建镜像
docker build -t agent-ga-zjku .

# 运行容器
docker run -d \
  -p 8123:8123 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/tmp \
  -e SPRING_DATASOURCE_USERNAME=your-user \
  -e SPRING_DATASOURCE_PASSWORD=your-password \
  -e SPRING_AI_DASHSCOPE_API_KEY=your-api-key \
  agent-ga-zjku
```

---

## 🔒 安全说明

- **API Key 保护**：所有密钥配置在 `application-local.yml`（已加入 `.gitignore`），不会提交到版本控制
- **配置模板**：使用 `application-local.yml.example` 作为模板，包含所有需要配置的占位符
- **JWT 密钥**：生产环境务必修改 `app.auth.jwt-secret` 为强随机字符串
- **数据库密码**：生产环境使用环境变量或配置中心管理

---

## 🧪 开发工具

- **AmapWeatherTool** — 高德天气 API 集成
- **WebSearchTool** — 搜索引擎集成
- **WebScrapingTool** — 网页内容抓取
- **PDFGenerationTool** — 中文 PDF 生成（自动检测系统中文字体）
- **FileOperationTool** — 文件读写操作
- **ResourceDownloadTool** — 网络资源下载
- **TerminateTool** — 对话终止控制

---

## 🤝 贡献

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 发起 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证。

---

**⭐ 如果这个项目对你有帮助，请给个 Star！**
