package com.debox.reward.modules.activity.service;

import com.debox.reward.common.redis.RedisLockService;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 期号处于 SETTLING 但仍存在未结订单时继续尝试结算（配合失败重试队列）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityResumeSettlingTask {

    private static final long LOCK_EXPIRE_SECONDS = 300L;

    private final ActivityIssueService activityIssueService;
    private final ActivityOrderService activityOrderService;
    private final RedisLockService redisLockService;

    @Scheduled(fixedDelay = 120_000)
    public void resume() {
        List<ActivityIssue> issues = activityIssueService.listIncompleteSettling();
        if (issues.isEmpty()) {
            return;
        }

        for (ActivityIssue issue : issues) {
            String lockKey = "lock:activity:resume:" + issue.getIssueNo();
            String lockVal = redisLockService.tryLock(lockKey, LOCK_EXPIRE_SECONDS);
            if (lockVal == null) {
                continue;
            }
            try {
                activityOrderService.settleIssueOrders(issue.getId());
            } catch (Exception e) {
                log.error("恢复结算异常 issueNo={}", issue.getIssueNo(), e);
            } finally {
                redisLockService.release(lockKey, lockVal);
            }
        }
    }
}
