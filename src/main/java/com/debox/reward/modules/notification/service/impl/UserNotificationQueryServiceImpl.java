package com.debox.reward.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.notification.entity.UserNotification;
import com.debox.reward.modules.notification.mapper.UserNotificationMapper;
import com.debox.reward.modules.notification.service.UserNotificationQueryService;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationQueryServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification>
        implements UserNotificationQueryService {

    @Override
    public Page<UserNotification> pageForUser(Long userId, long page, long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        return page(new Page<>(p, s), Wrappers.<UserNotification>lambdaQuery()
                .eq(UserNotification::getUserId, userId)
                .orderByDesc(UserNotification::getId));
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        UserNotification row = getById(notificationId);
        if (row == null || !userId.equals(row.getUserId())) {
            throw new BizException("通知不存在或无权操作");
        }
        update(Wrappers.<UserNotification>lambdaUpdate()
                .set(UserNotification::getReadFlag, 1)
                .eq(UserNotification::getId, notificationId));
    }
}
