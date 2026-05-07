package com.debox.reward.modules.wallet.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.wallet.entity.WalletAccount;
import com.debox.reward.modules.wallet.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletBalancesController {

    private final WalletAccountService walletAccountService;

    /** 清单：balances（当前登录用户） */
    @GetMapping("/balances")
    public Result<List<WalletAccount>> balances() {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        return Result.ok(walletAccountService.listByUserId(uid));
    }
}
