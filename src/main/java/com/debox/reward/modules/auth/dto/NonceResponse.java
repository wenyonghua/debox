package com.debox.reward.modules.auth.dto;

import lombok.Data;

@Data
public class NonceResponse {
    private String nonce;
    private long expireSeconds;
}
