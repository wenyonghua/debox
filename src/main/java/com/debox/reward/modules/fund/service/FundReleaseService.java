package com.debox.reward.modules.fund.service;

import java.math.BigDecimal;

public interface FundReleaseService {

    void createPlan(Long userId, String sourceBizNo, BigDecimal amount);

    /** 按 sourceBizNo 幂等创建释放计划（重试结算不会重复插入） */
    void createPlanIfAbsent(Long userId, String sourceBizNo, BigDecimal amount);

    void runDailyRelease();

    /**
     * 重试失败释放事件（status=2）
     */
    void retryFailedEvents(Long planId, int limit);
}

