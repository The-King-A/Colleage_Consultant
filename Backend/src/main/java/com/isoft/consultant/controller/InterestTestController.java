package com.isoft.consultant.controller;

import com.isoft.consultant.aiservice.InterestTestInterpretService;
import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.HollandQuestionVO;
import com.isoft.consultant.dto.HollandResultVO;
import com.isoft.consultant.dto.HollandSubmitRequest;
import com.isoft.consultant.service.HollandAssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/interest-test")
public class InterestTestController {

    private static final Logger log = LoggerFactory.getLogger(InterestTestController.class);

    private final HollandAssessmentService hollandAssessmentService;
    private final InterestTestInterpretService interpretService;

    public InterestTestController(
            HollandAssessmentService hollandAssessmentService,
            InterestTestInterpretService interpretService) {
        this.hollandAssessmentService = hollandAssessmentService;
        this.interpretService = interpretService;
    }

    @GetMapping("/questions")
    public Result<List<HollandQuestionVO>> questions() {
        return Result.success(hollandAssessmentService.listQuestions());
    }

    @PostMapping("/submit")
    public Result<HollandResultVO> submit(@RequestBody HollandSubmitRequest request) {
        try {
            return Result.success(hollandAssessmentService.submit(request));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /** 基于已算好的霍兰德结果生成 AI 解读（单次调用，约 10～20 秒） */
    @PostMapping(value = "/interpret", produces = "text/html;charset=utf-8")
    public Flux<String> interpret(@RequestBody HollandResultVO result) {
        log.info("POST /api/interest-test/interpret — code={}", result.getHollandCode());
        String prompt = hollandAssessmentService.buildInterpretPrompt(result);
        return interpretService.interpret(prompt);
    }
}
