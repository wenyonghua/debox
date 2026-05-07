package com.debox.reward.modules.compensation.dto;

import com.debox.reward.modules.wallet.enums.AssetCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompensationCreateRequest {
    @NotNull
    private Long userId;
    @NotNull
    private AssetCode assetCode;
    @NotBlank
    private String direction;
    @NotNull
    private BigDecimal amount;
    private String remark;
}
