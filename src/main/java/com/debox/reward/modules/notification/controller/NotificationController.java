package com.debox.reward.modules.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.notification.entity.UserNotification;
import com.debox.reward.modules.notification.service.UserNotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final UserNotificationQueryService userNotificationQueryService;

    @GetMapping
    public Result<PageResult<UserNotification>> list(@RequestParam(defaultValue = "1") long page,
                                                      @RequestParam(defaultValue = "20") long size) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<UserNotification> pg = userNotificationQueryService.pageForUser(uid, p, s);
        PageResult<UserNotification> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        userNotificationQueryService.markRead(uid, id);
        return Result.ok(null);
    }
}
