import bidserver.BidserverSsp;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.youyi.request.YouYiAdzone;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiMobile;
import cn.shuzilm.bean.youyi.request.YouYiUser;
import cn.shuzilm.bean.youyi.response.YouYiBidResponse;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.filter.FilterRule;
import cn.shuzilm.interf.rtb.parser.TencentRequestServiceImpl;
import cn.shuzilm.interf.rtb.parser.YouYiRequestServiceImpl;
import cn.shuzilm.util.HttpClientUtil;
import cn.shuzilm.util.HttpRequestUtil;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSet {

    private static final Logger log = LoggerFactory.getLogger(TestSet.class);

    private static ExecutorService executor5 = Executors.newFixedThreadPool(100);
    public static void main(String[] args) throws Exception {
        String json ="{\"session_id\":\"-ABtXAoADCG3DMEE\",\"bucket_id\":\"1\",\"host_nodes\":\"bidr1a2.sora.cm2\",\"detected_language\":\"Zh-CN\",\"exchange\":{\"bid_id\":\"99fa0964-df52-4d9b-94eb-47fa84b2750e-1550647551325\",\"adx_id\":20},\"user\":{\"user_exid\":\"eaa6a77edea24d44c670870383db7427\",\"user_yyid\":\"eaa6a77edea24d44c670870383db7427\",\"user_ip\":\"124.78.186.127\",\"user_agent\":\"Mozilla/5.0 (Linux; Android 9; CLT-AL00 Build/HUAWEICLT-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/72.0.3626.105 Mobile Safari/537.36\",\"user_area\":1156310000,\"user_yyid_type\":3},\"adzone\":[{\"pid\":\"0EB6AFC248FCE140A8CB7216F351116F\",\"adz_id\":\"3cbc3297-4f2e-4303-97f2-473ba886324f\",\"adz_type\":\"ADZONE_TYPE_WAP_BANNER\",\"adz_width\":640,\"adz_height\":100,\"adz_ad_count\":1,\"adz_position\":0,\"reserve_price\":300,\"is_deep_link\":true}],\"site\":{\"inapp_app_category\":[\"IAB1\"]},\"mobile\":{\"is_app\":true,\"device_os\":\"android\",\"device_os_version\":\"9\",\"device_model\":\"huawei clt-al00\",\"device_brand\":\"clt-al00\",\"device_id\":\"eaa6a77edea24d44c670870383db7427\",\"network\":1,\"device_type\":\"phone\",\"app_id\":\"5bab377d\",\"device_resolution\":\"720*1493\",\"mac\":\"EC89144AFC09\",\"app_name\":\"数独\",\"md5_imei\":\"eaa6a77edea24d44c670870383db7427\",\"md5_android_id\":\"5b787e2c459307f18ceff914076fc07f\",\"md5_mac\":\"7d9d8677aeeefa8e561e3810713e6c1f\",\"app_bundle\":\"com.wedobest.shudu\"}}";


        parseRequest(json);


//        for (int i = 0; i < 1000; i++) {
////            executor1.execute(clickRedisTask);
////            executor2.execute(lingJiRedisTask);
////            executor3.execute(youYIRedisTask);
////            executor4.execute(adviewRedisTask);
//            int finalI = i;
//            executor5.execute(new Runnable() {
//                @Override
//                public void run() {
//                    int j=0;
//                    HttpRequestUtil.sendGet("http://pixelsz.shuzijz.cn/houkp","id="+ j++);
//                }
//            });
//
//        }
//        String json="{\"session_id\": \"QfVrXAoADCs3JF0D\",\"bucket_id\": \"1\",\"host_nodes\": \"bidr1a9.sora.cm2\",\"keywords\": [\"剧情\",\"热血\",\"农村\",\"内地剧场\",\"内地\",\"生活\",\"普通话\",\"新农村\",\"类型\",\"主旋律\",\"剧情\",\"剧情\",\"地区\",\"励志\",\"家庭\",\"配音语种\"],\"exchange\": {\"bid_id\": \"7de699048db6ea76d9bac496771d904a\",\"adx_id\": 10},\"user\": {\"user_exid\": \"3039AD51-3FD9-44C5-A3BF-7293C602CB58\",\"user_yyid\": \"3039AD51-3FD9-44C5-A3BF-7293C602CB58\",\"user_ip\": \"113.44.45.125\",\"user_area\": 1156110000,\"user_yyid_type\": 2},\"adzone\": [{\"pid\": \"1000000000669\",\"adz_id\": \"0\",\"adz_type\": \"ADZONE_TYPE_BANNER\",\"view_type\": 12,\"adz_width\": 600,\"adz_height\": 500,\"adz_ad_count\": 1,\"reserve_price\": 600}],\"site\": {\"url\": \"www.iqiyi.com\"},\"mobile\": {\"is_app\": true,\"device_os\": \"ios\",\"device_id\": \"3039AD51-3FD9-44C5-A3BF-7293C602CB58\",\"network\": 1,\"device_type\": \"phone\",\"idfa\": \"3039AD51-3FD9-44C5-A3BF-7293C602CB58\"}}";
//        YouYiRequestServiceImpl youYiRequestService =new YouYiRequestServiceImpl();
//        youYiRequestService.parseRequest(json);
        //      String s=" bidResponseBean:{\"bidid\":\"ec4c9d51a0682b3b898b498489f6f2af\",\"id\":\"20190116-132940_bidreq_130-1533-1lm9-506\",\"seatbid\":[{\"bid\":[{\"adct\":0,\"adid\":\"205c6379-1785-4ed0-add5-8878ec896090\",\"admt\":8,\"adurl\":\"http://m.lehaitv.com/?c=111254?advertiserUid=418c1058-1d73-441d-9c54-17c723a381e5&adUid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&creativeUid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&materialId=205c6379-1785-4ed0-add5-8878ec896090\",\"cid\":\"358910978317\",\"curl\":[\"http://pixel.shuzijz.cn/adviewclick?id=20190116-132940_bidreq_130-1533-1lm9-506&bidid=ec4c9d51a0682b3b898b498489f6f2af&impid=20190116-132940_reqimpPOSIDp3a9rrqual5b_130-465-u0i4-479&act=20190116132938&adx=2&did=ed11bdca169f6cc7c649dcc3bb8a4cca&device=ed11bdca169f6cc7c649dcc3bb8a4cca&app=%E7%82%B9%E7%82%B9%E6%96%B0%E9%97%BB&appn=com.yingliang.clicknews&appv=1.0.9.1&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=0&dcit=0&dcou=0&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.179.184.209\",\"http://re.shuzilm.cn/1yxcrase?ip=${AUCTION_ID}&idfa=${AUCTION_BID_ID}&imei=${AUCTION_IMP_ID}&os=${AUCTION_PRICE}&mac=${MAC}&mac1=${mac1}&ua=%%WIN_PRICE%%\"],\"impid\":\"20190116-132940_reqimpPOSIDp3a9rrqual5b_130-465-u0i4-479\",\"native\":{\"assets\":[{\"id\":1,\"title\":{\"text\":\"\"}},{\"data\":{\"value\":\"\"}},{\"id\":2,\"img\":{\"h\":720,\"url\":\"http://cdn.shuzijz.cn/material/205c6379-1785-4ed0-add5-8878ec896090.jpg\",\"w\":1280}}],\"imptrackers\":[\"http://pixel.shuzijz.cn/adviewclick?id=20190116-132940_bidreq_130-1533-1lm9-506&bidid=ec4c9d51a0682b3b898b498489f6f2af&impid=20190116-132940_reqimpPOSIDp3a9rrqual5b_130-465-u0i4-479&act=20190116132938&adx=2&did=ed11bdca169f6cc7c649dcc3bb8a4cca&device=ed11bdca169f6cc7c649dcc3bb8a4cca&app=%E7%82%B9%E7%82%B9%E6%96%B0%E9%97%BB&appn=com.yingliang.clicknews&appv=1.0.9.1&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=0&dcit=0&dcou=0&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.179.184.209\"],\"link\":{\"clicktrackers\":[\"http://pixel.shuzijz.cn/adviewclick?id=20190116-132940_bidreq_130-1533-1lm9-506&bidid=ec4c9d51a0682b3b898b498489f6f2af&impid=20190116-132940_reqimpPOSIDp3a9rrqual5b_130-465-u0i4-479&act=20190116132938&adx=2&did=ed11bdca169f6cc7c649dcc3bb8a4cca&device=ed11bdca169f6cc7c649dcc3bb8a4cca&app=%E7%82%B9%E7%82%B9%E6%96%B0%E9%97%BB&appn=com.yingliang.clicknews&appv=1.0.9.1&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=0&dcit=0&dcou=0&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.179.184.209\"],\"url\":\"http://m.lehaitv.com/?c=111254?advertiserUid=418c1058-1d73-441d-9c54-17c723a381e5&adUid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&creativeUid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&materialId=205c6379-1785-4ed0-add5-8878ec896090\"},\"ver\":\"1\"},\"nurl\":{\"0\":[\"http://re.shuzilm.cn/1yxcraqr?ip=${AUCTION_ID}&idfa=${AUCTION_BID_ID}&imei=${AUCTION_IMP_ID}&os=${AUCTION_PRICE}&mac=${MAC}&mac1=${mac1}&ua=%%WIN_PRICE%%\",\"http://pixel.shuzijz.cn/adviewnurl?id=20190116-132940_bidreq_130-1533-1lm9-506&bidid=ec4c9d51a0682b3b898b498489f6f2af&impid=20190116-132940_reqimpPOSIDp3a9rrqual5b_130-465-u0i4-479&price=%%WIN_PRICE%%&act=20190116132938&adx=2&did=ed11bdca169f6cc7c649dcc3bb8a4cca&device=ed11bdca169f6cc7c649dcc3bb8a4cca&app=%E7%82%B9%E7%82%B9%E6%96%B0%E9%97%BB&appn=com.yingliang.clicknews&appv=1.0.9.1&pf=1.0&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=0&dcit=0&dcou=0&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.179.184.209\"]},\"price\":700000,\"wurl\":\"http://pixel.shuzijz.cn/adviewexp?id=20190116-132940_bidreq_130-1533-1lm9-506&bidid=ec4c9d51a0682b3b898b498489f6f2af&impid=20190116-132940_reqimpPOSIDp3a9rrqual5b_130-465-u0i4-479&price=%%WIN_PRICE%%&act=20190116132938&adx=2&did=ed11bdca169f6cc7c649dcc3bb8a4cca&device=ed11bdca169f6cc7c649dcc3bb8a4cca&app=%E7%82%B9%E7%82%B9%E6%96%B0%E9%97%BB&appn=com.yingliang.clicknews&appv=1.0.9.1&pf=1.0&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=0&dcit=0&dcou=0&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.179.184.209\"}]}]}\n";
//        String substring = s.substring(s.indexOf("price\":"));
//        String substring1 = substring.substring(substring.indexOf("\":")+2, substring.indexOf(",\""));
//        System.out.println(substring);
//        System.out.println(substring1);
//        String body="{\"session_id\": \"EnpRXAoADF-gNXUE\",\"bucket_id\": \"1\",\"host_nodes\": \"bidr4a6.sora.cm2\",\"keywords\": [\"18以上\",\"普通话\",\"搞笑\",\"日漫\",\"动画电影\",\"配音语种\",\"地区\",\"冒险\",\"年龄段\",\"剧场版\",\"版本\",\"番剧\",\"类型\"],\"exchange\": {\"bid_id\": \"3eda1df39770fc055b033e36f1924528\",\"adx_id\": 10},\"user\": {\"user_exid\": \"ac289400faf6e47098c866dc813a7b22\",\"user_yyid\": \"ac289400faf6e47098c866dc813a7b22\",\"user_ip\": \"223.21.25.75\",\"user_area\": 1156110000,\"user_yyid_type\": 3},\"adzone\": [{\"pid\": \"1000000000663\",\"adz_id\": \"0\",\"adz_type\": \"ADZONE_TYPE_WAP_BANNER\",\"adz_width\": 160,\"adz_height\": 160,\"adz_ad_count\": 1,\"reserve_price\": 75}],\"site\": {\"url\": \"www.iqiyi.com\"},\"mobile\": {\"is_app\": true,\"device_os\": \"android\",\"device_id\": \"ac289400faf6e47098c866dc813a7b22\",\"network\": 1,\"device_type\": \"phone\",\"imei\": \"A0000071EBD0F6\",\"md5_imei\": \"ac289400faf6e47098c866dc813a7b22\"}}";
//        YouYiRequestServiceImpl youYiRequestService  =new YouYiRequestServiceImpl();
//        try {
//            String s = youYiRequestService.parseRequest(body);
//            System.out.println(s);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        BidserverSsp.BidResponse.Builder builder =  BidserverSsp.BidResponse.newBuilder();
//        builder.setSessionId("1111");
//        BidserverSsp.BidResponse build = builder.build();
//        byte[] buff =  build.toByteArray();
//        BidserverSsp.BidResponse bidResponse = BidserverSsp.BidResponse.parseFrom(buff);
//        System.out.println(bidResponse.getSessionId());
//        BitSet bm = new BitSet();
//        int randomVal = 1000000;
//        System.out.println(new Random().nextInt(randomVal));
//        for (int i = 0; i < randomVal; i++) {
//            bm.set(new Random().nextInt(randomVal));
//        }
//        BitSet bm2 = new BitSet();
//        for (int i = 0; i < randomVal; i++) {
//            bm2.set(new Random().nextInt(randomVal));
//        }
//        long s = System.currentTimeMillis();
//        bm2.and(bm);
//        Map map = new HashMap();
//        List list =new ArrayList();
//        for (int j = bm2.nextSetBit(1); j < bm2.size(); j++) {
//            if (bm2.get(j)){
////                map.put(j,"");
//                list.add(j);
//            }
//        }
//
//        long e = System.currentTimeMillis();
//        System.out.println(e - s + "毫秒");
//        System.out.println(map.size());
//        System.out.println("list.size:"+list.size()+",list :"+list);
//        System.out.println(i);
//        System.out.println(bm2);
//        System.out.println(bm);


//        HashSet bm1 = new HashSet();
//        for (int i = 0; i < 1000000; i++) {
//            bm1.add(new Random().nextInt(randomVal));
//        }
//        Set bm21 = new HashSet();
//        for (int i = 0; i < 1000000; i++) {
//            bm21.add(new Random().nextInt(randomVal));
//        }
//
//        System.out.println(bm1.size());
//        long s1 = System.currentTimeMillis();
//        bm1.retainAll(bm21);
//        long e1 = System.currentTimeMillis();
//        System.out.println(e1 - s1 + "毫秒");
//        System.out.println(bm1.size());
//        System.out.println(bm21.size());




    }

    public static String parseRequest(String dataStr) throws Exception {
        String adxId = "3";
        String response = "";
            //请求报文解析
            YouYiBidRequest bidRequestBean = JSON.parseObject(dataStr, YouYiBidRequest.class);
            String session_id = bidRequestBean.getSession_id();
            //创建返回结果  bidRequest请求参数保持不变
            YouYiMobile userDevice = bidRequestBean.getMobile();//设备APP信息
            YouYiAdzone adzone = bidRequestBean.getAdzone().get(0);//曝光信息
            YouYiUser user = bidRequestBean.getUser();//用户信息

            Integer width = null;//广告位的宽
            Integer height = null;//广告位的高
//            Integer showtype = userImpression.getExt().getShowtype();//广告类型
            String adType = null; //对应内部 广告类型
            String stringSet = null;//文件类型列表
            String deviceId = null;//设备号

            String appPackageName = null;//应用包名
            if (userDevice != null) {
                appPackageName = userDevice.getApp_bundle();
            }
//            if (StringUtils.isBlank(adType)) {
//                response = "没有对应的广告类型";
//                return response;
//
//            }


//            //设备的设备号：用于匹配数盟库中的数据

            if (userDevice != null) {
                if ("ios".equals(userDevice.getDevice_os().toLowerCase())) {
                    deviceId = userDevice.getIdfa();
                } else if ("android".equalsIgnoreCase(userDevice.getDevice_os().toLowerCase())) {
                    //竞价请求进来之前对imei和mac做过滤
                    if (userDevice.getMd5_imei() != null) {
                        if (userDevice.getMd5_imei().length() == 32) {
                        }
                    } else if (userDevice.getMd5_mac() != null) {
                        if (userDevice.getMd5_mac().length() == 32) {
                            userDevice.setMd5_imei("mac-" + userDevice.getMd5_mac());
                        }
                    } else {
                        response = session_id + "deviceIdBlackList";
                        return response;
                    }
                    deviceId = userDevice.getMd5_imei();
                }
            }


            Map msg = FilterRule.filterRuleBidRequest(deviceId, appPackageName, user.getUser_ip());//过滤规则的返回结果

            //ip黑名单和 设备黑名单，媒体黑名单 内直接返回
            if (msg.get("ipBlackList") != null) {
                return "ipBlackList" + session_id;
            } else if (msg.get("bundleBlackList") != null) {
                return "bundleBlackList" + session_id;
            } else if (msg.get("deviceIdBlackList") != null) {
                return "deviceIdBlackList" + session_id;
            }


            //是否匹配长宽
            Boolean isDimension = true;
            //通过广告id获取长宽
            List adxNameList = new ArrayList();//
//            //支持的文件类型
            String adz_type = adzone.getAdz_type();
            if (adz_type.equals("ADZONE_TYPE_INAPP_BANNER") | adz_type.equals("ADZONE_TYPE_WAP_BANNER")) {
                stringSet = "[image/jpeg, image/png]";
                //广告位的宽和高
                width = adzone.getAdz_width();
                height = adzone.getAdz_height();
            } else if (adz_type.equals("ADZONE_TYPE_INAPP_VIDEO") | adz_type.equals("ADZONE_TYPE_WAP_VIDEO")) {
                stringSet = "[ application/x-shockwave-flash，video/x-flv]";
                //广告位的宽和高
                width = adzone.getAdz_width();
                height = adzone.getAdz_height();
            } else if (adz_type.equals("ADZONE_TYPE_INAPP_NATIVE")) {
                stringSet = "[image/jpeg, image/png]";
                adxNameList.add(adxId + "_" + adzone.getNative().get(0).getNative_id());
                //广告位的宽和高
                width = adzone.getAdz_width();
                height = adzone.getAdz_height();
            }
            if (width == null || width==0|| height == null||height==0) {
                width = -1;
                height = -1;
                //是否匹配长宽
                isDimension = false;
            }

            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                    deviceId,
                    adType,//广告类型
                    width,//广告位的宽
                    height,//广告位的高
                    true,// 是否要求分辨率
                    0,//宽误差值
                    0,// 高误差值;
                    adxId,//ADX 服务商ID
                    stringSet,//文件扩展名
                    user.getUser_ip(),//用户ip
                    appPackageName,//APP包名
                    adxNameList,//长宽列表
                    isDimension,
                    bidRequestBean.getSession_id());




    return  null;
    }


}
