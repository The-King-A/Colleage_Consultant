package com.isoft.consultant.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 霍兰德测评结果的一次性 AI 解读（无 RAG、无多轮记忆，响应更快）。
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel"
)
public interface InterestTestInterpretService {

    @SystemMessage(fromResource = "system_interest_interpret.txt")
    Flux<String> interpret(@UserMessage String assessmentSummary);
}
