package com.debox.reward.modules.activity.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.modules.activity.dto.CreateActivityOrderRequest;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.entity.ActivityOrder;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityIssueService activityIssueService;
    private final ActivityOrderService activityOrderService;

    @GetMapping("/issues/open")
    public Result<ActivityIssue> getOpenIssue() {
        return Result.ok(activityIssueService.getOpenIssue());
    }

    @PostMapping("/orders")
    public Result<ActivityOrder> createOrder(@Valid @RequestBody CreateActivityOrderRequest request) {
        return Result.ok(activityOrderService.createOrder(request));
    }
}
