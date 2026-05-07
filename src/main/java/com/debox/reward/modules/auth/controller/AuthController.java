package com.debox.reward.modules.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.dto.LoginRequest;
import com.debox.reward.modules.auth.dto.LoginResponse;
import com.debox.reward.modules.auth.dto.NonceResponse;
import com.debox.reward.modules.auth.security.JwtService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final long NONCE_EXPIRE_SECONDS = 300;

    /**
     * 钱包签名前置 nonce（占位：后续校验签名时再消费 nonce）
     */
    @PostMapping("/nonce")
    public Result<NonceResponse> nonce(@RequestParam(required = false) String walletAddress) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String key = "auth:nonce:" + nonce;
        stringRedisTemplate.opsForValue().set(key, walletAddress == null ? "" : walletAddress,
                Duration.ofSeconds(NONCE_EXPIRE_SECONDS));
        NonceResponse r = new NonceResponse();
        r.setNonce(nonce);
        r.setExpireSeconds(NONCE_EXPIRE_SECONDS);
        return Result.ok(r);
    }

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

