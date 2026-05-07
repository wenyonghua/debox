package com.debox.reward.modules.wallet.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.wallet.entity.WalletLedger;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}

