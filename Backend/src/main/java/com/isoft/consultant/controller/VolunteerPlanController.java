package com.isoft.consultant.controller;

import com.isoft.consultant.aiservice.PlanReviewService;
import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.SaveVolunteerPlanRequest;
import com.isoft.consultant.dto.VolunteerPlanVO;
import com.isoft.consultant.dto.ExportReviewPdfRequest;
import com.isoft.consultant.service.PlanReviewPdfService;
import com.isoft.consultant.service.VolunteerPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class VolunteerPlanController {

    private static final Logger log = LoggerFactory.getLogger(VolunteerPlanController.class);

    private final VolunteerPlanService volunteerPlanService;
    private final PlanReviewService planReviewService;
    private final PlanReviewPdfService planReviewPdfService;

    public VolunteerPlanController(
            VolunteerPlanService volunteerPlanService,
            PlanReviewService planReviewService,
            PlanReviewPdfService planReviewPdfService) {
        this.volunteerPlanService = volunteerPlanService;
        this.planReviewService = planReviewService;
        this.planReviewPdfService = planReviewPdfService;
    }

    @GetMapping
    public Result<List<VolunteerPlanVO>> list(org.springframework.security.core.Authentication auth) {
        return Result.success(volunteerPlanService.list(userId(auth)));
    }

    @GetMapping("/{id}")
    public Result<VolunteerPlanVO> get(
            org.springframework.security.core.Authentication auth, @PathVariable Long id) {
        return Result.success(volunteerPlanService.get(userId(auth), id));
    }

    @PostMapping
    public Result<Map<String, Long>> save(
            org.springframework.security.core.Authentication auth,
            @RequestBody SaveVolunteerPlanRequest body) {
        Long id = volunteerPlanService.save(userId(auth), body);
        return Result.success(Map.of("id", id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            org.springframework.security.core.Authentication auth, @PathVariable Long id) {
        volunteerPlanService.delete(userId(auth), id);
        return Result.success();
    }

    @PostMapping(value = "/{id}/ai-review", produces = "text/html;charset=utf-8")
    public Flux<String> aiReview(
            org.springframework.security.core.Authentication auth, @PathVariable Long id) {
        log.info("POST /api/plans/{}/ai-review", id);
        String summary = volunteerPlanService.buildReviewSummary(userId(auth), id);
        return planReviewService.review(summary);
    }

    /**
     * 将前端已生成的 AI 评审正文导出为 PDF（Apache PDFBox）。
     */
    @PostMapping(value = "/export-review-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportReviewPdf(
            org.springframework.security.core.Authentication auth,
            @RequestBody ExportReviewPdfRequest body) {
        userId(auth);
        log.info("POST /api/plans/export-review-pdf");
        byte[] pdf;
        try {
            pdf = planReviewPdfService.generate(body.getTitle(), body.getMeta(), body.getContent());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (IOException e) {
            log.error("export review pdf failed", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "PDF 生成失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("export review pdf failed", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "PDF 生成失败: " + e.getMessage(), e);
        }
        String filename = "AI方案评审.pdf";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private Long userId(org.springframework.security.core.Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("未登录");
        }
        return (Long) auth.getPrincipal();
    }
}
