package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.FavoriteSchoolVO;
import com.isoft.consultant.service.FavoriteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/schools")
    public Result<List<FavoriteSchoolVO>> list(Authentication auth) {
        return Result.success(favoriteService.listSchools(userId(auth)));
    }

    @GetMapping("/schools/{schoolCode}/status")
    public Result<Map<String, Boolean>> status(Authentication auth, @PathVariable String schoolCode) {
        return Result.success(Map.of("favorite", favoriteService.isFavorite(userId(auth), schoolCode)));
    }

    @PostMapping("/schools/{schoolCode}")
    public Result<Void> add(
            Authentication auth,
            @PathVariable String schoolCode,
            @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.get("note") : null;
        favoriteService.addSchool(userId(auth), schoolCode, note);
        return Result.success();
    }

    @DeleteMapping("/schools/{schoolCode}")
    public Result<Void> remove(Authentication auth, @PathVariable String schoolCode) {
        favoriteService.removeSchool(userId(auth), schoolCode);
        return Result.success();
    }

    private Long userId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("未登录");
        }
        return (Long) auth.getPrincipal();
    }
}
