package com.debox.reward.modules.wallet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.config.DepositAddressesProperties;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.wallet.dto.WalletDepositSubmitRequest;
import com.debox.reward.modules.wallet.dto.WalletWithdrawalSubmitRequest;
import com.debox.reward.modules.wallet.entity.WalletDeposit;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;
import com.debox.reward.modules.wallet.service.WalletDepositRecordService;
import com.debox.reward.modules.wallet.service.WalletWithdrawalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletFundingController {

    private final DepositAddressesProperties depositAddressesProperties;
    private final WalletDepositRecordService walletDepositRecordService;
    private final WalletWithdrawalRecordService walletWithdrawalRecordService;

    @GetMapping("/deposit-addresses")
    public Result<Map<String, String>> depositAddresses() {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        return Result.ok(depositAddressesProperties.getAddresses());
    }

    @GetMapping("/deposits")
    public Result<PageResult<WalletDeposit>> listDeposits(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "20") long size) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<WalletDeposit> pg = walletDepositRecordService.pageForUser(uid, p, s);
        PageResult<WalletDeposit> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @PostMapping("/deposits")
    public Result<WalletDeposit> submitDeposit(@Valid @RequestBody WalletDepositSubmitRequest request) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        return Result.ok(walletDepositRecordService.submit(uid, request));
    }

    @GetMapping("/withdrawals")
    public Result<PageResult<WalletWithdrawal>> listWithdrawals(@RequestParam(defaultValue = "1") long page,
                                                               @RequestParam(defaultValue = "20") long size) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<WalletWithdrawal> pg = walletWithdrawalRecordService.pageForUser(uid, p, s);
        PageResult<WalletWithdrawal> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }

    @PostMapping("/withdrawals")
    public Result<WalletWithdrawal> submitWithdrawal(@Valid @RequestBody WalletWithdrawalSubmitRequest request) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        return Result.ok(walletWithdrawalRecordService.submit(uid, request));
    }
}
