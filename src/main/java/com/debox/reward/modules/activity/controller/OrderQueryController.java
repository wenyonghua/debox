package com.debox.reward.modules.activity.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.activity.dto.OrderDetailResponse;
import com.debox.reward.modules.activity.entity.ActivityOrder;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import com.debox.reward.modules.reward.entity.RewardAllocation;
import com.debox.reward.modules.reward.service.RewardAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final ActivityOrderService activityOrderService;
    private final RewardAllocationService rewardAllocationService;

    @GetMapping
    public Result<PageResult<ActivityOrder>> list(@RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) Long issueId,
                                                  @RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "20") long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<ActivityOrder> pg = activityOrderService.page(new Page<>(p, s), Wrappers.<ActivityOrder>lambdaQuery()
                .eq(userId != null, ActivityOrder::getUserId, userId)
                .eq(issueId != null, ActivityOrder::getIssueId, issueId)
                .orderByDesc(ActivityOrder::getCreatedAt));
        PageResult<ActivityOrder> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/{id}")
    public Result<OrderDetailResponse> detail(@PathVariable Long id) {
        ActivityOrder order = activityOrderService.getById(id);
        if (order == null) {
            return Result.ok(null);
        }
        List<RewardAllocation> allocations = rewardAllocationService.list(Wrappers.<RewardAllocation>lambdaQuery()
                .eq(RewardAllocation::getOrderId, order.getId())
                .orderByAsc(RewardAllocation::getId));
        OrderDetailResponse resp = new OrderDetailResponse();
        resp.setOrder(order);
        resp.setAllocations(allocations);
        return Result.ok(resp);
    }
}

