package com.isoft.consultant.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 职业兴趣测试AI服务接口
 * 
 * 基于LangChain4j框架的@AiService注解定义的职业兴趣测试服务接口
 * 用于通过智能问答方式评估学生的兴趣、能力、性格和价值观，最终推荐最适合的大学专业
 * 
 * 核心特性：
 * 1. 使用显式装配模式(AiServiceWiringMode.EXPLICIT)，明确指定所使用的模型、记忆组件和检索器
 * 2. 集成流式响应机制，通过Flux<String>实现实时输出
 * 3. 支持多用户会话隔离，通过@MemoryId实现
 * 4. 加载系统角色定义，通过@SystemMessage定义AI助手的行为规范
 */
@AiService(
        // 显式装配模式，需要手动指定各个组件
        wiringMode = AiServiceWiringMode.EXPLICIT,
        // 指定对话模型bean名称，在CommonConfig中定义
        chatModel = "openAiChatModel",
        // 指定流式对话模型bean名称，支持SSE流式响应
        streamingChatModel = "openAiStreamingChatModel",
        // 指定会话记忆提供者bean名称，用于管理用户对话历史
        chatMemoryProvider = "chatMemoryProvider",
        // 指定内容检索器bean名称，用于RAG检索增强生成
        contentRetriever = "contentRetriever"
)
public interface InterestTestService {
    /**
     * 职业兴趣测试服务接口
     * 
     * 通过流式响应方式与用户进行交互式的兴趣测试问答
     * 按照预设的10个问题流程，逐步了解学生的学科兴趣、职业兴趣、性格特点和价值观
     * 
     * @param memoryId 会话ID，用于区分不同用户的对话历史，实现会话隔离
     * @param message 用户对兴趣测试问题的回答
     * @return Flux<String> 流式响应结果，支持SSE实时输出
     * 
     * 系统消息通过system_interest_test.txt文件定义，明确了AI助手的角色和行为规范：
     * - 作为专业的AI职业兴趣测试顾问
     * - 通过约10个问题循序渐进地了解学生的兴趣、能力、性格和价值观
     * - 生成详细的专业推荐报告（至少2000字）
     * - 遵循特定的问答规则和测试流程
     */
    @SystemMessage(fromResource = "system_interest_test.txt")
    public Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}