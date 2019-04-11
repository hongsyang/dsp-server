import cn.shuzilm.util.HttpClientUtil;

import java.util.Arrays;

public class StringTest {
    public static void main(String[] args) {
        String url = "http://pixel.shuzijz.cn/baiduclick?reqid=db6278d94f06a8b1&price=PRICE&ext_data=db6278d94f06a8b1&bidid=201904111430244724bfb7172-96ac-4574-a7&act=20190411143024472&device=f9ea2ae3641b7e029fbb1a57e456e832&appn=com.xunlei.downloadprovider&pf=1.0&ddem=ff905c5a-7f0b-4fbb-80d2-d63c3e863a69&dcuid=0037ba70-0180-406d-8892-ce56575e7bc0&dbidp=2.0&dade=6e2e9a01-8aa9-4882-8c1d-464ee5f7163b&dage=7230b157-5980-4b42-8028-b9a2bfd64bb9&daduid=479ad7a5-e9ee-4680-ab0d-0d5d13b38006&dmat=bd47c47c-fb90-4231-89f3-f7ab995a9f86&dpro=2&dcit=36&dcou=381&userip=10.58.31.21&lpd=h55_shuzilianmeng_cn&clk=pixel_shuzijz_cn-baiduclick?reqid=db6278d94f06a8b1&price=PRICE&ext_data=db6278d94f06a8b1&bidid=201904111430244724bfb7172-96ac-4574-a7&act=20190411143024472&device=f9ea2ae3641b7e029fbb1a57e456e832&appn=com.xunlei.downloadprovider&pf=1.0&ddem=ff905c5a-7f0b-4fbb-80d2-d63c3e863a69&dcuid=0037ba70-0180-406d-8892-ce56575e7bc0&dbidp=2.0&dade=6e2e9a01-8aa9-4882-8c1d-464ee5f7163b&dage=7230b157-5980-4b42-8028-b9a2bfd64bb9&daduid=479ad7a5-e9ee-4680-ab0d-0d5d13b38006&dmat=bd47c47c-fb90-4231-89f3-f7ab995a9f86&dpro=2&dcit=36&dcou=381&userip=10.58.31.21";
        if ( url.indexOf("lpd")>0){
            System.out.println("--------------");
            String lpdAndClk = url.substring(url.indexOf("lpd") + 4);
            String http = "http://";
            if (lpdAndClk != null) {
                String[] split = lpdAndClk.split("&clk=");
                if (split != null) {
                    String lpd = split[0];
                    if (lpd != null || "null".equals(lpd)) {
                        String[] split1 = lpd.split("\\?");
                        String replace = split1[0].replace("_", ".").replace("-", "/");
                        if (split1.length > 1) {
                            replace = replace +"?"+ split1[1];
                        }
                        String htppLpdUrl = http + replace;
                        System.out.println(htppLpdUrl);
                    }
                    String clk = split[1];
                    if (clk != null || "null".equals(clk)) {
                        String[] clkSplit = clk.split("\\?");
                        String clkreplace = clkSplit[0].replace("_", ".").replace("-", "/");
                        if (clkSplit.length > 1) {
                            clkreplace = clkreplace +"?"+ clkSplit[1];
                        }
                        String htppClkUrl = http + clkreplace;
                        System.out.println(htppClkUrl);
                        HttpClientUtil.get(htppClkUrl);
                        System.out.println("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
                    }

                }

            }
        }else {
            System.out.println("==============================");
        }


    }
}
