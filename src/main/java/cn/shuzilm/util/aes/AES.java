package cn.shuzilm.util.aes;

import com.google.common.io.BaseEncoding;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Simple implementation of AES protocol
 * - "AES"
 * - No KeyGenerator
 * - No IV 
 * - "ECB" instead of "CBC"
 */
public class AES {

    private static final String ALGORITHM = "AES";

    /**
     * 加密
     * @param s 需要加密的字符串
     * @param token 加密token，16进制的hex格式，必须是32长度的字符串。如果token不合法，返回null
     * @return 加密后的字符串
     */
    public static String encrypt(String s, String token) {
        try {
            byte[] key = Hex.toBytes(token);
            return encryptBase64URLSafe(s, key);
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     * @param encrypted 已加密的字符串
     * @param token 加密token，16进制的hex格式，必须是32长度的字符串。如果token不合法，返回空串
     * @return 解密后的字符串
     */
    public static String decrypt(String encrypted, String token){
        try {
            byte[] key = Hex.toBytes(token);
            return decryptBase64URLSafe(encrypted, key);
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    private static byte[] encryptBytes(String s, byte[] key) {
        if (key == null) {
            return null;
        }
        // 判断Key是否为16位
        if (key.length != 16) {
            return null;
        }
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(s.getBytes("utf-8"));
            return encrypted;
        } catch (Exception e) {
            return null;
        }
    }

    private static String decryptBytes(byte[] encrypted, byte[] key) {
        try {
            // 判断Key是否正确
            if (key == null) {
                return null;
            }
            // 判断Key是否为16位
            if (key.length != 16) {
                return null;
            }
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            try {
                byte[] original = cipher.doFinal(encrypted);
                String originalString = new String(original, "utf-8");
                return originalString;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private static String encryptBase64URLSafe(String s, byte[] key) {
        BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
        byte[] encryptedBytes = encryptBytes(s, key);
        return b64.encode(encryptedBytes);
    }

    private static String decryptBase64URLSafe(String encrypted, byte[] key) {
        BaseEncoding b64 = BaseEncoding.base64Url().omitPadding();
        byte[] encryptedBytes = b64.decode(encrypted);
        String decrypted = decryptBytes(encryptedBytes,key);
        return decrypted;
    }
}
