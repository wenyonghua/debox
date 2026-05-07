package com.debox.reward.modules.wallet.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
    public Result<List<WalletLedger>> list(@RequestParam Long userId,
                                          @RequestParam(defaultValue = "100") int limit) {
        int l = Math.max(1, Math.min(200, limit));
        return Result.ok(walletLedgerService.list(Wrappers.<WalletLedger>lambdaQuery()
                .eq(WalletLedger::getUserId, userId)
                .orderByDesc(WalletLedger::getId)
                .last("limit " + l)));
    }
}

