# 张家口学院校园智能问答助手前端

基于 Vue 3 + TypeScript + Element Plus 的校园智能问答助手前端应用。

## 功能特性

- 🎨 现代化UI设计，仿"豆包"聊天室布局
- 🤖 支持普通模式和高级模式聊天
- 📱 完全响应式，支持PC、平板、移动端
- 💬 SSE流式输出，打字机效果
- 📚 历史对话管理
- 🎭 丰富的动画效果和卡通元素

## 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **UI库**: Element Plus
- **路由**: Vue Router 4
- **HTTP客户端**: Axios (内置SSE支持)
- **样式**: 原生CSS + 响应式设计

## 项目结构

```
src/
├── api/           # API服务
│   ├── chat.ts    # 聊天API服务
│   └── types.ts   # 类型定义
├── composables/   # 组合式函数
│   └── useChat.ts # 聊天逻辑
├── router/        # 路由配置
│   └── index.ts
├── views/         # 页面组件
│   ├── Home.vue   # 主页
│   └── Chat.vue   # 聊天页面
├── App.vue        # 根组件
└── main.ts        # 入口文件
```

## 安装和运行

### 安装依赖

```bash
npm install
```

### 开发环境

```bash
npm run dev
```

访问 `http://localhost:3000`

### 构建生产版本

```bash
npm run build
```

## API接口

项目通过SSE流式接口与后端通信：

- **普通模式**: `GET /api/ai/campus_app/chat/rag/stream`
- **高级模式**: `GET /api/ai/campus_app/chat/tool/stream`

参数：
- `userMessage`: 用户消息
- `chatId`: 对话ID

## 页面说明

### 主页 (/)
- 展示应用介绍和功能特色
- 包含丰富的动画效果和卡通元素
- 一键进入聊天页面

### 聊天页面 (/chat)
- 左侧边栏：历史对话管理
- 主区域：聊天界面
- 支持普通/高级模式切换
- 实时流式对话
- 响应式布局适配

## 浏览器支持

- Chrome 70+
- Firefox 70+
- Safari 12+
- Edge 79+

## 开发说明

### 代码规范

- 使用 TypeScript 严格模式
- 遵循 Vue 3 Composition API 最佳实践
- 使用 ESLint 代码检查

### 响应式设计

项目采用移动优先的响应式设计策略：

- **移动端** (< 768px): 单列布局，侧边栏折叠
- **平板端** (768px - 1024px): 适应性布局
- **PC端** (> 1024px): 完整双栏布局

## 注意事项

1. 确保后端服务运行在 `http://localhost:8123`
2. 项目使用原生SSE，需要现代浏览器支持
3. 聊天历史存储在localStorage中

