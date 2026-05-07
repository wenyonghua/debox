package com.debox.reward.modules.reward.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.reward.dto.RewardSummaryResponse;
import com.debox.reward.modules.reward.entity.RewardAllocation;
import com.debox.reward.modules.reward.service.RewardAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardQueryController {

    private final RewardAllocationService rewardAllocationService;

    @GetMapping("/records")
    public Result<PageResult<RewardAllocation>> records(@RequestParam Long userId,
                                                        @RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "20") long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<RewardAllocation> pg = rewardAllocationService.page(new Page<>(p, s), Wrappers.<RewardAllocation>lambdaQuery()
                .eq(RewardAllocation::getBeneficiaryUserId, userId)
                .orderByDesc(RewardAllocation::getId));
        PageResult<RewardAllocation> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/summary")
    public Result<RewardSummaryResponse> summary(@RequestParam Long userId) {
        List<RewardAllocation> list = rewardAllocationService.list(Wrappers.<RewardAllocation>lambdaQuery()
                .eq(RewardAllocation::getBeneficiaryUserId, userId));
        BigDecimal total = BigDecimal.ZERO;
        for (RewardAllocation a : list) {
            if (a.getAmount() != null) {
                total = total.add(a.getAmount());
            }
        }
        RewardSummaryResponse resp = new RewardSummaryResponse();
        resp.setTotalAmount(total);
        return Result.ok(resp);
    }
}

