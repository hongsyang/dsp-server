package cn.shuzilm.youyi;

import cn.shuzilm.util.base64.AdViewDecodeUtil;

public class YouYiTest {
    public static void main(String[] args) {
        Long priceDecode = AdViewDecodeUtil.priceDecode("W4T7SgAB4kAAAAAAAAAAACtp9fz1cyuylmD0qQ", "0dagsdW3RB2W8RykDkd7hJ3m2G0mifXg", "ponxZFQT8fmWiQGYkiqJ9HuvJfW8jPHK");
        System.out.println(priceDecode/100000);
    }
}
