package com.debox.reward.modules.job.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import com.debox.reward.modules.job.entity.RetryTask;
import com.debox.reward.modules.job.enums.RetryBizType;
import com.debox.reward.modules.job.enums.RetryTaskStatus;
import com.debox.reward.modules.job.mapper.RetryTaskMapper;
import com.debox.reward.modules.job.service.RetryTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class RetryTaskServiceImpl extends ServiceImpl<RetryTaskMapper, RetryTask> implements RetryTaskService {

    private static final String KEY_PREFIX_ORDER = "ORDER_SETTLEMENT:";

    private static final int DEFAULT_MAX = 144; // ~3 天，每 30 分钟一次量级可调

    @Lazy
    @Autowired
    private ActivityOrderService activityOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enqueueOrderSettlement(Long orderId, Long issueId, String errorSummary) {
        String bizKey = KEY_PREFIX_ORDER + orderId;
        Long exists = baseMapper.selectCount(Wrappers.<RetryTask>lambdaQuery()
                .eq(RetryTask::getBizKey, bizKey)
                .eq(RetryTask::getStatus, RetryTaskStatus.PENDING));
        if (exists != null && exists > 0) {
            log.warn("重试任务已存在: {}", bizKey);
            return;
        }
        RetryTask t = new RetryTask();
        t.setBizType(RetryBizType.ORDER_SETTLEMENT);
        t.setBizKey(bizKey);
        t.setOrderId(orderId);
        t.setIssueId(issueId);
        String err = errorSummary;
        if (err != null && err.length() > 2000) {
            err = err.substring(0, 2000);
        }
        t.setLastError(err);
        t.setStatus(RetryTaskStatus.PENDING);
        t.setRetryCount(0);
        t.setMaxRetries(DEFAULT_MAX);
        t.setNextRetryAt(LocalDateTime.now());
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        save(t);
    }

    @Override
    public void processDueTasks(int batchSize) {
        int n = Math.max(1, Math.min(100, batchSize));
        LocalDateTime now = LocalDateTime.now();
        List<RetryTask> tasks = list(Wrappers.<RetryTask>lambdaQuery()
                .eq(RetryTask::getStatus, RetryTaskStatus.PENDING)
                .le(RetryTask::getNextRetryAt, now)
                .last("LIMIT " + n));

        for (RetryTask t : tasks) {
            try {
                if (t.getBizType() == RetryBizType.ORDER_SETTLEMENT && t.getOrderId() != null) {
                    activityOrderService.settleSingleOrder(t.getOrderId());
                }
                t.setStatus(RetryTaskStatus.DONE);
                t.setUpdatedAt(LocalDateTime.now());
                updateById(t);
                log.info("重试任务成功: bizKey={}, id={}", t.getBizKey(), t.getId());
            } catch (Exception ex) {
                int rc = (t.getRetryCount() == null ? 0 : t.getRetryCount()) + 1;
                int max = t.getMaxRetries() == null ? DEFAULT_MAX : t.getMaxRetries();
                String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                if (msg.length() > 1800) {
                    msg = msg.substring(0, 1800);
                }
                t.setRetryCount(rc);
                t.setLastError(msg);
                if (rc >= max) {
                    t.setStatus(RetryTaskStatus.EXHAUSTED);
                } else {
                    // 递增退避：1min → 最长 120min
                    int delayMin = Math.min(120, rc * 2);
                    t.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMin));
                }
                t.setUpdatedAt(LocalDateTime.now());
                updateById(t);
                log.warn("重试任务失败 bizKey={}, attempt={}: {}", t.getBizKey(), rc, msg);
            }
        }
    }
}
