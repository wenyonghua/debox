package com.debox.reward.modules.auth.eth;

import org.bouncycastle.util.Arrays;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;

/**
 * EIP-191 personal_sign（web3.eth.personal.sign 默认语义）
 */
public final class EthereumPersonalSignVerifier {

    private EthereumPersonalSignVerifier() {
    }

    public static String normalizeAddress(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        String h = Numeric.cleanHexPrefix(address.trim()).toLowerCase();
        return "0x" + h;
    }

    /**
     * @param signingMessageUtf8 用户签名的明文（须与前端一致）
     * @param signatureHexRaw    0x 开头 132 hex（65字节 r+s+v）
     */
    public static String recoverSignMessageAddress(String signingMessageUtf8, String signatureHexRaw) {
        byte[] sig = Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(signatureHexRaw.trim()));
        if (sig.length != 65) {
            throw new IllegalArgumentException("signature length must be 65 bytes got " + sig.length);
        }
        byte[] r = Arrays.copyOfRange(sig, 0, 32);
        byte[] s = Arrays.copyOfRange(sig, 32, 64);
        byte v = sig[64];
        if (v < 27) {
            v += 27;
        }
        Sign.SignatureData sd = new Sign.SignatureData(v, r, s);
        byte[] messageBytes = signingMessageUtf8.getBytes(StandardCharsets.UTF_8);
        final BigInteger pubKey;
        try {
            pubKey = Sign.signedPrefixedMessageToKey(messageBytes, sd);
        } catch (SignatureException e) {
            throw new IllegalArgumentException("invalid signature", e);
        }
        return normalizeAddress(Numeric.prependHexPrefix(Keys.getAddress(pubKey)));
    }

    /** 恢复的地址必须与声明地址等价（checksum 不影响） */
    public static boolean isValidSigner(String signingMessageUtf8, String signatureHexRaw, String claimedAddress) {
        String recovered = recoverSignMessageAddress(signingMessageUtf8, signatureHexRaw);
        return recovered.equals(normalizeAddress(claimedAddress));
    }

    public static boolean looksLikeEvmAddress(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        String h = Numeric.cleanHexPrefix(address.trim()).toLowerCase();
        if (h.length() != 40) {
            return false;
        }
        for (int i = 0; i < h.length(); i++) {
            char c = h.charAt(i);
            boolean hex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
            if (!hex) {
                return false;
            }
        }
        return true;
    }
}
