package com.debox.reward.modules.auth.eth;

/**
 * 与前端 EIP-191 personal_sign 对齐的明文。修改时请同步前端 / 文档。
 */
public final class WalletLoginMessage {

    private WalletLoginMessage() {
    }

    public static String build(String nonce, String normalizedWalletAddress0xLower) {
        return "Debox 登录\n\nNonce: " + nonce + "\nAddress: " + normalizedWalletAddress0xLower;
    }
}
