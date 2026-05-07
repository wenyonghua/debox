package com.debox.reward.modules.fund.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.common.api.Result;
import com.debox.reward.modules.fund.dto.FundSummaryResponse;
import com.debox.reward.modules.fund.entity.FundReleaseEvent;
import com.debox.reward.modules.fund.mapper.FundReleaseEventMapper;
import com.debox.reward.modules.wallet.entity.WalletAccount;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fund")
@RequiredArgsConstructor
public class FundController {

    private final WalletAccountService walletAccountService;
    private final FundReleaseEventMapper fundReleaseEventMapper;

    @GetMapping
    public Result<FundSummaryResponse> summary(@RequestParam Long userId) {
        List<WalletAccount> accounts = walletAccountService.listByUserId(userId);
        BigDecimal fund = BigDecimal.ZERO;
        BigDecimal six = BigDecimal.ZERO;
        for (WalletAccount a : accounts) {
            if (a.getAssetCode() == AssetCode.FUND_SIX) {
                fund = a.getAvailableBalance();
            }
            if (a.getAssetCode() == AssetCode.SIX) {
                six = a.getAvailableBalance();
            }
        }
        FundSummaryResponse resp = new FundSummaryResponse();
        resp.setFundSixBalance(fund);
        resp.setSixBalance(six);
        return Result.ok(resp);
    }

    @GetMapping("/releases")
    public Result<List<FundReleaseEvent>> releases(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "100") int limit) {
        int l = Math.max(1, Math.min(200, limit));
        return Result.ok(fundReleaseEventMapper.selectList(Wrappers.<FundReleaseEvent>lambdaQuery()
                .eq(FundReleaseEvent::getUserId, userId)
                .orderByDesc(FundReleaseEvent::getId)
                .last("limit " + l)));
    }
}

