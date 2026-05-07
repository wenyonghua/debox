package com.debox.reward.modules.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDepositSubmitRequest {

    @NotBlank
    private String assetCode;

    @NotNull
    private BigDecimal amount;

    private String txHash;

    private String fromAddress;
}
