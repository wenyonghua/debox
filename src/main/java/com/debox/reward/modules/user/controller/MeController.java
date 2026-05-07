package com.debox.reward.modules.user.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.user.dto.ReferrerBindRequest;
import com.debox.reward.modules.user.dto.UserProfileResponse;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping
    public Result<UserProfileResponse> me() {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        User u = userService.getById(uid);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        UserProfileResponse r = new UserProfileResponse();
        BeanUtils.copyProperties(u, r);
        return Result.ok(r);
    }

    @PostMapping("/referrer")
    public Result<Void> bindReferrer(@Valid @RequestBody ReferrerBindRequest request) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        userService.bindReferrer(uid, request.getInviteCode());
        return Result.ok(null);
    }
}
