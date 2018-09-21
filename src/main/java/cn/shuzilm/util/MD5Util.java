package cn.shuzilm.util;

import java.security.MessageDigest;

public class MD5Util {

    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.trim().getBytes("utf-8"));
            return toHex(bytes);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    public static void main(String[] args) {
//        System.out.println("品牌：oppo r7007;"+"操作系统：Android;IMEI:"+MD5("866089025831526"));
//        System.out.println("品牌：山寨苹果;"+"操作系统：Android;IMEI"+MD5("350998832332580"));
//        System.out.println("品牌：Lenovo K30;"+"操作系统：Android;IMEI"+MD5("867792029921198"));
//        System.out.println("品牌：小米NOTE4;"+"操作系统：Android;IMEI"+MD5("861206037426724"));
//        System.out.println("品牌：红米2A;"+"操作系统：Android;IMEI"+MD5("868942026149793"));
//        System.out.println("品牌：中兴远航3;"+"操作系统：Android;IMEI"+MD5("868153022515488"));
//        System.out.println("品牌：小辣椒;"+"操作系统：Android;IMEI"+MD5("867338031004605"));
//        System.out.println("品牌：魅族;"+"操作系统：Android;IMEI"+MD5("867900916074530"));
//        System.out.println("品牌：oppo r7007;"+"操作系统：Android;"+MD5("866089025831526"));
//        System.out.println("品牌：oppo r7007;"+"操作系统：Android;"+MD5("866089025831526"));
        System.out.println("token;"+MD5("houkp@shuzilm.cncwxk9c2220wkpjvvughbrypay9k1x50o"));
    }
}

