package com.debox.reward.modules.buyback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("buyback_execution")
public class BuybackExecution {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizNo;
    private BigDecimal amountUsdt;
    private String status;
    private String swapTxHash;
    private String burnTxHash;
    private String remark;
    private LocalDateTime createdAt;
}
