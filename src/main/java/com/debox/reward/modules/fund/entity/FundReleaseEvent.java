package com.debox.reward.modules.fund.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fund_release_event")
public class FundReleaseEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;
    private Long userId;
    private LocalDate releaseDate;
    private BigDecimal amount;
    private Integer status; // 0=pending,1=posted,2=failed
    private String bizNo;
    private LocalDateTime createdAt;
    private LocalDateTime postedAt;
}

