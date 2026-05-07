package com.debox.reward.modules.fund.service;

import com.debox.reward.common.redis.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 基金每日释放任务（0.5%/天）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FundReleaseTask {

    private static final long LOCK_EXPIRE_SECONDS = 300L;

    private final FundReleaseService fundReleaseService;
    private final RedisLockService redisLockService;

    @Scheduled(cron = "0 10 0 * * ?", zone = "Asia/Shanghai")
    public void releaseDaily() {
        String key = "lock:fund:release:daily";
        String val = redisLockService.tryLock(key, LOCK_EXPIRE_SECONDS);
        if (val == null) {
            return;
        }
        try {
            fundReleaseService.runDailyRelease();
        } finally {
            redisLockService.release(key, val);
        }
    }
}

