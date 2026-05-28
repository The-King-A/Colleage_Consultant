package com.isoft.consultant.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 联网搜索专用AI服务接口
 *
 * 与ConsultantService的区别：不使用contentRetriever（RAG），
 * 因为联网搜索时用户需要实时网络信息而非静态PDF内容。
 * 去掉RAG可避免用户消息+搜索结果过长导致Embedding API 413错误。
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider"
)
public interface ConsultantSearchService {

    @SystemMessage(fromResource = "system_search.txt")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
