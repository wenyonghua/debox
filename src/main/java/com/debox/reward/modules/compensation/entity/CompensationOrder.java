package com.debox.reward.modules.compensation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.compensation.enums.CompensationOrderStatus;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("compensation_order")
public class CompensationOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizNo;
    private Long userId;
    private AssetCode assetCode;
    /** credit / debit */
    private String direction;
    private BigDecimal amount;
    private String remark;
    private CompensationOrderStatus status;
    private Long createdBy;
    private Long approvedBy;
    private String executedBizNo;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
