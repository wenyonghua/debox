package com.debox.reward.modules.activity.dto;

import com.debox.reward.modules.wallet.enums.AssetCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateActivityOrderRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "期号ID不能为空")
    private Long issueId;

    @NotNull(message = "资产类型不能为空")
    private AssetCode assetCode;

    @NotNull(message = "参与数量不能为空")
    @DecimalMin(value = "0.000000000000000001", message = "参与数量必须大于0")
    private BigDecimal amount;
}
