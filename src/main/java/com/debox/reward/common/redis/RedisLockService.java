package com.debox.reward.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockService {

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取锁
     *
     * @param key           锁 key
     * @param expireSeconds 过期时间（秒）
     * @return 锁标识（获取成功），null（获取失败）
     */
    public String tryLock(String key, long expireSeconds) {
        String value = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功: key={}", key);
            return value;
        }
        log.debug("获取分布式锁失败: key={}", key);
        return null;
    }

    /**
     * 释放锁（原子操作，防止误删他人锁）
     *
     * @param key   锁 key
     * @param value 锁标识
     */
    public void release(String key, String value) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        stringRedisTemplate.execute(script, List.of(key), value);
        log.debug("释放分布式锁: key={}", key);
    }
}

