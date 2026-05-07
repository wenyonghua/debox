package com.debox.reward.modules.admin.service.impl;

import com.debox.reward.modules.admin.entity.AdminAuditLog;
import com.debox.reward.modules.admin.mapper.AdminAuditLogMapper;
import com.debox.reward.modules.admin.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AdminAuditLogMapper adminAuditLogMapper;

    @Override
    public void log(String action, String targetType, String targetId, String beforeJson, String afterJson) {
        AdminAuditLog l = new AdminAuditLog();
        l.setAdminId(currentAdminId());
        l.setAction(action);
        l.setTargetType(targetType);
        l.setTargetId(targetId);
        l.setBeforeJson(beforeJson);
        l.setAfterJson(afterJson);
        l.setCreatedAt(LocalDateTime.now());
        adminAuditLogMapper.insert(l);
    }

    private Long currentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(auth.getPrincipal()));
        } catch (Exception ignore) {
            return null;
        }
    }
}

