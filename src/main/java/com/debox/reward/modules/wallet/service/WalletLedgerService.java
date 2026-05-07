package com.debox.reward.modules.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.wallet.entity.WalletLedger;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.enums.WalletBizType;

import java.math.BigDecimal;

/**
 * 钱包流水服务：入账、扣账、冻结、解冻
 */
public interface WalletLedgerService extends IService<WalletLedger> {

    /**
     * 入账（增加可用余额）
     */
    void credit(Long userId, AssetCode assetCode, BigDecimal amount,
                WalletBizType bizType, String bizNo, String remark);

    /**
     * 扣账（减少可用余额）
     */
    void debit(Long userId, AssetCode assetCode, BigDecimal amount,
               WalletBizType bizType, String bizNo, String remark);

    /**
     * 冻结（可用余额转为冻结余额）
     */
    void freeze(Long userId, AssetCode assetCode, BigDecimal amount,
                String bizNo, String remark);

    /**
     * 解冻（冻结余额转为可用余额）
     */
    void unfreeze(Long userId, AssetCode assetCode, BigDecimal amount,
                  String bizNo, String remark);
}

