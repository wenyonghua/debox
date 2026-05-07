package com.debox.reward.modules.wallet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.wallet.dto.WalletDepositSubmitRequest;
import com.debox.reward.modules.wallet.entity.WalletDeposit;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.mapper.WalletDepositMapper;
import com.debox.reward.modules.wallet.service.WalletDepositRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class WalletDepositRecordServiceImpl extends ServiceImpl<WalletDepositMapper, WalletDeposit>
        implements WalletDepositRecordService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletDeposit submit(Long userId, WalletDepositSubmitRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new BizException("amount 必须大于0");
        }
        AssetCode ac;
        try {
            ac = AssetCode.valueOf(request.getAssetCode().trim().toUpperCase());
        } catch (Exception e) {
            throw new BizException("不支持的资产类型");
        }
        LocalDateTime now = LocalDateTime.now();
        WalletDeposit row = new WalletDeposit();
        row.setUserId(userId);
        row.setAssetCode(ac);
        row.setAmount(request.getAmount());
        row.setStatus("SUBMITTED");
        row.setTxHash(blankToNull(request.getTxHash()));
        row.setFromAddress(blankToNull(request.getFromAddress()));
        row.setRemark(null);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        save(row);
        return row;
    }

    @Override
    public Page<WalletDeposit> pageForUser(Long userId, long page, long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        return page(new Page<>(p, s), Wrappers.<WalletDeposit>lambdaQuery()
                .eq(WalletDeposit::getUserId, userId)
                .orderByDesc(WalletDeposit::getId));
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
