package com.debox.reward.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReferrerBindRequest {
    @NotBlank
    private String inviteCode;
}
