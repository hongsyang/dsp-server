import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class ExpTest {
    public static void main(String[] args) {

        Map hashMap =new HashMap<>();
        hashMap.put("houkp","test");
        Object houkp = hashMap.get("houkp");
        System.out.println(houkp);
//        String json = "{\"actualPrice\":0.02,\"actualPricePremium\":0.02857,\"adTypeId\":\"banner\",\"adUid\":\"7635434a-7ff2-45f6-9806-09b6d4908e2e\",\"adct\":0,\"adh\":50,\"adm\":\"http://dp.test.zhiheworld.com/m/qsbk_320x50.gif\",\"admt\":\"pic\",\"advertiserUid\":\"5ee19403-2bc8-4886-864d-b6ef139e26cb\",\"adw\":320,\"adxAdTypeId\":14,\"adxId\":\"1\",\"adxSource\":\"LingJi\",\"agencyProfit\":0.0,\"appName\":\"????\",\"appPackageName\":\"com.chuanqi.LOLBox\",\"biddingPrice\":13.0,\"bidid\":\"2018-08-31T17:13:56.418b0164b44-74de-4716-882c-db6f3d0bfd27\",\"city\":\"62\",\"country\":\"737\",\"createTime\":0,\"creativeUid\":\"56fde193-de65-4c57-a92d-674ac431e0db\",\"crid\":\"56fde193-de65-4c57-a92d-674ac431e0db\",\"demographicTagId\":\"2,3\",\"desc\":\"SDFSAFSDF\",\"descLong\":\"\",\"deviceId\":\"97C304E-4C8E-4872-8666-03FE67DC15DF\",\"did\":\"97C304E-4C8E-4872-8666-03FE67DC15DF\",\"dspid\":\"2018-08-31T17:13:56.41865b5bebe-a863-4abb-b491-14a1b06e4655\",\"hour\":17,\"impression\":[{\"banner\":{\"h\":50,\"mimes\":[\"image/jpeg\",\"image/png\",\"application/x-shockwave-flash\",\"video/x-flv\",\"application/x-shockwave-flash\",\"text/html\",\"image/gif\"],\"pos\":0,\"w\":320},\"bidfloor\":1,\"ext\":{\"action_type\":1,\"has_clickthrough\":0,\"has_winnotice\":1,\"showtype\":14},\"id\":\"4a7f9a1000044101913a8fd0b19ba440\",\"secure\":0,\"tagid\":\"669\"}],\"infoId\":\"y053-test-t24-1535706837-0-453f3cc05f3-2533-44f4-a565-81bf70294ad4\",\"landingUrl\":\"https://www.shuzilm.cn/\",\"linkUrl\":\"http://www.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssssww.qsbk.com/ssss.html\",\"mode\":\"cpm\",\"ourProfit\":0.00857,\"platform\":\"android\",\"premiumFactor\":0.3,\"province\":\"6\",\"requestId\":\"y053-test-t24-1535706837-0-453\",\"title\":\"DFQEREQT\",\"titleLong\":\"\",\"tracking\":\"https://www.shuzilm.cn/\",\"widthHeightRatio\":\"32/5\",\"winNoticeTime\":1535706837779}\n";
//        DUFlowBean element = JSON.parseObject(json, DUFlowBean.class);//json转换为对象
//        String infoId = element.getInfoId();
//        for (int i = 0; i < 10000; i++) {
//            element.setInfoId(infoId + i);
//            boolean lingJiExp = JedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
//            System.out.println(lingJiExp + ":" + i);
//        }
//        System.out.println(JedisQueueManager.getAllElement("EXP").size());
    }
}