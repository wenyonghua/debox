package com.debox.reward.modules.user.dto;

import com.debox.reward.modules.user.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String inviteCode;
    private Long parentId;
    private UserRole role;
    private Integer status;
    private LocalDateTime createdAt;
}
