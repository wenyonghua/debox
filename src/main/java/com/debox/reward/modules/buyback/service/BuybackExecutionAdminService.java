package com.debox.reward.modules.buyback.service;

import com.debox.reward.modules.buyback.entity.BuybackExecution;

import java.math.BigDecimal;

/**
 * 管理端回购执行（链上占位：仅扣减池子余额并记执行流水）。
 */
public interface BuybackExecutionAdminService {

    BuybackExecution executeStub(BigDecimal amountUsdt, String remark);
}
