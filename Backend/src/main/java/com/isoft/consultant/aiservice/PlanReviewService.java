package com.isoft.consultant.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * 志愿方案 AI 评审（基于已保存方案的 JSON 摘要）。
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel"
)
public interface PlanReviewService {

    @SystemMessage(fromResource = "system_plan_review.txt")
    Flux<String> review(@UserMessage String planSummary);
}
