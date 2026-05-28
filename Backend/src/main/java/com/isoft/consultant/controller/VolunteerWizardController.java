package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.CompleteWizardStepRequest;
import com.isoft.consultant.dto.WizardProgressVO;
import com.isoft.consultant.service.VolunteerWizardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wizard")
public class VolunteerWizardController {

    private final VolunteerWizardService volunteerWizardService;

    public VolunteerWizardController(VolunteerWizardService volunteerWizardService) {
        this.volunteerWizardService = volunteerWizardService;
    }

    @GetMapping("/progress")
    public Result<WizardProgressVO> progress(Authentication auth) {
        return Result.success(volunteerWizardService.getProgress(userId(auth)));
    }

    @PostMapping("/complete-step")
    public Result<WizardProgressVO> completeStep(
            Authentication auth, @RequestBody CompleteWizardStepRequest body) {
        try {
            return Result.success(volunteerWizardService.completeStep(userId(auth), body));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @PostMapping("/save-step")
    public Result<WizardProgressVO> saveStep(
            Authentication auth, @RequestBody CompleteWizardStepRequest body) {
        try {
            return Result.success(volunteerWizardService.saveStep(userId(auth), body));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    private Long userId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("未登录");
        }
        return (Long) auth.getPrincipal();
    }
}
