package com.debox.reward.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WalletLoginRequest {

    @NotBlank
    private String nonce;

    @NotBlank
    private String signature;

    @NotBlank
    private String walletAddress;
}
