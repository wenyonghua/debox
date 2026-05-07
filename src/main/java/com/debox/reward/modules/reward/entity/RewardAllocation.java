package com.debox.reward.modules.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.reward.enums.AllocationType;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reward_allocation")
public class RewardAllocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceBizNo;
    private Long issueId;
    private Long orderId;

    private AllocationType type;
    private Long beneficiaryUserId;
    private AssetCode assetCode;
    private BigDecimal amount;

    private Integer status; // 0=pending,1=posted,2=failed
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime postedAt;
}

