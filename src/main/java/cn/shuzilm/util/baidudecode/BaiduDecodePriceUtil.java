package cn.shuzilm.util.baidudecode;

import cn.shuzilm.util.base64.Base64;
import cn.shuzilm.util.base64.Decrypter;

import java.util.Arrays;


public class BaiduDecodePriceUtil {

    public static String priceDecode(String price, String ekey, String ikey) {
        String b64EncodedCiphertext = Decrypter.unWebSafeAndPad(price);
        try {

//            String priceDecode =  BaiduDecrypter.decrypt(b64EncodedCiphertext, ekey, ekey);

            String ciphertext_bytes = "0d1fe39e573488cf";
            String price_pad =  "0d1fe39e573488ee595acd5c6d4ce0f445476794";
            int[] test =new int[16];
            byte[] ciphertext_bytesa =  ciphertext_bytes.getBytes("US-ASCII");
            System.out.println(Arrays.toString(ciphertext_bytesa));
            byte[] price_padbytes = price_pad.getBytes("US-ASCII");
            System.out.println(Arrays.toString(price_padbytes));
            for (int i = 0; i < ciphertext_bytes.length(); i++) {
                test[i] = ciphertext_bytesa[i] ^ price_padbytes[i];
                
            }
            int i =99;
            int j=101;
            System.out.println( i^j);
            System.out.println(Arrays.toString(test));
            return "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String  price = "Uja0xQADFz97jEpgW5IA8g0f455XNIjPRj8IqA";
        String s = priceDecode(price, "", "");
        
    }
}
