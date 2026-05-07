package com.debox.reward.modules.activity.service;

import com.debox.reward.common.redis.RedisLockService;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 活动期号定时结算任务
 * 每分钟扫描已结束但未结算的期号，加分布式锁后逐一结算
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivitySettleTask {

    private static final long LOCK_EXPIRE_SECONDS = 300L;

    private final ActivityIssueService activityIssueService;
    private final ActivityOrderService activityOrderService;
    private final RedisLockService redisLockService;

    @Scheduled(fixedDelay = 60_000)
    public void settle() {
        List<ActivityIssue> pendingList = activityIssueService.listPendingClose();
        if (pendingList.isEmpty()) {
            return;
        }
        log.info("待结算期号数量: {}", pendingList.size());

        for (ActivityIssue issue : pendingList) {
            String lockKey = "lock:activity:issue:" + issue.getIssueNo();
            String lockValue = redisLockService.tryLock(lockKey, LOCK_EXPIRE_SECONDS);
            if (lockValue == null) {
                log.warn("获取结算锁失败，跳过期号: {}", issue.getIssueNo());
                continue;
            }

            try {
                // CAS 更新状态，防止并发重复结算
                boolean marked = activityIssueService.markSettling(issue.getId());
                if (!marked) {
                    log.warn("期号状态已变更，跳过: {}", issue.getIssueNo());
                    continue;
                }

                log.info("开始结算期号: {}", issue.getIssueNo());
                activityOrderService.settleIssueOrders(issue.getId());
                // settleIssueOrders 内部在全部订单 CREATED 清零后才会 markSettled；部分失败留在 SETTLING 由恢复任务继续
                log.info("期号结算批次结束: {}", issue.getIssueNo());

            } catch (Exception e) {
                log.error("期号结算异常: issueNo={}", issue.getIssueNo(), e);
            } finally {
                redisLockService.release(lockKey, lockValue);
            }
        }
    }
}

