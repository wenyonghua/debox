package com.debox.reward.modules.wallet.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.wallet.entity.WalletAccount;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.mapper.WalletAccountMapper;
import com.debox.reward.modules.wallet.service.WalletAccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class WalletAccountServiceImpl extends ServiceImpl<WalletAccountMapper, WalletAccount> implements WalletAccountService {

    @Override
    public void createDefaultAccounts(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Arrays.stream(AssetCode.values()).forEach(assetCode -> {
            WalletAccount account = new WalletAccount();
            account.setUserId(userId);
            account.setAssetCode(assetCode);
            account.setAvailableBalance(BigDecimal.ZERO);
            account.setFrozenBalance(BigDecimal.ZERO);
            account.setStatus(1);
            account.setCreatedAt(now);
            account.setUpdatedAt(now);
            save(account);
        });
    }

    @Override
    public List<WalletAccount> listByUserId(Long userId) {
        return list(Wrappers.<WalletAccount>lambdaQuery().eq(WalletAccount::getUserId, userId));
    }
}
