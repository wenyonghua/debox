package com.debox.reward.modules.wallet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.wallet.entity.WalletAccount;
import com.debox.reward.modules.wallet.entity.WalletLedger;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import com.debox.reward.modules.wallet.mapper.WalletAccountMapper;
import com.debox.reward.modules.wallet.mapper.WalletLedgerMapper;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletLedgerServiceImpl extends ServiceImpl<WalletLedgerMapper, WalletLedger>
        implements WalletLedgerService {

    private final WalletAccountMapper walletAccountMapper;
    private final WalletLedgerMapper walletLedgerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void credit(Long userId, AssetCode assetCode, BigDecimal amount,
                       WalletBizType bizType, String bizNo, String remark) {
        if (isIdempotentDuplicate(bizNo)) return;
        WalletAccount account = lockAccount(userId, assetCode);
        BigDecimal newBalance = account.getAvailableBalance().add(amount);
        updateAvailableBalance(account, newBalance);
        writeLedger(userId, assetCode, amount, newBalance, bizType, bizNo, remark);
        log.info("钱包入账: userId={}, asset={}, amount={}, bizNo={}", userId, assetCode, amount, bizNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debit(Long userId, AssetCode assetCode, BigDecimal amount,
                      WalletBizType bizType, String bizNo, String remark) {
        if (isIdempotentDuplicate(bizNo)) return;
        WalletAccount account = lockAccount(userId, assetCode);
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BizException("可用余额不足");
        }
        BigDecimal newBalance = account.getAvailableBalance().subtract(amount);
        updateAvailableBalance(account, newBalance);
        writeLedger(userId, assetCode, amount.negate(), newBalance, bizType, bizNo, remark);
        log.info("钱包扣账: userId={}, asset={}, amount={}, bizNo={}", userId, assetCode, amount, bizNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long userId, AssetCode assetCode, BigDecimal amount,
                       String bizNo, String remark) {
        if (isIdempotentDuplicate(bizNo)) return;
        WalletAccount account = lockAccount(userId, assetCode);
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new BizException("可用余额不足，无法冻结");
        }
        BigDecimal newAvailable = account.getAvailableBalance().subtract(amount);
        BigDecimal newFrozen = account.getFrozenBalance().add(amount);
        updateBalance(account, newAvailable, newFrozen);
        writeLedger(userId, assetCode, amount.negate(), newAvailable, WalletBizType.FREEZE, bizNo, remark);
        log.info("钱包冻结: userId={}, asset={}, amount={}, bizNo={}", userId, assetCode, amount, bizNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long userId, AssetCode assetCode, BigDecimal amount,
                         String bizNo, String remark) {
        if (isIdempotentDuplicate(bizNo)) return;
        WalletAccount account = lockAccount(userId, assetCode);
        if (account.getFrozenBalance().compareTo(amount) < 0) {
            throw new BizException("冻结余额不足，无法解冻");
        }
        BigDecimal newAvailable = account.getAvailableBalance().add(amount);
        BigDecimal newFrozen = account.getFrozenBalance().subtract(amount);
        updateBalance(account, newAvailable, newFrozen);
        writeLedger(userId, assetCode, amount, newAvailable, WalletBizType.UNFREEZE, bizNo, remark);
        log.info("钱包解冻: userId={}, asset={}, amount={}, bizNo={}", userId, assetCode, amount, bizNo);
    }

    // ---------------------- 私有辅助方法 ----------------------

    private boolean isIdempotentDuplicate(String bizNo) {
        Long count = walletLedgerMapper.selectCount(
                Wrappers.<WalletLedger>lambdaQuery().eq(WalletLedger::getBizNo, bizNo));
        if (count > 0) {
            log.warn("钱包流水幂等重复: bizNo={}", bizNo);
            return true;
        }
        return false;
    }

    private WalletAccount lockAccount(Long userId, AssetCode assetCode) {
        WalletAccount account = walletAccountMapper.selectOne(
                Wrappers.<WalletAccount>lambdaQuery()
                        .eq(WalletAccount::getUserId, userId)
                        .eq(WalletAccount::getAssetCode, assetCode)
                        .last("FOR UPDATE"));
        if (account != null) {
            return account;
        }

        // 兼容历史数据或系统账户：若账户不存在则创建，再次加锁查询
        WalletAccount created = new WalletAccount();
        created.setUserId(userId);
        created.setAssetCode(assetCode);
        created.setAvailableBalance(BigDecimal.ZERO);
        created.setFrozenBalance(BigDecimal.ZERO);
        created.setStatus(1);
        created.setCreatedAt(LocalDateTime.now());
        created.setUpdatedAt(LocalDateTime.now());
        walletAccountMapper.insert(created);

        WalletAccount account2 = walletAccountMapper.selectOne(
                Wrappers.<WalletAccount>lambdaQuery()
                        .eq(WalletAccount::getUserId, userId)
                        .eq(WalletAccount::getAssetCode, assetCode)
                        .last("FOR UPDATE"));
        if (account2 == null) {
            throw new BizException("钱包账户创建失败: userId=" + userId + ", asset=" + assetCode);
        }
        return account2;
    }

    private void updateAvailableBalance(WalletAccount account, BigDecimal newBalance) {
        account.setAvailableBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());
        walletAccountMapper.updateById(account);
    }

    private void updateBalance(WalletAccount account, BigDecimal newAvailable, BigDecimal newFrozen) {
        account.setAvailableBalance(newAvailable);
        account.setFrozenBalance(newFrozen);
        account.setUpdatedAt(LocalDateTime.now());
        walletAccountMapper.updateById(account);
    }

    private void writeLedger(Long userId, AssetCode assetCode, BigDecimal changeAmount,
                             BigDecimal balanceAfter, WalletBizType bizType, String bizNo, String remark) {
        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(userId);
        ledger.setAssetCode(assetCode);
        ledger.setChangeAmount(changeAmount);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setBizType(bizType);
        ledger.setBizNo(bizNo);
        ledger.setRemark(remark);
        ledger.setCreatedAt(LocalDateTime.now());
        walletLedgerMapper.insert(ledger);
    }
}

