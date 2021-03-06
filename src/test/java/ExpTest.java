import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.interf.pixcel.parser.LingJiClickParameterParserImpl;
import cn.shuzilm.interf.pixcel.parser.TencentImpParameterParserImpl;
import cn.shuzilm.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ExpTest {

    private static final Logger log = LoggerFactory.getLogger(LingJiClickParameterParserImpl.class);

    public static void main(String[] args) throws  Exception {
//        HttpClientUtil.get("https://api.ipplus360.com/ip/res/v1/poi/?key=156186&ip=12.32.26.53&type=00");
        DUFlowBean targetDuFlowBean =new DUFlowBean();
        targetDuFlowBean.setCrid("");
        if (targetDuFlowBean.getCrid()== null || "".equals(targetDuFlowBean.getCrid().trim()) || "null".equals(targetDuFlowBean.getCrid().toLowerCase())) {
            MDC.put("sift", "ExceptionMaterialId");
            log.debug("请求id:{},素材id,推审id:{}，广告位列表:{},是否匹配长宽:{}", "", targetDuFlowBean.getMaterialId(), targetDuFlowBean.getCrid(),"222","1");//
            MDC.remove("sift");

        }

//        String result="ipBlackListmy089_8086-t25-1550823314-7-704";
//        if (result != null) {
//            if (result.contains("ipBlackList")) {
//                MDC.put("sift", "ipBlackList");
//                log.debug("requestId:{},result:{}",result);
//                MDC.remove("sift");
//                System.out.println("ipBlackList");
//            }
//            if (result.contains("bundleBlackList")) {
//                System.out.println("bundleBlackList");
//            }
//            if (result.contains("deviceIdBlackList")) {
//                MDC.put("sift", "deviceIdBlackList");
//                log.debug("requestId:{},result:{}",result);
//                MDC.remove("sift");
//                System.out.println("deviceIdBlackList");
//            }
//        }
//        String url="tencentimp?bidid=ancftd3dt6hla&win=hJBhSEhMKDrvIPEkHumcug==&impparam=ancftd3dt6hla&bidid=2019021915493968657605ba1-3618-4b11-9b&impid=1060515766791231&act=20190219154939686&adx=4&device=9fc5a30c-28a2-4405-9d0b-0c0bfdc60377&appn=com.chancky.365Read&pf=0.3&ddem=0698471f-1829-419d-9bde-49369151e66f&dcuid=f11d160c-8bd4-4333-94ef-a4818d30e318&dpro=2&dcit=36&dcou=377&dade=89633fef-b7d0-4a36-802d-8960ffe5e851&dage=null&daduid=fedf05bb-8b8c-499c-bb60-f3a4e64dd6cc&userip=124.193.88.142&ip=124.193.88.142&remoteIp=14.17.3.31";


//        if (url.contains("tencentimp")) {
////            bundleBlackListFlag = 0;
//            System.out.println(0);
//        }else {
//            System.out.println(1);
//        }
//        TencentImpParameterParserImpl.parseUrlStr(url);
//        String  url= "/adviewexp?id=20190103-110336_bidreq_174-2623-sVuT-107&bidid=fd5b512c637ab3e8102fd5fe9d50b1b0&impid=20190103-110336_reqimp_174-1809-9ahQ-65&price=iIyqEWgBAABxdFQKZ0JQI4Jy3wA0Dgq9yAoWTw&act=20190103110335&adx=2&did=eafc3db8a9e2d1ac6d4c2874fa884f82&device=eafc3db8a9e2d1ac6d4c2874fa884f82&app=%E8%A1%8C%E5%AE%B6%E8%AF%B4%E8%AF%B4-%E5%AE%89%E5%8D%93&appn=cn.hangjiashuo.app&appv=1.1.3&pf=1.0&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=11&dcit=113&dcou=1187&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.182.243.158";
//        String s="ipBlackList204session_id:1111";
//        String substring = s.substring(s.indexOf("204session_id")+14);
//        System.out.println(substring);
//        List<String> list = UrlParserUtil.urlParser(url);
//        for (String s : list) {
//            System.out.println(s);
//        }
//        YouYiBidResponse youYiBidResponse = new YouYiBidResponse();
//        youYiBidResponse.setSession_id("gbP3WwoADDkjd3oB");
//        YouYiAd youYiAd =new YouYiAd();
//        youYiAd.setBid_price(500000);
//        youYiAd.setAdz_id("houkp");
//        youYiAd.setAdz_array_id(0);
//        youYiAd.setAdz_id("houkp");
//        youYiAd.setAdvertiser_id("houkpadv");
//        youYiAd.setWin_para("houkp21");
//        List youYiAdList =new ArrayList();
//        youYiAdList.add(youYiAd);
//        youYiBidResponse.setAds(youYiAdList);
//        String resultData = JSON.toJSONString(youYiBidResponse);
//        System.out.println(resultData);
//        if (resultData.contains("session_id")) {
//            BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
//            JsonFormat.merge(resultData, builder);
//            BidserverSsp.BidResponse build = builder.build();
//            byte[] bytes = build.toByteArray();
//            System.out.println( build.toByteArray());
//            System.out.println(new String(bytes));
//
//        }
//
//        Jedis jedis = JedisManager.getInstance().getResource();
////        jedis.set("houkp", "1111");
//        for (int i = 0; i < 100; i++) {
////            jedis.rpush("houkplist",  (String.valueOf(i)));
//            String houkplist = jedis.rpop("houkplist");
//            System.out.println(houkplist);
//        }

//        while (true){
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
//        System.out.println(exp);
    }
}
