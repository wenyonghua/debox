package com.debox.reward.modules.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.entity.ActivityOrder;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import com.debox.reward.modules.admin.entity.AdminAuditLog;
import com.debox.reward.modules.admin.mapper.AdminAuditLogMapper;
import com.debox.reward.modules.reward.entity.RewardAllocation;
import com.debox.reward.modules.reward.service.RewardAllocationService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/query")
@RequiredArgsConstructor
public class AdminQueryController {

    private final AdminAuditLogMapper adminAuditLogMapper;
    private final UserService userService;
    private final ActivityIssueService activityIssueService;
    private final ActivityOrderService activityOrderService;
    private final RewardAllocationService rewardAllocationService;

    @GetMapping("/audit-logs")
    public Result<PageResult<AdminAuditLog>> auditLogs(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "20") long size,
                                                       @RequestParam(required = false) Long adminId,
                                                       @RequestParam(required = false) String action) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<AdminAuditLog> pg = adminAuditLogMapper.selectPage(new Page<>(p, s),
                Wrappers.<AdminAuditLog>lambdaQuery()
                        .eq(adminId != null, AdminAuditLog::getAdminId, adminId)
                        .eq(action != null && !action.isBlank(), AdminAuditLog::getAction, action)
                        .orderByDesc(AdminAuditLog::getId));
        PageResult<AdminAuditLog> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) String username) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<User> pg = userService.page(new Page<>(p, s),
                Wrappers.<User>lambdaQuery()
                        .eq(status != null, User::getStatus, status)
                        .like(username != null && !username.isBlank(), User::getUsername, username)
                        .orderByDesc(User::getId));
        PageResult<User> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/issues")
    public Result<PageResult<ActivityIssue>> issues(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "20") long size,
                                                    @RequestParam(required = false) String status) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<ActivityIssue> pg = activityIssueService.page(new Page<>(p, s),
                Wrappers.<ActivityIssue>lambdaQuery()
                        .eq(status != null && !status.isBlank(), ActivityIssue::getStatus, status)
                        .orderByDesc(ActivityIssue::getEndTime));
        PageResult<ActivityIssue> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/orders")
    public Result<PageResult<ActivityOrder>> orders(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "20") long size,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) Long issueId,
                                                    @RequestParam(required = false) String status) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<ActivityOrder> pg = activityOrderService.page(new Page<>(p, s),
                Wrappers.<ActivityOrder>lambdaQuery()
                        .eq(userId != null, ActivityOrder::getUserId, userId)
                        .eq(issueId != null, ActivityOrder::getIssueId, issueId)
                        .eq(status != null && !status.isBlank(), ActivityOrder::getStatus, status)
                        .orderByDesc(ActivityOrder::getId));
        PageResult<ActivityOrder> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/allocations")
    public Result<PageResult<RewardAllocation>> allocations(@RequestParam(defaultValue = "1") long page,
                                                            @RequestParam(defaultValue = "20") long size,
                                                            @RequestParam(required = false) Long beneficiaryUserId,
                                                            @RequestParam(required = false) Long issueId,
                                                            @RequestParam(required = false) String sourceBizNo) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<RewardAllocation> pg = rewardAllocationService.page(new Page<>(p, s),
                Wrappers.<RewardAllocation>lambdaQuery()
                        .eq(beneficiaryUserId != null, RewardAllocation::getBeneficiaryUserId, beneficiaryUserId)
                        .eq(issueId != null, RewardAllocation::getIssueId, issueId)
                        .eq(sourceBizNo != null && !sourceBizNo.isBlank(), RewardAllocation::getSourceBizNo, sourceBizNo)
                        .orderByDesc(RewardAllocation::getId));
        PageResult<RewardAllocation> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }
}

