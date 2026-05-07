package com.debox.reward.modules.activity.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.enums.ActivityIssueStatus;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/draws")
@RequiredArgsConstructor
public class DrawController {

    private final ActivityIssueService activityIssueService;

    @GetMapping("/current")
    public Result<ActivityIssue> current() {
        ActivityIssue open = activityIssueService.getOpenIssue();
        if (open != null) {
            return Result.ok(open);
        }
        // 若没有 OPEN，则返回最近一条（可能已开奖/结算）
        ActivityIssue last = activityIssueService.getOne(Wrappers.<ActivityIssue>lambdaQuery()
                .orderByDesc(ActivityIssue::getEndTime)
                .last("limit 1"));
        return Result.ok(last);
    }

    @GetMapping
    public Result<List<ActivityIssue>> list(@RequestParam(defaultValue = "30") int limit) {
        int l = Math.max(1, Math.min(200, limit));
        List<ActivityIssue> list = activityIssueService.list(Wrappers.<ActivityIssue>lambdaQuery()
                .in(ActivityIssue::getStatus, ActivityIssueStatus.OPEN, ActivityIssueStatus.SETTLING, ActivityIssueStatus.SETTLED)
                .orderByDesc(ActivityIssue::getEndTime)
                .last("limit " + l));
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<ActivityIssue> detail(@PathVariable Long id) {
        return Result.ok(activityIssueService.getById(id));
    }
}

