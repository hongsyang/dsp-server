import bidserver.BidserverSsp;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.youyi.response.YouYiAd;
import cn.shuzilm.bean.youyi.response.YouYiBidResponse;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.interf.pixcel.parser.LingJiClickParameterParserImpl;
import cn.shuzilm.util.AsyncRedisClient;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.*;

public class ExpTest {

    private static final Logger log = LoggerFactory.getLogger(LingJiClickParameterParserImpl.class);

    public static void main(String[] args) throws  Exception {

        YouYiBidResponse youYiBidResponse = new YouYiBidResponse();
        youYiBidResponse.setSession_id("gbP3WwoADDkjd3oB");
        YouYiAd youYiAd =new YouYiAd();
        youYiAd.setBid_price(500000);
        youYiAd.setAdz_id("houkp");
        youYiAd.setAdz_array_id(0);
        youYiAd.setAdz_id("houkp");
        youYiAd.setAdvertiser_id("houkpadv");
        youYiAd.setWin_para("houkp21");
        List youYiAdList =new ArrayList();
        youYiAdList.add(youYiAd);
        youYiBidResponse.setAds(youYiAdList);
        String resultData = JSON.toJSONString(youYiBidResponse);
        System.out.println(resultData);
        if (resultData.contains("session_id")) {
            BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
            JsonFormat.merge(resultData, builder);
            BidserverSsp.BidResponse build = builder.build();
            byte[] bytes = build.toByteArray();
            System.out.println( build.toByteArray());
            System.out.println(new String(bytes));

        }
//
//        Jedis jedis = JedisManager.getInstance().getResource();
////        jedis.set("houkp", "1111");
//        for (int i = 0; i < 100; i++) {
////            jedis.rpush("houkplist",  (String.valueOf(i)));
//            String houkplist = jedis.rpop("houkplist");
//            System.out.println(houkplist);
//        }

//        while (true){
//            System.out.println( JedisQueueManager.getLength("EXP_ERROR"));
//            System.out.println( JedisQueueManager.getElementFromQueue("EXP_ERROR"));
//        }

//        String url = "https://fanyi.baidu.com/";
//        if (url.contains("?")) {
//            String s = url + "&id=1" + "";
//            System.out.println(s);
//        } else {
//            String s = url + "?name=houkp&id=1" + "";
//            System.out.println(s);
//        }
//        String elemen="{\"adTypeId\":\"fullscreen\",\"adUid\":\"5d4af634-fdfb-4832-bbcb-e5ae2d317c4b\",\"adct\":1,\"adh\":960,\"adm\":\"http://cdn.shuzijz.cn/material/36da27ab-eb7e-493d-9db2-a34101ab3fa4.jpg\",\"admt\":\"pic\",\"advertiserUid\":\"89633fef-b7d0-4a36-802d-8960ffe5e851\",\"adw\":640,\"adxAdTypeId\":4,\"adxId\":\"2\",\"adxSource\":\"AdView\",\"appId\":\"e80c2dcb9bdac7acabb139c959afd071\",\"appName\":\"追追漫画\",\"appPackageName\":\"com.mandongkeji.comiclover\",\"appVersion\":\"1.1.3\",\"audienceuid\":\"dbbe7ee6-73d2-4e4a-b62f-e5efa25b661c\",\"biddingPrice\":25.0,\"bidid\":\"2018-10-26T11:02:06.42266b12440-ef52-49b6-8d91-4eb17d5bdaaf\",\"createTime\":0,\"creativeUid\":\"3e74df56-ab4f-4d65-a924-affd0e16f0fe\",\"crid\":\"192417516058\",\"desc\":\"\",\"descLong\":\"\",\"deviceId\":\"38834d31d495f55f55cc344fb307d43e\",\"did\":\"38834d31d495f55f55cc344fb307d43e\",\"dspid\":\"2018-10-26T11:02:06.4224b4ae5e9-d7de-484c-b5ee-8a309894cfd9\",\"duration\":0,\"impression\":[{\"banner\":{\"btype\":[4],\"h\":960,\"pos\":1,\"w\":640},\"bidfloor\":90000,\"bidfloorcur\":\"RMB\",\"id\":\"20181026-110206_reqimp_130-634-eaKC-884\",\"instl\":4,\"tagid\":\"POSIDu1crm2fgljmy\"}],\"landingUrl\":\"https://www.chengzijianzhan.com/tetris/page/1608029410456584/\",\"linkUrl\":\"\",\"materialId\":\"36da27ab-eb7e-493d-9db2-a34101ab3fa4\",\"mode\":\"cpm\",\"premiumFactor\":0.3,\"requestId\":\"20181026-110206_bidreq_130-1375-shod-910\",\"title\":\"\",\"titleLong\":\"\",\"tracking\":\"\",\"widthHeightRatio\":\"2/3\"}";
//        DUFlowBean duFlowBean= JSON.parseObject(elemen, DUFlowBean.class);
//        System.out.println(duFlowBean);
//        boolean exp = JedisQueueManager.putElementToQueue("EXP", duFlowBean, Priority.MAX_PRIORITY);
//        System.out.println(exp);
    }
}
