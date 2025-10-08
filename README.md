# 智能高考志愿填报咨询系统 - 项目文档

## 1. 项目概述

智能高考志愿填报咨询系统是一个基于Spring Boot和LangChain4j框架开发的AI驱动咨询服务系统。该系统利用大语言模型技术，为高考考生提供专业的志愿填报指导服务，包括院校信息查询、专业推荐、录取分析等功能。

### 1.1 核心功能
- 智能聊天咨询：基于AI的交互式咨询服务
- 兴趣测试：通过问答形式评估学生兴趣并推荐专业
- 院校信息查询：提供院校简介、录取规则等信息
- 专业推荐：根据学生分数和偏好推荐合适的专业
- 会话记忆：基于Redis的持久化会话存储

### 1.2 技术架构
- **后端框架**: Spring Boot 3.5.4
- **Java版本**: Java 17
- **AI框架**: LangChain4j 1.0.1-beta6
- **大模型**: 阿里云通义千问 (qwen-plus)
- **向量数据库**: Redis (支持向量存储和会话记忆)
- **文档解析**: Apache PDFBox (支持PDF文档解析)
- **响应式编程**: Spring WebFlux + Reactor

## 2. 项目结构

```
com.isoft.consultant/
├── ConsultantApplication.java          # 项目启动类
├── aiservice/
│   ├── ConsultantService.java         # 志愿填报AI服务接口
│   └── InterestTestService.java       # 兴趣测试AI服务接口
├── config/
│   └── CommonConfig.java             # 核心配置类
├── controller/
│   └── ChatController.java           # REST API控制器
└── repository/
    └── RedisChatMemoryStore.java     # Redis会话存储实现
```

## 3. 核心组件详解

### 3.1 AI服务层

