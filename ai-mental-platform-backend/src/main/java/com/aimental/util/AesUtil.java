package com.aimental.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-256加密解密工具类
 */
@Component
public class AesUtil {

    private static final Logger log = LoggerFactory.getLogger(AesUtil.class);

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密算法/加密模式/填充方式
     */
    private static final String ALGORITHM_NAME = "AES/ECB/PKCS5Padding";

    /**
     * 密钥长度（字节）
     */
    private static final int KEY_LENGTH = 32;

    /**
     * AES密钥
     */
    @Value("${aes.secret-key}")
    private String secretKey;

    /**
     * 缓存的密钥规范对象（延迟初始化）
     */
    private SecretKeySpec keySpec;

    /**
     * 加密字符串
     *
     * @param content 待加密内容
     * @return 加密后的Base64字符串
     */
    public String encrypt(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec());
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败: {}", e.getMessage());
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedContent 加密后的Base64字符串
     * @return 解密后的原始字符串
     */
    public String decrypt(String encryptedContent) {
        if (encryptedContent == null || encryptedContent.isEmpty()) {
            return encryptedContent;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec());
            byte[] decoded = Base64.getDecoder().decode(encryptedContent);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败: {}", e.getMessage());
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * 获取密钥规范（双重检查锁实现延迟初始化）
     *
     * @return SecretKeySpec
     */
    private SecretKeySpec getKeySpec() {
        if (keySpec == null) {
            synchronized (this) {
                if (keySpec == null) {
                    byte[] keyBytes = new byte[KEY_LENGTH];
                    byte[] tempBytes = secretKey.getBytes(StandardCharsets.UTF_8);
                    int length = Math.min(tempBytes.length, KEY_LENGTH);
                    System.arraycopy(tempBytes, 0, keyBytes, 0, length);
                    keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
                }
            }
        }
        return keySpec;
    }
}
