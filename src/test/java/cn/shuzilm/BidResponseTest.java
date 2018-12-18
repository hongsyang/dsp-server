package cn.shuzilm;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import com.alibaba.fastjson.JSON;

public class BidResponseTest {

    public static void main(String[] args) {
        String responseTest = "{\n" +
                "\"bidid\": \"7c9857f5191071bd4be6342ce3e3bff9\",\n" +
                "\"id\": \"y068_8081-t20-1542124881-57-411\",\n" +
                "\"seatbid\": [{\n" +
                "    \"bid\": [{\n" +
                "        \"adm\": \"http://cdn.shuzijz.cn/material/c57022be-eb43-4e78-81d2-86c88f3af5a2.jpg\",\n" +
                "        \"crid\": \"582379037959\",\n" +
                "        \"ext\": {\n" +
                "            \"cm\": [\"http://pixel.shuzijz.cn/lingjiclick?id=y068_8081-t20-1542124881-57-411&bidid=7c9857f5191071bd4be6342ce3e3bff9&impid=385e3df7534f4910b5fee568a8fb9567&act=20181114000120&adx=1&did=6c07616aad9fb6568c73d7193a2b4b03&device=6c07616aad9fb6568c73d7193a2b4b03&app=Drivers' License - Android&appn=com.jxedt&appv=null&ddem=281d4991-fac8-4969-ba6b-233e35a43d4a&dcuid=477cb3e4-d6fa-4616-b9c3-752620d557f8&dpro=20&dcit=243&dcou=2268&dade=89633fef-b7d0-4a36-802d-8960ffe5e851&dage=null&daduid=a2d3e66f-ddd6-4ff2-92d4-e88cbdd3f4a9&pmp=null\", \"http://re.shuzilm.cn/1ytmls3v?ip=${AUCTION_ID}&idfa=${AUCTION_BID_ID}&imei=${AUCTION_IMP_ID}&os=${AUCTION_PRICE}&mac=${MAC}&mac1=${mac1}&ua=%%WIN_PRICE%%\"],\n" +
                "            \"ldp\": \"https://www.chengzijianzhan.com/tetris/page/1608029410456584/?advertiserUid=89633fef-b7d0-4a36-802d-8960ffe5e851&adUid=a2d3e66f-ddd6-4ff2-92d4-e88cbdd3f4a9&creativeUid=477cb3e4-d6fa-4616-b9c3-752620d557f8&materialId=c57022be-eb43-4e78-81d2-86c88f3af5a2\",\n" +
                "            \"pm\": [\"http://re.shuzilm.cn/1ytmlrxq?ip=${AUCTION_ID}&idfa=${AUCTION_BID_ID}&imei=${AUCTION_IMP_ID}&os=${AUCTION_PRICE}&mac=${MAC}&mac1=${mac1}&ua=%%WIN_PRICE%%\"]\n" +
                "        },\n" +
                "        \"id\": \"7c9857f5191071bd4be6342ce3e3bff9\",\n" +
                "        \"impid\": \"385e3df7534f4910b5fee568a8fb9567\",\n" +
                "        \"nurl\": \"http://pixel.shuzijz.cn/lingjiexp?id=${AUCTION_ID}&bidid=${AUCTION_BID_ID}&impid=${AUCTION_IMP_ID}&price=${AUCTION_PRICE}&act=20181114000120&adx=1&did=6c07616aad9fb6568c73d7193a2b4b03&device=6c07616aad9fb6568c73d7193a2b4b03&app=Drivers' License - Android&appn=com.jxedt&appv=null&pf=0.5&ddem=281d4991-fac8-4969-ba6b-233e35a43d4a&dcuid=477cb3e4-d6fa-4616-b9c3-752620d557f8&dpro=20&dcit=243&dcou=2268&dade=89633fef-b7d0-4a36-802d-8960ffe5e851&dage=null&daduid=a2d3e66f-ddd6-4ff2-92d4-e88cbdd3f4a9&pmp=null\",\n" +
                "        \"price\": 2700.0\n" +
                "    }]\n" +
                "}]\n" +
                "}";
        BidResponseBean bidResponseBean = JSON.parseObject(responseTest, BidResponseBean.class);
        System.out.println("bidResponseBean:" + bidResponseBean);
        String response = "空请求";
        response = JSON.toJSONString(bidResponseBean);
        System.out.println("response:" + response);

        boolean contains = response.contains("http://pixel.shuzijz.cn/lingjiclick?");
        System.out.println(contains);
        response.indexOf("http://pixel.shuzijz.cn/lingjiclick?");
        String substring = response.substring(response.indexOf("http://pixel.shuzijz.cn/lingjiclick?"));
        System.out.println(substring.substring(0, substring.indexOf('"')));

    }
}
