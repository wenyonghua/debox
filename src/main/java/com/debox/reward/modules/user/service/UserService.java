package com.debox.reward.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.user.dto.RegisterRequest;
import com.debox.reward.modules.user.entity.User;

public interface UserService extends IService<User> {

    User register(RegisterRequest request);

    /** 绑定邀请人（仅能绑定一次） */
    void bindReferrer(Long userId, String inviteCode);

    /**
     * 钱包登录：按链上地址查找；不存在则建档并初始化钱包账户。
     *
     * @param normalizedWalletAddress {@link com.debox.reward.modules.auth.eth.EthereumPersonalSignVerifier#normalizeAddress}
     */
    User findOrCreateForWalletLogin(String normalizedWalletAddress);
}
