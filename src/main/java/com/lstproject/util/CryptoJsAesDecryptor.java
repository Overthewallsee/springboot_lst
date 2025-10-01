package com.lstproject.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * CryptoJS AES解密工具类
 * 用于解密前端CryptoJS.AES.encrypt(data, key)加密的数据
 */
public class CryptoJsAesDecryptor {

    public static void main(String[] args) {
        String lst_login_secret_key_2024 = decrypt("U2FsdGVkX1+amyxirzuR+bGp89itaKJbirfWtHUAon0=", "LST_LOGIN_SECRET_KEY_2024");
        System.out.println(lst_login_secret_key_2024);
    }

    /**
     * 解密CryptoJS AES加密的数据
     *
     * @param encryptedData 前端传来的加密数据（Base64格式）
     * @param secretKey     前端使用的密钥
     * @return 解密后的明文
     */
    public static String decrypt(String encryptedData, String secretKey) {
        try {
            // 解析CryptoJS加密后的数据格式
            String[] parts = encryptedData.split("--");
            if (parts.length != 2) {
                // 如果没有使用自定义分隔符，则按标准CryptoJS格式处理
                return decryptStandardFormat(encryptedData, secretKey);
            }

            String ciphertext = parts[0];
            String iv = parts[1];

            return decryptWithIv(ciphertext, secretKey, iv);
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密标准CryptoJS格式的AES加密数据
     *
     * @param encryptedData Base64编码的加密数据
     * @param secretKey     密钥
     * @return 解密后的明文
     */
    private static String decryptStandardFormat(String encryptedData, String secretKey) {
        try {
            // CryptoJS加密的数据是Salted__开头的格式
            byte[] cipherData = Base64.getDecoder().decode(encryptedData);

            if (cipherData.length < 16 || !new String(cipherData, 0, 8, StandardCharsets.US_ASCII).equals("Salted__")) {
                throw new RuntimeException("无效的CryptoJS加密数据格式");
            }

            // 提取salt（8字节）
            byte[] salt = new byte[8];
            System.arraycopy(cipherData, 8, salt, 0, 8);

            // 提取实际密文
            byte[] ciphertext = new byte[cipherData.length - 16];
            System.arraycopy(cipherData, 16, ciphertext, 0, ciphertext.length);

            // 使用EVP_BytesToKey生成密钥和IV
            byte[][] keyAndIv = evpBytesToKey(secretKey.getBytes(StandardCharsets.UTF_8), salt, 32, 16);
            byte[] key = keyAndIv[0];
            byte[] iv = keyAndIv[1];

            // 执行解密
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密标准格式数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用IV解密数据
     *
     * @param ciphertext Base64编码的密文
     * @param secretKey  密钥
     * @param iv         Base64编码的初始化向量
     * @return 解密后的明文
     */
    private static String decryptWithIv(String ciphertext, String secretKey, String iv) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(ciphertext);
            byte[] keyBytes = deriveKey(secretKey, 32); // AES-256需要32字节密钥
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("使用IV解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 模拟OpenSSL的EVP_BytesToKey函数
     *
     * @param password 密码字节数组
     * @param salt     盐值
     * @param keyLen   所需密钥长度
     * @param ivLen    所需IV长度
     * @return 包含密钥和IV的字节数组
     */
    private static byte[][] evpBytesToKey(byte[] password, byte[] salt, int keyLen, int ivLen) {
        byte[] d = new byte[keyLen + ivLen];
        int nrounds = (keyLen + ivLen + 15) / 16;
        byte[] data = new byte[password.length + 8];

        System.arraycopy(password, 0, data, 0, password.length);
        System.arraycopy(salt, 0, data, password.length, 8);

        byte[] md5 = md5(data);
        System.arraycopy(md5, 0, d, 0, 16);

        for (int i = 1; i < nrounds; i++) {
            byte[] input = new byte[md5.length + data.length];
            System.arraycopy(md5, 0, input, 0, md5.length);
            System.arraycopy(data, 0, input, md5.length, data.length);

            md5 = md5(input);
            System.arraycopy(md5, 0, d, i * 16, Math.min(16, d.length - i * 16));
        }

        byte[][] result = new byte[2][];
        result[0] = new byte[keyLen]; // Key
        result[1] = new byte[ivLen];  // IV

        System.arraycopy(d, 0, result[0], 0, keyLen);
        System.arraycopy(d, keyLen, result[1], 0, ivLen);

        return result;
    }

    /**
     * MD5哈希函数
     *
     * @param data 输入数据
     * @return MD5哈希结果
     */
    private static byte[] md5(byte[] data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            return md.digest(data);
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }

    /**
     * 从密钥派生固定长度的密钥
     *
     * @param secretKey 原始密钥
     * @param length    所需长度
     * @return 派生的密钥
     */
    private static byte[] deriveKey(String secretKey, int length) {
        byte[] keyBytes = new byte[length];
        byte[] secretBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, length));
        return keyBytes;
    }
}
