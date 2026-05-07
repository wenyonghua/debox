package com.debox.reward.modules.fund.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundSummaryResponse {
    private BigDecimal fundSixBalance;
    private BigDecimal sixBalance;
}

