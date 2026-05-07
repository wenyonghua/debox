package com.debox.reward.modules.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_account")
public class WalletAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private AssetCode assetCode;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
