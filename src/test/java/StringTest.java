import java.util.Arrays;

public class StringTest {
    public static void main(String[] args) {
        String url = "http://localhost:8880/baiduclick?reqid=%%ID%%&price=%%PRICE%%&ext_data=%%EXT_DATA%%&lpd=h55_shuzilm-cn&clk=admaster_click?shuzijz-0408";
        if ( url.indexOf("lpd")>0){
            System.out.println("--------------");
        }
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
                }

            }

        }

    }
}
