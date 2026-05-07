package com.debox.reward.modules.admin.service;

public interface AdminAuditLogService {
    void log(String action, String targetType, String targetId, String beforeJson, String afterJson);
}

