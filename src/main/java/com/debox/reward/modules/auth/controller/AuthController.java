package com.debox.reward.modules.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.dto.LoginRequest;
import com.debox.reward.modules.auth.dto.LoginResponse;
import com.debox.reward.modules.auth.security.JwtService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException("账号已冻结");
        }

        String stored = user.getPasswordHash();
        if (!matchesPassword(request.getPassword(), stored)) {
            throw new BizException("用户名或密码错误");
        }

        String token = jwtService.generate(Map.of(
                "userId", user.getId(),
                "role", user.getRole().name()
        ));

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setRole(user.getRole().name());
        return Result.ok(resp);
    }

    private boolean matchesPassword(String raw, String stored) {
        if (stored == null) {
            return false;
        }
        // 兼容旧数据：若不是 bcrypt，则按明文比对
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, stored);
        }
        return stored.equals(raw);
    }
}

