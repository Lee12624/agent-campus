
## 🔧 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- 阿里通义千问API密钥

### 2. 克隆项目

```bash
git clone <项目地址>
cd agent-ga-zjku
```

### 3. 数据库配置

创建MySQL数据库：
```sql
CREATE DATABASE tmp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 配置文件

**application.yml** - 主配置文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tmp?useSSL=false&serverTimezone=UTC
    username: your-username
    password: your-password
  
  ai:
    dashscope:
      api-key: your-dashscope-api-key
      model: qwen3-max

server:
  port: 8123
```

**application-local.yml** - 本地配置（已加入.gitignore）：
```yaml
# 本地敏感配置，不会提交到Git
spring:
  datasource:
    username: root
    password: 123456
  
  ai:
    dashscope:
      api-key: sk-your-actual-api-key

search-api:
  api-key: your-search-api-key
```

### 5. 启动项目

```bash
# 开发环境
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/agent-ga-zjku-0.0.1-SNAPSHOT.jar
```

### 6. 访问API文档

启动后访问：http://localhost:8123/api/swagger-ui.html

## 🔌 API接口

### AI对话接口

- **POST** `/api/chat` - 发送消息
- **GET** `/api/chat/history/{conversationId}` - 获取对话历史
- **GET** `/api/chat/conversations` - 获取会话列表

### 工具接口

- **POST** `/api/tools/file-operation` - 文件操作
- **POST** `/api/tools/generate-pdf` - 生成PDF
- **POST** `/api/tools/web-search` - 网页搜索
- **POST** `/api/tools/web-scraping` - 网页抓取

## 🧠 核心功能详解

### 1. AI大模型集成

使用Spring AI Alibaba集成通义千问：
```java
// 配置示例
DashScopeChatModel chatModel = DashScopeChatModel.builder()
    .apiKey("your-api-key")
    .model("qwen3-max")
    .temperature(0.7)
    .build();
```

### 2. RAG知识库

支持文档加载和向量检索：
- 支持Markdown、PDF、HTML文档
- 基于向量的语义检索
- 上下文查询增强

### 3. 会话记忆管理

基于MySQL的多轮对话记忆：
- 会话持久化存储
- 支持对话历史查询
- 可配置的会话超时

### 4. 工具调用系统

集成多种实用工具：
- **文件操作**: 读写、删除、重命名
- **PDF生成**: 支持模板渲染
- **网页搜索**: 集成搜索引擎
- **网页抓取**: 提取网页内容
- **资源下载**: 下载网络资源

## 🔒 安全配置

### 敏感信息保护

1. **本地配置文件**: `application-local.yml` 已加入`.gitignore`
2. **占位符配置**: 主配置文件使用占位符
3. **环境变量**: 支持通过环境变量覆盖配置

### 跨域配置

已配置CORS支持前后端分离：
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("*")
                    .allowedHeaders("*");
            }
        };
    }
}
```

## 📊 性能优化

### 1. 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
```

### 2. 缓存策略

- 支持Redis缓存（可扩展）
- 工具结果缓存
- 文档索引缓存

### 3. 异步处理

支持异步API调用和工具执行。

## 🧪 开发指南

### 添加新工具

1. 创建工具类实现`Tool`接口
2. 在`ToolRegistration`中注册
3. 添加API接口

### 扩展RAG功能

1. 自定义文档加载器
2. 配置向量存储
3. 实现查询增强器

### 自定义AI模型

支持切换不同的AI模型：
- 通义千问系列
- 其他兼容OpenAI接口的模型

## 🔍 监控与日志

### 日志配置

使用SLF4J + Logback：
```xml
<configuration>
    <logger name="com.lee.agentgazjku" level="INFO"/>
    <logger name="org.springframework.ai" level="DEBUG"/>
</configuration>
```

### 健康检查

Spring Boot Actuator端点：
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息

## 🐛 常见问题

### Q: API密钥如何配置？
A: 在`application-local.yml`中配置，该文件不会提交到Git。

### Q: 如何切换AI模型？
A: 修改`spring.ai.dashscope.model`配置。

### Q: 数据库连接失败？
A: 检查MySQL服务状态和连接配置。

### Q: 工具调用失败？
A: 查看日志确认工具权限和参数。

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交更改
4. 发起Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 📞 联系方式

- 项目维护者: Lee
- 邮箱: 3121953094@qq.com

---

**⭐ 如果这个项目对你有帮助，请给个Star！**
