package com.debox.reward.modules.admin.dto;

import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminWalletAdjustRequest {
    @NotNull
    private Long userId;
    @NotNull
    private AssetCode assetCode;
    @NotNull
    private BigDecimal amount;

    /**
     * credit=入账，debit=扣账
     */
    @NotNull
    private String direction;

    /**
     * 业务号（用于幂等），不传则自动生成
     */
    private String bizNo;

    private String remark;

    private WalletBizType bizType = WalletBizType.ADJUST;
}

