package com.debox.reward.modules.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reward_record")
public class RewardRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String rewardNo;
    private Long userId;
    private String eventType;
    private String sourceBizNo;
    private AssetCode assetCode;
    private BigDecimal amount;
    private Integer status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime grantedAt;
}
