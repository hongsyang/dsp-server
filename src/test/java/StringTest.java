import java.util.Arrays;

public class StringTest {
    public static void main(String[] args) {
        String url = "http://pixel.shuzijz.cn/baiduclick?reqid=9c0b2ce150fc7a03&price=%%PRICE%%&ext_data=9c0b2ce150fc7a03&bidid=2019041011285475518ed8ab3-ca4e-4db9-83&act=20190410112854755&device=f7863932088cf24cc0d602e4eb8ea753&appn=com.brianbaek.popstar&pf=1.0&ddem=ff905c5a-7f0b-4fbb-80d2-d63c3e863a69&dcuid=b3edd36b-6b47-44ff-a497-43267ab88a8b&dbidp=152.0&dade=6e2e9a01-8aa9-4882-8c1d-464ee5f7163b&dage=7230b157-5980-4b42-8028-b9a2bfd64bb9&daduid=6072b03c-aa33-494a-8692-e0c98503f79d&dmat=6072ac6a-3d3a-41c3-996e-cbb9ff3c3e27&userip=10.58.31.21";
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
                    }

                }

            }
        }else {
            System.out.println("==============================");
        }


    }
}
