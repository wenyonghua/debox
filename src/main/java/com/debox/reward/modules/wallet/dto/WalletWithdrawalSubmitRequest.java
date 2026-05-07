package com.debox.reward.modules.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletWithdrawalSubmitRequest {

    @NotBlank
    private String assetCode;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String toAddress;
}
