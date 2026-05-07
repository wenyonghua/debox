package com.debox.reward.modules.admin.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import com.debox.reward.modules.admin.dto.AdminCreateSnapshotRequest;
import com.debox.reward.modules.admin.dto.AdminDrawRequest;
import com.debox.reward.modules.admin.dto.AdminFreezeUserRequest;
import com.debox.reward.modules.reward.entity.RuleSnapshot;
import com.debox.reward.modules.reward.service.RuleSnapshotService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.mapper.UserMapper;
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

    @PostMapping("/rules/snapshots")
    public Result<RuleSnapshot> createSnapshot(@Valid @RequestBody AdminCreateSnapshotRequest request) {
        return Result.ok(ruleSnapshotService.createSnapshot(request.getVersion(), request.getPayloadJson()));
    }

    @PostMapping("/issues/{issueId}/draw")
    public Result<ActivityIssue> manualDraw(@PathVariable Long issueId, @RequestBody AdminDrawRequest request) {
        ActivityIssue issue = activityIssueService.getById(issueId);
        if (issue == null) {
            throw new BizException("期号不存在");
        }

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
        return Result.ok(activityIssueService.getById(issueId));
    }

    @PostMapping("/issues/{issueId}/settle")
    public Result<Void> manualSettle(@PathVariable Long issueId) {
        activityOrderService.settleIssueOrders(issueId);
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
        user.setStatus(request.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.ok(userMapper.selectById(userId));
    }
}

