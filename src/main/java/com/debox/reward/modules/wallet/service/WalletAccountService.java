package com.debox.reward.modules.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.wallet.entity.WalletAccount;

import java.util.List;

public interface WalletAccountService extends IService<WalletAccount> {

    void createDefaultAccounts(Long userId);

    List<WalletAccount> listByUserId(Long userId);
}
