package com.debox.reward.modules.admin.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import com.debox.reward.modules.admin.dto.AdminCreateSnapshotRequest;
import com.debox.reward.modules.admin.dto.AdminDrawRequest;
import com.debox.reward.modules.admin.dto.AdminFreezeUserRequest;
import com.debox.reward.modules.admin.dto.AdminRetryFundReleaseRequest;
import com.debox.reward.modules.admin.dto.AdminWalletAdjustRequest;
import com.debox.reward.modules.admin.service.AdminAuditLogService;
import com.debox.reward.modules.fund.service.FundReleaseService;
import com.debox.reward.modules.reward.entity.RuleSnapshot;
import com.debox.reward.modules.reward.service.RuleSnapshotService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.mapper.UserMapper;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final SecureRandom RND = new SecureRandom();

    private final RuleSnapshotService ruleSnapshotService;
    private final ActivityIssueService activityIssueService;
    private final ActivityOrderService activityOrderService;
    private final UserMapper userMapper;
    private final AdminAuditLogService adminAuditLogService;
    private final WalletLedgerService walletLedgerService;
    private final FundReleaseService fundReleaseService;

    @PostMapping("/rules/snapshots")
    public Result<RuleSnapshot> createSnapshot(@Valid @RequestBody AdminCreateSnapshotRequest request) {
        RuleSnapshot s = ruleSnapshotService.createSnapshot(request.getVersion(), request.getPayloadJson());
        adminAuditLogService.log("RULE_SNAPSHOT_CREATE", "rule_snapshot", String.valueOf(s.getId()),
                null, request.getPayloadJson());
        return Result.ok(s);
    }

    @PostMapping("/issues/{issueId}/draw")
    public Result<ActivityIssue> manualDraw(@PathVariable Long issueId, @RequestBody AdminDrawRequest request) {
        ActivityIssue issue = activityIssueService.getById(issueId);
        if (issue == null) {
            throw new BizException("期号不存在");
        }
        String before = issue.getResultPayload();

        Long snapshotId = request == null ? null : request.getRuleSnapshotId();
        if (snapshotId == null && request != null && request.getPayloadJson() != null && request.getVersion() != null) {
            RuleSnapshot s = ruleSnapshotService.createSnapshot(request.getVersion(), request.getPayloadJson());
            snapshotId = s.getId();
        }

        long seed = request != null && request.getSeed() != null ? Math.abs(request.getSeed()) : Math.abs(RND.nextLong());
        int winRateBp = request != null && request.getWinRateBp() != null ? request.getWinRateBp() : 1000;

        String payload = "{\"algo\":\"hash_mod\",\"seed\":" + seed + ",\"winRateBp\":" + winRateBp
                + (snapshotId == null ? "" : ",\"ruleSnapshotId\":" + snapshotId)
                + ",\"drawTime\":\"" + LocalDateTime.now() + "\",\"source\":\"manual\"}";

        boolean ok = activityIssueService.markDrawn(issueId, payload);
        if (!ok) {
            throw new BizException("开奖写入失败（可能已开奖/状态不允许）");
        }
        if (snapshotId != null) {
            issue.setRuleSnapshotId(snapshotId);
            issue.setResultPayload(payload);
            issue.setDrawnAt(LocalDateTime.now());
            activityIssueService.updateById(issue);
        }
        ActivityIssue afterIssue = activityIssueService.getById(issueId);
        adminAuditLogService.log("ISSUE_DRAW_MANUAL", "activity_issue", String.valueOf(issueId), before, payload);
        return Result.ok(afterIssue);
    }

    @PostMapping("/issues/{issueId}/settle")
    public Result<Void> manualSettle(@PathVariable Long issueId) {
        activityOrderService.settleIssueOrders(issueId);
        adminAuditLogService.log("ISSUE_SETTLE_MANUAL", "activity_issue", String.valueOf(issueId), null, null);
        return Result.ok(null);
    }

    @PostMapping("/users/{userId}/freeze")
    public Result<User> freezeUser(@PathVariable Long userId, @RequestBody AdminFreezeUserRequest request) {
        if (request == null || request.getStatus() == null) {
            throw new BizException("status 必填");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        String before = "{\"status\":" + user.getStatus() + "}";
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        User after = userMapper.selectById(userId);
        String afterJson = "{\"status\":" + after.getStatus() + "}";
        adminAuditLogService.log("USER_FREEZE", "sys_user", String.valueOf(userId), before, afterJson);
        return Result.ok(after);
    }

    @PostMapping("/wallet/adjust")
    public Result<Void> walletAdjust(@Valid @RequestBody AdminWalletAdjustRequest request) {
        if (request.getAmount().signum() <= 0) {
            throw new BizException("amount 必须大于0");
        }
        String bizNo = (request.getBizNo() == null || request.getBizNo().isBlank())
                ? ("ADJ-" + System.currentTimeMillis() + "-" + request.getUserId())
                : request.getBizNo();
        String remark = request.getRemark() == null ? "管理员调整" : request.getRemark();

        if ("credit".equalsIgnoreCase(request.getDirection())) {
            walletLedgerService.credit(request.getUserId(), request.getAssetCode(), request.getAmount(),
                    request.getBizType(), bizNo, remark);
        } else if ("debit".equalsIgnoreCase(request.getDirection())) {
            walletLedgerService.debit(request.getUserId(), request.getAssetCode(), request.getAmount(),
                    request.getBizType(), bizNo, remark);
        } else {
            throw new BizException("direction 仅支持 credit/debit");
        }

        adminAuditLogService.log("WALLET_ADJUST", "wallet_account", String.valueOf(request.getUserId()),
                null,
                "{\"asset\":\"" + request.getAssetCode() + "\",\"direction\":\"" + request.getDirection()
                        + "\",\"amount\":\"" + request.getAmount() + "\",\"bizNo\":\"" + bizNo + "\"}");
        return Result.ok(null);
    }

    @PostMapping("/fund/release/retry")
    public Result<Void> retryFundRelease(@RequestBody AdminRetryFundReleaseRequest request) {
        Long planId = request == null ? null : request.getPlanId();
        int limit = request == null || request.getLimit() == null ? 100 : request.getLimit();
        fundReleaseService.retryFailedEvents(planId, limit);
        adminAuditLogService.log("FUND_RELEASE_RETRY", "fund_release_event", planId == null ? null : String.valueOf(planId),
                null, "{\"limit\":" + limit + "}");
        return Result.ok(null);
    }
}

