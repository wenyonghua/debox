package com.debox.reward.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.admin.dto.AdminWithdrawalDecisionRequest;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;
import com.debox.reward.modules.wallet.service.WalletWithdrawalAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/withdrawals")
@RequiredArgsConstructor
public class AdminWithdrawalController {

    private final WalletWithdrawalAdminService walletWithdrawalAdminService;

    @GetMapping
    public Result<PageResult<WalletWithdrawal>> list(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "20") long size,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Long userId) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<WalletWithdrawal> pg = walletWithdrawalAdminService.page(p, s, status, userId);
        PageResult<WalletWithdrawal> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @PostMapping("/{id}/approve")
    public Result<WalletWithdrawal> approve(@PathVariable Long id) {
        Long adminId = SecurityUtils.currentUserId();
        if (adminId == null) {
            throw new BizException("未登录");
        }
        return Result.ok(walletWithdrawalAdminService.approve(id, adminId));
    }

    @PostMapping("/{id}/reject")
    public Result<WalletWithdrawal> reject(@PathVariable Long id, @RequestBody(required = false) AdminWithdrawalDecisionRequest req) {
        Long adminId = SecurityUtils.currentUserId();
        if (adminId == null) {
            throw new BizException("未登录");
        }
        String reason = req == null ? null : req.getReason();
        return Result.ok(walletWithdrawalAdminService.reject(id, adminId, reason));
    }

    @PostMapping("/{id}/executed")
    public Result<WalletWithdrawal> executed(@PathVariable Long id, @RequestBody(required = false) AdminWithdrawalDecisionRequest req) {
        Long adminId = SecurityUtils.currentUserId();
        if (adminId == null) {
            throw new BizException("未登录");
        }
        String executedBizNo = req == null ? null : req.getExecutedBizNo();
        return Result.ok(walletWithdrawalAdminService.markExecuted(id, adminId, executedBizNo));
    }

    @PostMapping("/{id}/failed")
    public Result<WalletWithdrawal> failed(@PathVariable Long id, @RequestBody(required = false) AdminWithdrawalDecisionRequest req) {
        Long adminId = SecurityUtils.currentUserId();
        if (adminId == null) {
            throw new BizException("未登录");
        }
        String reason = req == null ? null : req.getReason();
        return Result.ok(walletWithdrawalAdminService.markFailed(id, adminId, reason));
    }
}

