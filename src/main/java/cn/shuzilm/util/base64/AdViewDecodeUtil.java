package cn.shuzilm.util.base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;

/**
 * @Description: AdViewDecodeUtil 快友解密工具类
 * @Author: houkp
 * @CreateDate: 2018/8/9 13:26
 * @UpdateUser: houkp
 * @UpdateDate: 2018/8/9 13:26
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AdViewDecodeUtil {

    public static Long priceDecode(String price, String ekey, String ikey) {
        String b64EncodedCiphertext = Decrypter.unWebSafeAndPad(price);
        try {
            byte[] codeString = Base64.decodeBase64(b64EncodedCiphertext.getBytes("US-ASCII"));
            SecretKey encryptionKey = new SecretKeySpec(ekey.getBytes(), "HmacSHA1");
            SecretKey integrityKey = new SecretKeySpec(ikey.getBytes(), "HmacSHA1");
            byte[] decrypt = Decrypter.decrypt(codeString, encryptionKey, integrityKey);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(decrypt));
            Long value = dis.readLong();
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
