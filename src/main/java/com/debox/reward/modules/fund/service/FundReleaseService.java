package com.debox.reward.modules.fund.service;

import java.math.BigDecimal;

public interface FundReleaseService {

    void createPlan(Long userId, String sourceBizNo, BigDecimal amount);

    void runDailyRelease();

    /**
     * 重试失败释放事件（status=2）
     */
    void retryFailedEvents(Long planId, int limit);
}

