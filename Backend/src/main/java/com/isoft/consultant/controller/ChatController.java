package com.isoft.consultant.controller;

import com.isoft.consultant.aiservice.ConsultantSearchService;
import com.isoft.consultant.aiservice.ConsultantService;
import com.isoft.consultant.aiservice.InterestTestService;
import com.isoft.consultant.dto.ChatRequest;
import com.isoft.consultant.service.WebSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private ConsultantSearchService consultantSearchService;

    @Autowired
    private InterestTestService interestTestService;

    @Autowired
    private WebSearchService webSearchService;

    /** GET — query params (backward compatible) */
    @GetMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chatGet(
            @RequestParam String memoryId,
            @RequestParam String message,
            @RequestParam(defaultValue = "false") boolean search) {
        log.info("GET /chat — memoryId={}, search={}, msgLen={}", memoryId, search, message.length());
        return doChat(memoryId, message, search);
    }

    /** POST — JSON body */
    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chatPost(@RequestBody ChatRequest request) {
        log.info("POST /chat — memoryId={}, search={}, msgLen={}",
                request.getMemoryId(), request.isSearch(),
                request.getMessage() != null ? request.getMessage().length() : 0);
        return doChat(request.getMemoryId(), request.getMessage(), request.isSearch());
    }

    /** GET — interest test */
    @GetMapping(value = "/interest-test", produces = "text/html;charset=utf-8")
    public Flux<String> interestTestGet(
            @RequestParam String memoryId,
            @RequestParam String message) {
        log.info("GET /interest-test — memoryId={}", memoryId);
        return interestTestService.chat(memoryId, message);
    }

    /** POST — interest test JSON body */
    @PostMapping(value = "/interest-test", produces = "text/html;charset=utf-8")
    public Flux<String> interestTestPost(@RequestBody ChatRequest request) {
        log.info("POST /interest-test — memoryId={}", request.getMemoryId());
        return interestTestService.chat(request.getMemoryId(), request.getMessage());
    }

    private Flux<String> doChat(String memoryId, String message, boolean search) {
        try {
            if (search) {
                String searchResults = webSearchService.search(message);
                return consultantSearchService.chat(memoryId, message + searchResults);
            }
            return consultantService.chat(memoryId, message);
        } catch (Exception e) {
            log.error("Chat error", e);
            return Flux.error(e);
        }
    }
}
