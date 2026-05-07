package com.debox.reward.modules.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.dto.LoginRequest;
import com.debox.reward.modules.auth.dto.LoginResponse;
import com.debox.reward.modules.auth.dto.NonceResponse;
import com.debox.reward.modules.auth.dto.WalletLoginRequest;
import com.debox.reward.modules.auth.eth.EthereumPersonalSignVerifier;
import com.debox.reward.modules.auth.eth.WalletLoginMessage;
import com.debox.reward.modules.auth.security.JwtService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.mapper.UserMapper;
import com.debox.reward.modules.user.service.UserService;
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
    private final UserService userService;
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

    @PostMapping("/login/wallet")
    public Result<LoginResponse> loginWallet(@Valid @RequestBody WalletLoginRequest request) {
        String normalized = EthereumPersonalSignVerifier.normalizeAddress(request.getWalletAddress());
        if (!EthereumPersonalSignVerifier.looksLikeEvmAddress(normalized)) {
            throw new BizException("钱包地址格式无效");
        }

        String key = "auth:nonce:" + request.getNonce().trim();
        String bound = stringRedisTemplate.opsForValue().get(key);
        if (bound == null) {
            throw new BizException("nonce 无效或已过期");
        }
        if (!bound.isBlank()) {
            String boundNorm = EthereumPersonalSignVerifier.normalizeAddress(bound);
            if (!normalized.equalsIgnoreCase(boundNorm)) {
                throw new BizException("钱包地址与 nonce 绑定不一致");
            }
        }

        String messageUtf8 = WalletLoginMessage.build(request.getNonce().trim(), normalized);
        try {
            if (!EthereumPersonalSignVerifier.isValidSigner(messageUtf8, request.getSignature(), normalized)) {
                throw new BizException("签名验证失败");
            }
        } catch (IllegalArgumentException e) {
            throw new BizException("签名格式错误或无法恢复公钥");
        }

        stringRedisTemplate.delete(key);

        User user = userService.findOrCreateForWalletLogin(normalized);

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

