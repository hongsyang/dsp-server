package cn.shuzilm.util.baidudecode;

import cn.shuzilm.util.base64.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class BaiduDecrypter {

    private static int INITIALIZATION_VECTOR_SIZE = 16;
    private static int CIPHER_TEXT_SIZE = 8;
    private static  int SIGNATURE_SIZE = 4;
    private static int ENCRYPTED_VALUE_SIZE =
            INITIALIZATION_VECTOR_SIZE + CIPHER_TEXT_SIZE + SIGNATURE_SIZE;
    private  static int HASH_OUTPUT_SIZE = 20;
    

    public static String decrypt(String b64EncodedCiphertext, String ekey, String ekey1) throws UnsupportedEncodingException {
        byte[] initialization_vector = Base64.decodeBase64(b64EncodedCiphertext.getBytes("US-ASCII"));
        //len(ciphertext_bytes) = 8 bytes
        byte[] ciphertext_bytes = Arrays.copyOf(initialization_vector, INITIALIZATION_VECTOR_SIZE);
        //signatrue = initialization_vector + INITIALIZATION_VECTOR_SIZE(16) + CIPHER_TEXT_SIZE(8)
        byte[] signature = Arrays.copyOf(ciphertext_bytes, CIPHER_TEXT_SIZE);

        int pad_size = HASH_OUTPUT_SIZE;
        int[] price_pad = new int[HASH_OUTPUT_SIZE];

        //get price_pad using openssl/hmac.h
//        if (!HMAC(EVP_sha1(), encryption_key.data(), encryption_key.length(),
//                initialization_vector, INITIALIZATION_VECTOR_SIZE, price_pad,
//                &pad_size)) {
//            return false;
//        }

        int[] plaintext_bytes = new int[CIPHER_TEXT_SIZE];

        for (int i = 0; i < CIPHER_TEXT_SIZE; ++i) {
            plaintext_bytes[i] = price_pad[i] ^ ciphertext_bytes[i];
        }

        for (int i = 0; i < CIPHER_TEXT_SIZE; ++i) {
            System.out.println(Arrays.toString(ciphertext_bytes) );

            System.out.println( new String(ciphertext_bytes,0,8) ) ;

        }
        System.out.println(Arrays.toString(plaintext_bytes) );
        System.out.println(Arrays.toString(ciphertext_bytes) );
        System.out.println(Arrays.toString(price_pad) );


        return null;
    }
}
