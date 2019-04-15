package cn.shuzilm.util.baidudecode;


import cn.shuzilm.util.base64.Decrypter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by thunders on 2019/4/4.
 */
public class BaiduPriceDecryptUtil {
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static char check_key[]  = {
            0x00, 0x5e, 0x38, 0xcb, 0x01, 0xab, 0xc2, 0x2f,
            0xd5, 0xad, 0x6b, 0xe0, 0x01, 0xab, 0xc2, 0x2f,
            0xd5, 0xad, 0x73, 0xb0, 0x01, 0xab, 0xc2, 0x2f,
            0xd5, 0xad, 0x7b, 0x80, 0x2e, 0x88, 0xc5, 0x32
    };
//测试解密秘钥:    005e38cb01abc22fd5acd77001abc22fd5acfe8001abc22fd5ad06509b1bb875
//    private static String ENCRYPT_KEY = "005e38cb01abc22fd5acd77001abc22fd5acfe8001abc22fd5ad06509b1bb875";
    //联调环境 解密秘钥
//    private static String ENCRYPT_KEY = "01a4949700605b727c11c8e000605b727c11d6bc00605b727c11d92bf923d2bf";
    //正式环境 解密秘钥  参考地址：http://buyer.bes.baidu.com/static/main.html#/setting/dsprtb/update
    private static String ENCRYPT_KEY = "01a71a35000d15b4654736e1000d15b465479d03000d15b46547aa077ee81a78";

//    private static char encryption_key[] = {
//            0x00, 0x5e, 0x38, 0xcb, 0x01, 0xab, 0xc2, 0x2f,
//            0xd5, 0xac, 0xd7, 0x70, 0x01, 0xab, 0xc2, 0x2f,
//            0xd5, 0xac, 0xfe, 0x80, 0x01, 0xab, 0xc2, 0x2f,
//            0xd5, 0xad, 0x06, 0x50, 0x9b, 0x1b, 0xb8, 0x75
//    };

    /**
     * 大端到小端的转换
     *JAVA 默认是大端字节序 ，即顺序排列，按照内存地址编号字节码依次提高：
     *常用的字节序
     Little endian：将低序字节存储在起始地址。例如一个4字节的值为0x1234567的整数与高低字节对应关系:
     01	23	45	67
     Byte3	Byte2	Byte1	Byte0
     将在内存中按照如下顺序排放：
     内存地址序号	字节在内存中的地址	16进制值
     0x03	Byte3	01
     0x02	Byte2	23
     0x01	Byte1	45
     0x00	Byte0	67

     Big endian：将高序字节存储在起始地址。例如一个4字节的值为0x1234567的整数与高低字节对应关系:
     01	23	45	67
     Byte3	Byte2	Byte1	Byte0
     将在内存中按照如下顺序排放：
     内存地址序号	字节在内存中的地址	16进制值
     0x03	Byte0	67
     0x02	Byte1	45
     0x01	Byte2	23
     0x00	Byte3	01
     * @param a
     * @return
     */
    public static int toLittleEndian(int a) {
        return (((a & 0xFF) << 24) | (((a >> 8) & 0xFF) << 16) | (((a >> 16) & 0xFF) << 8) | ((a >> 24) & 0xFF));

    }

//    public static String convert(char[] src){
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < src.length; i++) {
//            String a = toStringHex1(src[i]);
//            sb.append(a);
//        }
//        return sb.toString();
//    }
    public static void main(String[] args) {
        BaiduPriceDecryptUtil b = new BaiduPriceDecryptUtil();
//        String encryptStr = "Uja0xQADFz97jEpgW5IA8g0f455XNIjPRj8IqA";
        String encryptStr = "XKqufAACrvN7jEpgW5IA8v3a5_BT14VfQwoGYA";
        String unWebSafeAndPad = Decrypter.unWebSafeAndPad(encryptStr);
        int price = b.decodePrice(unWebSafeAndPad);
        System.out.println("最终价格:" + price);
    }
    /**
     *
     * 解密过程(以下参数均为十六进制表示):
     1、Base64Decode(Uja0xQADFz97jEpgW5IA8g0f455XNIjPRj8IqA)，结果为:5236b4c50003173f7b8c4a605b9200f2   0d1fe39e573488cf   463f08a8
     2、初始化向量iv  = 5236b4c50003173f7b8c4a605b9200f2（前）
     加密价格  p   = 0d1fe39e573488cf
     校验部分  sig = 463f08a8
     3、price_pad = hmac(解密秘钥, iv) = 0d1fe39e573488ee595acd5c6d4ce0f445476794
     4、price = p ^ price_pad = 0d1fe39e573488cf ^ 0d1fe39e573488ee595acd5c6d4ce0f445476794 = 0000000000000021
     5、conf_sig  = hmac(校验秘钥, price || iv) = 8dc4c36eb866849e4672747991c98b9445e0cda0
     6、memcmp(conf_sig, sig, 4)结果不等于0，表示校验失败

     *
     * @return
     */
    public static int decodePrice(String encryptStr){
        String unWebSafeAndPad = Decrypter.unWebSafeAndPad(encryptStr);
        byte[] decryptArr = Base64Simple.decode(unWebSafeAndPad );
        System.out.println("base64 :" + bytesToHexFun2(decryptArr));
        // 解密
        byte[] encryptP = new byte[28];
        byte[] initByteArray = new byte[16];
//        arraycopy(Object src,  int  srcPos,
//        Object dest, int destPos,
//        int length);
//
        System.arraycopy(decryptArr, 16, encryptP, 0,8 );
        System.arraycopy(decryptArr,0,initByteArray,0,16);

        System.out.println("加密价格:" + bytesToHexFun2(encryptP));
        System.out.println("初始化向量:" + bytesToHexFun2(initByteArray));
        byte[] encryptKeyArray = hexToByteArray(ENCRYPT_KEY);
        byte[] pricePad =  decodeWithHMAC(encryptKeyArray,initByteArray);
        System.out.println("price pad: " + bytesToHexFun2(pricePad));
        byte[] price = new byte[pricePad.length];
        for (int i = 0; i < 8; i++) {
            price[i] = (byte)(encryptP[i] ^ pricePad[i]);
        }
        System.out.println("final price:  " + bytesToHexFun2(price));
        String strPrice = bytesToHexFun2(price).substring(0,16);
        int nPrice = Integer.parseInt(strPrice,16);
        return nPrice;
    }

    public static byte[] hexToByteArray(String inHex){
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1){
            //奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {
            //偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2){
            result[j]=hexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }

    public static byte hexToByte(String inHex){
        return (byte)Integer.parseInt(inHex,16);
    }

    public static String bytesToHexFun2(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for(byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    public static  byte[] decodeWithHMAC(byte[] keyArray,byte[] encryptByteArray){
        try {
            byte[] priceByteArray = HMACSHA1.hmacSHA1Encrypt(encryptByteArray,keyArray);
            return priceByteArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
