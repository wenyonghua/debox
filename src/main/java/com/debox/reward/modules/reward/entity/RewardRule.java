package com.debox.reward.modules.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.user.enums.UserRole;
import com.debox.reward.modules.wallet.enums.AssetCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reward_rule")
public class RewardRule {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventType;
    private UserRole userRole;
    private AssetCode rewardAssetCode;
    private BigDecimal rewardRate;
    private BigDecimal maxRewardAmount;
    private Integer enabled;
    private LocalDateTime effectiveAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
