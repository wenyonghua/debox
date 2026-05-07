package com.debox.reward.modules.wallet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.eth.EthereumPersonalSignVerifier;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import com.debox.reward.modules.wallet.dto.WalletWithdrawalSubmitRequest;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.mapper.WalletWithdrawalMapper;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import com.debox.reward.modules.wallet.service.WalletWithdrawalRecordService;
import com.debox.reward.modules.wallet.util.ChainAddressValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletWithdrawalRecordServiceImpl extends ServiceImpl<WalletWithdrawalMapper, WalletWithdrawal>
        implements WalletWithdrawalRecordService {

    private final WalletLedgerService walletLedgerService;

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
        String toRaw = request.getToAddress() == null ? "" : request.getToAddress().trim();
        if (toRaw.isBlank()) {
            throw new BizException("toAddress 必填");
        }
        String to;
        if (toRaw.startsWith("0x") || toRaw.startsWith("0X")) {
            to = EthereumPersonalSignVerifier.normalizeAddress(toRaw);
            if (!EthereumPersonalSignVerifier.looksLikeEvmAddress(to)) {
                throw new BizException("提现地址格式无效（EVM 0x 地址）");
            }
        } else if (toRaw.startsWith("T")) {
            if (!ChainAddressValidator.isValidTronBase58Address(toRaw)) {
                throw new BizException("提现地址格式无效（TRC20 / TRON Base58Check）");
            }
            to = toRaw;
        } else {
            throw new BizException("提现地址格式无效（仅支持 EVM 0x / TRC20 T...）");
        }

        LocalDateTime now = LocalDateTime.now();
        WalletWithdrawal row = new WalletWithdrawal();
        row.setBizNo("WDR-" + UUID.randomUUID().toString().replace("-", ""));
        row.setUserId(userId);
        row.setAssetCode(ac);
        row.setAmount(request.getAmount());
        row.setToAddress(to);
        row.setStatus("SUBMITTED");
        row.setExecutedBizNo(null);
        row.setFailureReason(null);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        save(row);

        // 提交即冻结余额：避免重复提现/并发超额
        walletLedgerService.freeze(userId, ac, request.getAmount(),
                "WDR-FREEZE-" + row.getBizNo(),
                "提现冻结 " + row.getBizNo());

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
