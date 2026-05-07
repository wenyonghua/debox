package com.debox.reward.modules.job.task;

import com.debox.reward.modules.job.service.RetryTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 异步失败任务自动重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryTaskScheduler {

    private final RetryTaskService retryTaskService;

    /** 每分钟处理一批到期任务 */
    @Scheduled(fixedDelay = 60_000)
    public void run() {
        try {
            retryTaskService.processDueTasks(50);
        } catch (Exception e) {
            log.error("重试调度异常", e);
        }
    }
}
