package com.debox.reward.modules.admin.dto;

import lombok.Data;

@Data
public class AdminFreezeUserRequest {
    /**
     * 1=正常，0=冻结
     */
    private Integer status;
}

