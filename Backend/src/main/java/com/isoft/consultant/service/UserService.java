package com.isoft.consultant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.isoft.consultant.dto.*;
import com.isoft.consultant.entity.User;

public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    UserInfoResponse getCurrentUser(Long userId);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    UserInfoResponse updateProfile(Long userId, UserInfoResponse request);
}
