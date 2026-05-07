package com.debox.reward.modules.fund.service;

import java.math.BigDecimal;

public interface FundReleaseService {

    void createPlan(Long userId, String sourceBizNo, BigDecimal amount);

    void runDailyRelease();
}

