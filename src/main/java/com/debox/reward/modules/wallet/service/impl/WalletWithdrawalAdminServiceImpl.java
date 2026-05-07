package com.debox.reward.modules.wallet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.wallet.entity.WalletWithdrawal;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import com.debox.reward.modules.wallet.mapper.WalletWithdrawalMapper;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import com.debox.reward.modules.wallet.service.WalletWithdrawalAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletWithdrawalAdminServiceImpl implements WalletWithdrawalAdminService {

    private final WalletWithdrawalMapper walletWithdrawalMapper;
    private final WalletLedgerService walletLedgerService;

    @Override
    public Page<WalletWithdrawal> page(long page, long size, String status, Long userId) {
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        return walletWithdrawalMapper.selectPage(new Page<>(p, s), Wrappers.<WalletWithdrawal>lambdaQuery()
                .eq(userId != null, WalletWithdrawal::getUserId, userId)
                .eq(status != null && !status.isBlank(), WalletWithdrawal::getStatus, status.trim())
                .orderByDesc(WalletWithdrawal::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawal approve(Long withdrawalId, Long adminId) {
        WalletWithdrawal w = walletWithdrawalMapper.selectById(withdrawalId);
        if (w == null) {
            throw new BizException("提现单不存在");
        }
        if (!"SUBMITTED".equalsIgnoreCase(w.getStatus())) {
            throw new BizException("当前状态不可审批: " + w.getStatus());
        }
        WalletWithdrawal upd = new WalletWithdrawal();
        upd.setId(w.getId());
        upd.setStatus("APPROVED");
        upd.setUpdatedAt(LocalDateTime.now());
        walletWithdrawalMapper.updateById(upd);
        return walletWithdrawalMapper.selectById(withdrawalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawal reject(Long withdrawalId, Long adminId, String reason) {
        WalletWithdrawal w = walletWithdrawalMapper.selectById(withdrawalId);
        if (w == null) {
            throw new BizException("提现单不存在");
        }
        if (!"SUBMITTED".equalsIgnoreCase(w.getStatus()) && !"APPROVED".equalsIgnoreCase(w.getStatus())) {
            throw new BizException("当前状态不可拒绝: " + w.getStatus());
        }
        WalletWithdrawal upd = new WalletWithdrawal();
        upd.setId(w.getId());
        upd.setStatus("REJECTED");
        upd.setFailureReason(reason);
        upd.setUpdatedAt(LocalDateTime.now());
        walletWithdrawalMapper.updateById(upd);

        walletLedgerService.unfreeze(w.getUserId(), w.getAssetCode(), w.getAmount(),
                "WDR-UNFREEZE-REJECT-" + w.getBizNo(),
                "提现拒绝解冻 " + w.getBizNo());
        return walletWithdrawalMapper.selectById(withdrawalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawal markExecuted(Long withdrawalId, Long adminId, String executedBizNo) {
        WalletWithdrawal w = walletWithdrawalMapper.selectById(withdrawalId);
        if (w == null) {
            throw new BizException("提现单不存在");
        }
        if (!"APPROVED".equalsIgnoreCase(w.getStatus())) {
            throw new BizException("当前状态不可执行: " + w.getStatus());
        }

        // 用“解冻 + 扣账”消费冻结余额（保持复用现有账本能力）
        walletLedgerService.unfreeze(w.getUserId(), w.getAssetCode(), w.getAmount(),
                "WDR-UNFREEZE-EXEC-" + w.getBizNo(),
                "提现执行解冻 " + w.getBizNo());
        walletLedgerService.debit(w.getUserId(), w.getAssetCode(), w.getAmount(),
                WalletBizType.WITHDRAWAL,
                "WDR-DEBIT-" + w.getBizNo(),
                "提现扣减 " + w.getBizNo());

        WalletWithdrawal upd = new WalletWithdrawal();
        upd.setId(w.getId());
        upd.setStatus("EXECUTED");
        upd.setExecutedBizNo(executedBizNo);
        upd.setUpdatedAt(LocalDateTime.now());
        walletWithdrawalMapper.updateById(upd);
        return walletWithdrawalMapper.selectById(withdrawalId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawal markFailed(Long withdrawalId, Long adminId, String reason) {
        WalletWithdrawal w = walletWithdrawalMapper.selectById(withdrawalId);
        if (w == null) {
            throw new BizException("提现单不存在");
        }
        if (!"APPROVED".equalsIgnoreCase(w.getStatus())) {
            throw new BizException("当前状态不可失败回滚: " + w.getStatus());
        }
        WalletWithdrawal upd = new WalletWithdrawal();
        upd.setId(w.getId());
        upd.setStatus("FAILED");
        upd.setFailureReason(reason);
        upd.setUpdatedAt(LocalDateTime.now());
        walletWithdrawalMapper.updateById(upd);

        walletLedgerService.unfreeze(w.getUserId(), w.getAssetCode(), w.getAmount(),
                "WDR-UNFREEZE-FAIL-" + w.getBizNo(),
                "提现失败解冻 " + w.getBizNo());
        return walletWithdrawalMapper.selectById(withdrawalId);
    }
}

