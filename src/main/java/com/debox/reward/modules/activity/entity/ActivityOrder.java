package com.debox.reward.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.activity.enums.ActivityOrderStatus;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("activity_order")
public class ActivityOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long issueId;
    private AssetCode assetCode;
    private BigDecimal amount;
    private ActivityOrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime settledAt;
}
