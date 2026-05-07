package com.debox.reward.modules.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String role;
}

