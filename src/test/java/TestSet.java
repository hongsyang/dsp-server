import bidserver.BidserverSsp;
import cn.shuzilm.interf.rtb.parser.YouYiRequestServiceImpl;
import cn.shuzilm.util.HttpClientUtil;
import cn.shuzilm.util.HttpRequestUtil;
import com.google.protobuf.InvalidProtocolBufferException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSet {

    private static ExecutorService executor5 = Executors.newFixedThreadPool(100);
    public static void main(String[] args) throws InvalidProtocolBufferException {
        for (int i = 0; i < 1000; i++) {
//            executor1.execute(clickRedisTask);
//            executor2.execute(lingJiRedisTask);
//            executor3.execute(youYIRedisTask);
//            executor4.execute(adviewRedisTask);
            int finalI = i;
            executor5.execute(new Runnable() {
                @Override
                public void run() {
                    int j=0;
                    HttpRequestUtil.sendGet("http://pixelsz.shuzijz.cn/houkp","id="+ j++);
                }
            });

        }
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
}
