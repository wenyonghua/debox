package com.debox.reward.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String mobile;

    private String password;

    private String inviteCode;
}
