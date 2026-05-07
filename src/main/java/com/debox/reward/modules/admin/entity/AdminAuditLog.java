package com.debox.reward.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_audit_log")
public class AdminAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;
    private String action;
    private String targetType;
    private String targetId;
    private String beforeJson;
    private String afterJson;
    private LocalDateTime createdAt;
}

