package com.debox.reward.modules.admin.dto;

import lombok.Data;

@Data
public class AdminRetryFundReleaseRequest {
    private Long planId;
    private Integer limit = 100;
}

