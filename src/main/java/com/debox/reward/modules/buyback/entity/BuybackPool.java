package com.debox.reward.modules.buyback.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("buyback_pool")
public class BuybackPool {

    @TableId
    private Integer id;
    private String assetCode;
    private BigDecimal balance;
    private LocalDateTime updatedAt;
}
