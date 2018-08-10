package cn.shuzilm.util.aes;

public class Bytes {
    public static final char[] HEXCHARS = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    
    public static String asHex(byte[] bytes) {
        char chars[] = new char[bytes.length * 2];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = Bytes.HEXCHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = Bytes.HEXCHARS[b & 0xf];
        }
        return new String(chars);
    }
}
