package com.debox.reward.modules.wallet.util;

import java.security.MessageDigest;

/**
 * 地址格式校验（不做链上存在性验证）
 */
public final class ChainAddressValidator {

    private ChainAddressValidator() {
    }

    /** TRON 主网地址（Base58Check），通常以 T 开头 */
    public static boolean isValidTronBase58Address(String address) {
        if (address == null) {
            return false;
        }
        String s = address.trim();
        if (s.length() < 26 || s.length() > 36) {
            return false;
        }
        if (!s.startsWith("T")) {
            return false;
        }
        byte[] decoded = base58Decode(s);
        if (decoded == null || decoded.length < 5) {
            return false;
        }
        if (decoded.length != 25) {
            // TRON 常见长度 25 bytes（21 payload + 4 checksum）
            return false;
        }
        byte[] payload = new byte[21];
        byte[] checksum = new byte[4];
        System.arraycopy(decoded, 0, payload, 0, 21);
        System.arraycopy(decoded, 21, checksum, 0, 4);
        byte[] checksum2 = sha256d(payload);
        if (checksum2 == null || checksum2.length < 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if (checksum[i] != checksum2[i]) {
                return false;
            }
        }
        // TRON 主网 version byte = 0x41
        return (payload[0] & 0xFF) == 0x41;
    }

    private static byte[] sha256d(byte[] input) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            byte[] first = d.digest(input);
            return d.digest(first);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Base58 解码（最小实现，适用于 Base58Check 校验）。
     * 返回原始字节数组；失败返回 null。
     */
    private static byte[] base58Decode(String input) {
        // Bitcoin alphabet
        final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        int[] indexes = new int[128];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = -1;
        }
        for (int i = 0; i < ALPHABET.length(); i++) {
            indexes[ALPHABET.charAt(i)] = i;
        }

        byte[] input58 = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 128 || indexes[c] == -1) {
                return null;
            }
            input58[i] = (byte) indexes[c];
        }

        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            zeros++;
        }

        byte[] decoded = new byte[input.length()];
        int outputStart = decoded.length;
        int inputStart = zeros;
        while (inputStart < input58.length) {
            int mod = divmod58(input58, inputStart);
            if (input58[inputStart] == 0) {
                inputStart++;
            }
            decoded[--outputStart] = (byte) mod;
        }

        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            outputStart++;
        }

        byte[] out = new byte[decoded.length - (outputStart - zeros)];
        System.arraycopy(decoded, outputStart - zeros, out, 0, out.length);
        return out;
    }

    private static int divmod58(byte[] number58, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number58.length; i++) {
            int digit256 = number58[i] & 0xFF;
            int temp = remainder * 58 + digit256;
            number58[i] = (byte) (temp / 256);
            remainder = temp % 256;
        }
        return remainder;
    }
}

