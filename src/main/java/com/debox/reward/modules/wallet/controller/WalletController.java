package com.debox.reward.modules.wallet.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.modules.wallet.entity.WalletAccount;
import com.debox.reward.modules.wallet.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletAccountService walletAccountService;

    @GetMapping("/accounts/{userId}")
    public Result<List<WalletAccount>> listAccounts(@PathVariable Long userId) {
        return Result.ok(walletAccountService.listByUserId(userId));
    }
}
