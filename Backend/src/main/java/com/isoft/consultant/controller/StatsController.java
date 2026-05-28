package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.DashboardStatsVO;
import com.isoft.consultant.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> dashboard() {
        return Result.success(statsService.getDashboardStats());
    }
}
