package com.debox.reward.modules.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_withdrawal")
public class WalletWithdrawal {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizNo;
    private Long userId;
    private AssetCode assetCode;
    private BigDecimal amount;
    private String toAddress;
    private String status;
    private String executedBizNo;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
