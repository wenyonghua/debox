package com.debox.reward.modules.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fund_release_plan")
public class FundReleasePlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String sourceBizNo;
    private BigDecimal totalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal dailyRate; // 0.005 = 0.5%/day
    private Integer status; // 1=active,2=done,3=paused
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

