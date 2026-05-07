package com.debox.reward.modules.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.notification.entity.UserNotification;

public interface UserNotificationQueryService extends IService<UserNotification> {

    Page<UserNotification> pageForUser(Long userId, long page, long size);

    void markRead(Long userId, Long notificationId);
}