#### ConsultantService.java
志愿填报咨询服务接口，定义了与志愿填报相关的AI服务方法：
- 使用`@AiService`注解配置AI服务
- 配置了手动装配模式，指定了聊天模型、流式聊天模型、会话记忆提供者和内容检索器
- 提供[chat](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/aiservice/ConsultantService.java#L23-L23)方法用于与用户进行志愿填报相关的对话
- 使用[system.txt](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/resources/system.txt)作为系统消息定义AI行为准则

#### InterestTestService.java
兴趣测试服务接口，定义了专业兴趣测试的AI服务方法：
- 类似于ConsultantService，使用相同的AI服务配置
- 提供[chat](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/aiservice/InterestTestService.java#L14-L14)方法用于进行兴趣测试对话
- 使用[system_interest_test.txt](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/resources/system_interest_test.txt)作为系统消息定义兴趣测试流程

### 3.2 配置层

#### CommonConfig.java
系统核心配置类，负责配置各种AI服务所需的组件：
- 配置会话记忆对象([chatMemory](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/config/CommonConfig.java#L46-L50))
- 配置会话记忆提供者([chatMemoryProvider](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/config/CommonConfig.java#L52-L62))
- 配置向量数据库操作对象([store](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/config/CommonConfig.java#L64-L85))
- 配置向量数据库检索对象([contentRetriever](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/config/CommonConfig.java#L89-L95))

### 3.3 控制层

#### ChatController.java
REST API控制器，暴露对外的服务接口：
- 提供[/chat](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/controller/ChatController.java#L22-L25)接口用于志愿填报咨询
- 提供[/interest-test](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/controller/ChatController.java#L27-L30)接口用于兴趣测试
- 使用Flux实现流式响应，提升用户体验

### 3.4 数据存储层

#### RedisChatMemoryStore.java
Redis会话存储实现类，负责将会话数据持久化到Redis：
- 实现ChatMemoryStore接口
- 使用StringRedisTemplate操作Redis
- 提供[getMessages](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/repository/RedisChatMemoryStore.java#L22-L28)、[updateMessages](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/repository/RedisChatMemoryStore.java#L31-L38)、[deleteMessages](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/repository/RedisChatMemoryStore.java#L40-L42)方法管理会话数据
- 设置会话数据过期时间为1天

## 4. 系统配置

### 4.1 application.yaml
系统主要配置文件，配置了LangChain4j和Redis相关参数：

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${API-KEY}
      model-name: qwen-plus
      log-requests: true
      log-responses: true
    streaming-chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${API-KEY}
      model-name: qwen-plus
      log-requests: true  
      log-responses: true
    embedding-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${API-KEY}
      model-name: text-embedding-v3
      log-requests: true
      log-responses: true
      max-segments-per-batch: 10
  community:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    dev.langchain4j: debug

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### 4.2 系统提示文件

#### system.txt
定义志愿填报咨询AI的行为准则和服务范围，包括：
- 可提供的11项具体服务功能
- 预约服务的具体流程和要求
- 回答问题的规范和限制

#### system_interest_test.txt
定义兴趣测试AI的完整测试流程：
- 包含学科兴趣、职业兴趣、性格特点、价值观与动机四个维度的测试
- 规定了测试流程和问答规则
- 定义了详细的测试报告生成格式和内容要求

## 5. API接口说明

### 5.1 志愿填报咨询接口
```
GET /chat
```

**请求参数:**
- `memoryId` (String): 会话ID，用于保持对话上下文
- `message` (String): 用户输入的消息

**响应格式:**
- text/event-stream (Server-Sent Events)，实现流式响应

**使用示例:**
```bash
curl "http://localhost:8080/chat?memoryId=user123&message=我想了解计算机专业的就业情况"
```

### 5.2 兴趣测试接口
```
GET /interest-test
```

**请求参数:**
- `memoryId` (String): 会话ID，用于保持对话上下文
- `message` (String): 用户输入的消息

**响应格式:**
- text/event-stream (Server-Sent Events)，实现流式响应

**使用示例:**
```bash
curl "http://localhost:8080/interest-test?memoryId=user123&message=开始兴趣测试"
```

## 6. 部署指南

### 6.1 环境要求
- Java 17或更高版本
- Maven 3.6+
- Redis 6.0+ (需要启用Redis模块支持)
- 阿里云DashScope API密钥

### 6.2 本地开发
1. **配置环境变量**
   ```bash
   export API-KEY=你的阿里云DashScope API密钥
   ```

2. **启动Redis**
   ```bash
   redis-server
   ```

3. **运行项目**
   ```bash
   mvn spring-boot:run
   ```

### 6.3 生产部署
1. **构建项目**
   ```bash
   mvn clean package
   ```

2. **运行jar包**
   ```bash
   java -jar target/consultant-0.0.1-SNAPSHOT.jar
   ```

## 7. 数据准备

### 7.1 院校资料
将院校招生简章、专业介绍等PDF文档放入 `src/main/resources/content/` 目录，系统启动时会自动解析并建立向量索引。

### 7.2 数据格式
支持PDF格式的院校资料，包括但不限于：
- 招生简章
- 专业介绍
- 录取分数统计
- 学校介绍材料

## 8. 扩展开发

### 8.1 添加新功能
1. 在AI服务接口中添加新方法
2. 更新对应的系统提示词文件
3. 添加相应的控制器方法

### 8.2 支持更多文档类型
1. 添加新的文档解析器依赖
2. 扩展[CommonConfig](file:///C:/Users/TheKingA/Desktop/Consultant/Backend/src/main/java/com/isoft/consultant/config/CommonConfig.java#L33-L102)中的文档加载逻辑
3. 更新向量存储配置

## 9. 故障排查

### 9.1 常见问题
1. **API密钥无效**: 检查环境变量 `API-KEY` 是否正确设置
2. **Redis连接失败**: 检查Redis服务状态和配置
3. **向量检索无结果**: 确认PDF文档已正确放入content目录
4. **内存不足**: 调整JVM参数或增加系统内存

### 9.2 调试技巧
- 开启debug日志查看详细调用过程
- 使用Redis CLI检查向量数据是否正确存储
- 验证PDF文档是否能被正确解析
