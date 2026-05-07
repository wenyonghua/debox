package com.debox.reward.modules.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.wallet.dto.WalletWithdrawalSubmitRequest;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;

public interface WalletWithdrawalRecordService extends IService<WalletWithdrawal> {

    WalletWithdrawal submit(Long userId, WalletWithdrawalSubmitRequest request);

    Page<WalletWithdrawal> pageForUser(Long userId, long page, long size);
}
