package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.ScoreRankLookupVO;
import com.isoft.consultant.service.ScoreRankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rank")
public class ScoreRankController {

    private final ScoreRankService scoreRankService;

    public ScoreRankController(ScoreRankService scoreRankService) {
        this.scoreRankService = scoreRankService;
    }

    @GetMapping("/lookup")
    public Result<ScoreRankLookupVO> lookup(
            @RequestParam String province,
            @RequestParam(defaultValue = "science") String subjectType,
            @RequestParam int score,
            @RequestParam(required = false) Integer year) {
        try {
            return Result.success(scoreRankService.lookup(province, subjectType, score, year));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
