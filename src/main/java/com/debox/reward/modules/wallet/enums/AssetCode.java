package com.debox.reward.modules.wallet.enums;

public enum AssetCode {
    /**
     * 兼容旧资产：积分/奖励
     */
    POINT,
    BONUS,
    /**
     * 目标业务资产（按功能清单）：USDT 与 小六币（SIX）及基金待释放（FUND_SIX）
     */
    USDT,
    SIX,
    FUND_SIX
}
