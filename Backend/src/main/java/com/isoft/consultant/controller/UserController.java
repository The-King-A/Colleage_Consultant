package com.isoft.consultant.controller;

import com.isoft.consultant.common.Result;
import com.isoft.consultant.dto.UserInfoResponse;
import com.isoft.consultant.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        return Result.success(userService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<UserInfoResponse> updateProfile(
            HttpServletRequest request,
            @RequestBody UserInfoResponse updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.updateProfile(userId, updateRequest));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updatePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }
}
