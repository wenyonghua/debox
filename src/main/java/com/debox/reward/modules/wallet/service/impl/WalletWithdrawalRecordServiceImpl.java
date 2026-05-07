package com.debox.reward.modules.wallet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.eth.EthereumPersonalSignVerifier;
import com.debox.reward.modules.wallet.dto.WalletWithdrawalSubmitRequest;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.mapper.WalletWithdrawalMapper;
import com.debox.reward.modules.wallet.service.WalletWithdrawalRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WalletWithdrawalRecordServiceImpl extends ServiceImpl<WalletWithdrawalMapper, WalletWithdrawal>
        implements WalletWithdrawalRecordService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawal submit(Long userId, WalletWithdrawalSubmitRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new BizException("amount 必须大于0");
        }
        AssetCode ac;
        try {
            ac = AssetCode.valueOf(request.getAssetCode().trim().toUpperCase());
        } catch (Exception e) {
            throw new BizException("不支持的资产类型");
        }
        String to = EthereumPersonalSignVerifier.normalizeAddress(request.getToAddress());
        if (!EthereumPersonalSignVerifier.looksLikeEvmAddress(to)) {
            throw new BizException("提现地址格式无效（暂仅支持 EVM 0x 地址）");
        }
        LocalDateTime now = LocalDateTime.now();
        WalletWithdrawal row = new WalletWithdrawal();
        row.setBizNo("WDR-" + UUID.randomUUID().toString().replace("-", ""));
        row.setUserId(userId);
        row.setAssetCode(ac);
        row.setAmount(request.getAmount());
        row.setToAddress(to);
        row.setStatus("PENDING");
        row.setExecutedBizNo(null);
        row.setFailureReason(null);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        save(row);
        return row;
    }

    @Override
    public Page<WalletWithdrawal> pageForUser(Long userId, long page, long size) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        return page(new Page<>(p, s), Wrappers.<WalletWithdrawal>lambdaQuery()
                .eq(WalletWithdrawal::getUserId, userId)
                .orderByDesc(WalletWithdrawal::getId));
    }
}
