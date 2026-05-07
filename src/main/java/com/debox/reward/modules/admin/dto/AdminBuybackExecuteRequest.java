package com.debox.reward.modules.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminBuybackExecuteRequest {

    @NotNull
    private BigDecimal amountUsdt;

    private String remark;
}
