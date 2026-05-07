package com.debox.reward.modules.wallet.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.wallet.entity.WalletLedger;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final WalletLedgerService walletLedgerService;

    @GetMapping("/entries")
    public Result<PageResult<WalletLedger>> list(@RequestParam Long userId,
                                                 @RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "20") long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<WalletLedger> pg = walletLedgerService.page(new Page<>(p, s), Wrappers.<WalletLedger>lambdaQuery()
                .eq(WalletLedger::getUserId, userId)
                .orderByDesc(WalletLedger::getId));
        PageResult<WalletLedger> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @GetMapping("/entries/{id}")
    public Result<WalletLedger> entry(@PathVariable Long id) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        WalletLedger row = walletLedgerService.getById(id);
        if (row == null || !uid.equals(row.getUserId())) {
            throw new BizException("流水不存在或无权查看");
        }
        return Result.ok(row);
    }
}

