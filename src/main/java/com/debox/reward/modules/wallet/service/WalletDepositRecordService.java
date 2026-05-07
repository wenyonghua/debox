package com.debox.reward.modules.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.wallet.dto.WalletDepositSubmitRequest;
import com.debox.reward.modules.wallet.entity.WalletDeposit;

public interface WalletDepositRecordService extends IService<WalletDeposit> {

    WalletDeposit submit(Long userId, WalletDepositSubmitRequest request);

    Page<WalletDeposit> pageForUser(Long userId, long page, long size);
}
