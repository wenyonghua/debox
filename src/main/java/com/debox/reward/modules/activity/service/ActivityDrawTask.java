package com.debox.reward.modules.activity.service;

import com.debox.reward.common.redis.RedisLockService;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日开奖任务（按文档 21:34）。
 * <p>
 * 当前实现：对“已到 endTime 且尚未开奖”的 OPEN 期号写入 resultPayload（包含 seed、winRateBp）。
 * 后续可替换为链上随机数/第三方源。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityDrawTask {

    private static final long LOCK_EXPIRE_SECONDS = 120L;
    private static final SecureRandom RND = new SecureRandom();

    /**
     * 中奖率占位（bp = 1/10000）。后续应由规则快照/后台配置驱动。
     */
    private static final int DEFAULT_WIN_RATE_BP = 1000; // 10%

    private final ActivityIssueService activityIssueService;
    private final RedisLockService redisLockService;

    /**
     * 21:34 执行（Asia/Shanghai）。
     */
    @Scheduled(cron = "0 34 21 * * ?", zone = "Asia/Shanghai")
    public void drawAt2134() {
        drawPendingIssues();
    }

    /**
     * 每分钟兜底扫描，避免错过 cron 或补偿历史期次。
     */
    @Scheduled(fixedDelay = 60_000)
    public void drawFallbackScan() {
        drawPendingIssues();
    }

    private void drawPendingIssues() {
        List<ActivityIssue> pending = activityIssueService.listPendingDraw();
        if (pending.isEmpty()) {
            return;
        }
        log.info("待开奖期号数量: {}", pending.size());

        for (ActivityIssue issue : pending) {
            String lockKey = "lock:activity:draw:" + issue.getIssueNo();
            String lockVal = redisLockService.tryLock(lockKey, LOCK_EXPIRE_SECONDS);
            if (lockVal == null) {
                continue;
            }

            try {
                long seed = Math.abs(RND.nextLong());
                String payload = "{\"algo\":\"hash_mod\",\"seed\":" + seed + ",\"winRateBp\":" + DEFAULT_WIN_RATE_BP
                        + ",\"drawTime\":\"" + LocalDateTime.now() + "\"}";
                boolean ok = activityIssueService.markDrawn(issue.getId(), payload);
                if (ok) {
                    log.info("开奖写入成功: issueNo={}, payload={}", issue.getIssueNo(), payload);
                } else {
                    log.warn("开奖写入失败(可能已开奖/状态变化): issueNo={}", issue.getIssueNo());
                }
            } catch (Exception e) {
                log.error("开奖写入异常: issueNo={}", issue.getIssueNo(), e);
            } finally {
                redisLockService.release(lockKey, lockVal);
            }
        }
    }
}

