package com.debox.reward.modules.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;

public interface WalletWithdrawalAdminService {

    Page<WalletWithdrawal> page(long page, long size, String status, Long userId);

    WalletWithdrawal approve(Long withdrawalId, Long adminId);

    WalletWithdrawal reject(Long withdrawalId, Long adminId, String reason);

    WalletWithdrawal markExecuted(Long withdrawalId, Long adminId, String executedBizNo);

    WalletWithdrawal markFailed(Long withdrawalId, Long adminId, String reason);
}

