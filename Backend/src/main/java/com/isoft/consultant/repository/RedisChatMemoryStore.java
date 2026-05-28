package com.isoft.consultant.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

/**
 * Redis会话存储实现类
 * 
 * 实现LangChain4j的ChatMemoryStore接口，使用Redis作为会话历史的持久化存储
 * 替代默认的内存存储方案，提供更好的数据持久性和可扩展性
 * 
 * 核心功能：
 * 1. 会话数据的持久化存储
 * 2. 24小时会话保持
 * 3. 多用户会话隔离
 * 4. 会话数据的序列化与反序列化
 * 
 * 存储设计：
 * - Key: memoryId (String) - 用户会话唯一标识
 * - Value: JSON格式的对话记录列表
 * - TTL: 24小时 - 自动过期管理
 */
@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {
    /** Redis操作模板，用于执行Redis命令 */
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 获取指定会话ID的消息列表
     * 
     * 从Redis中获取用户会话历史消息，支持多用户会话隔离
     * 
     * @param memoryId 会话ID，用于标识不同用户的对话历史
     * @return List<ChatMessage> 会话消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 从Redis中获取会话消息的JSON字符串表示
        String json = redisTemplate.opsForValue().get(memoryId);
        
        // 将JSON字符串反序列化为ChatMessage列表
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }

    /**
     * 更新指定会话ID的消息列表
     * 
     * 将用户最新的会话历史序列化后存储到Redis中
     * 设置24小时TTL，实现会话自动过期管理
     * 
     * @param memoryId 会话ID，用于标识不同用户的对话历史
     * @param list 会话消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 更新会话消息
        
        // 1.将ChatMessage列表序列化为JSON字符串
        String json = ChatMessageSerializer.messagesToJson(list);
        
        // 2.将JSON数据存储到Redis中，设置24小时过期时间
        redisTemplate.opsForValue().set(memoryId.toString(), json, Duration.ofDays(1));
    }

    /**
     * 删除指定会话ID的消息列表
     * 
     * 从Redis中删除用户的会话历史数据
     * 
     * @param memoryId 会话ID，用于标识不同用户的对话历史
     */
    @Override
    public void deleteMessages(Object memoryId) {
        // 从Redis中删除会话消息
        redisTemplate.delete(memoryId.toString());
    }

}