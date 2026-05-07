package com.debox.reward.modules.activity.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
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
    public Result<PageResult<ActivityIssue>> list(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<ActivityIssue> pg = activityIssueService.page(new Page<>(p, s), Wrappers.<ActivityIssue>lambdaQuery()
                .in(ActivityIssue::getStatus, ActivityIssueStatus.OPEN, ActivityIssueStatus.SETTLING, ActivityIssueStatus.SETTLED)
                .orderByDesc(ActivityIssue::getEndTime));
        PageResult<ActivityIssue> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/{id}")
    public Result<ActivityIssue> detail(@PathVariable Long id) {
        return Result.ok(activityIssueService.getById(id));
    }
}

