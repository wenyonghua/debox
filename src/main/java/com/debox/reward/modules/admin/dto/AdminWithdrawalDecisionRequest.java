package com.debox.reward.modules.admin.dto;

import lombok.Data;

@Data
public class AdminWithdrawalDecisionRequest {

    /** 拒绝原因 / 失败原因 / 备注 */
    private String reason;

    /** 交易所/钱包/第三方流水号等 */
    private String executedBizNo;
}

