package com.debox.reward.modules.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_ledger")
public class WalletLedger {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private AssetCode assetCode;
    private BigDecimal changeAmount;
    private BigDecimal balanceAfter;
    private WalletBizType bizType;
    private String bizNo;
    private String remark;
    private LocalDateTime createdAt;
}
