import bidserver.BidserverSsp;
import cn.shuzilm.bean.tencent.request.TencentBidRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;

public class TestSet {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        String response="{\"request_id\":\"njhjri3ypryl6\",\"seat_bids\":[{\"bids\":[{\"bid_price\":1500,\"click_param\":\"id=njhjri3ypryl6&bidid=20190123112538711963389cf-b952-41a4-b370-183d04ac8a4d&impid=0&act=20190123112538711&adx=4&did=fa6531e5a360c24755c90593adde2d97&device=fa6531e5a360c24755c90593adde2d97&app=&appn=com.qq.e.union.demo&appv=null&ddem=d16a0dad-c693-4bf1-968b-69fd3ba944c9&dcuid=a8dbd3dd-04cf-4d28-86cc-b2d35277ba1b&dpro=2&dcit=36&dcou=381&dade=9dfc3dcb-8687-40b9-a85f-889ac8e1d7e9&dage=2e76b7a0-71b0-4860-b347-8f67e11c8672&daduid=cd346c3c-eb3b-473b-8c67-556ee2a7a64c&pmp=null&userip=10.223.139.234\",\"creaive_id\":\"4_113\",\"impression_param\":\"id=njhjri3ypryl6&bidid=20190123112538711963389cf-b952-41a4-b370-183d04ac8a4d&impid=0&price=__WIN_PRICE__&act=20190123112538711&adx=4&did=fa6531e5a360c24755c90593adde2d97&device=fa6531e5a360c24755c90593adde2d97&app=&appn=com.qq.e.union.demo&appv=null&pf=0.3&ddem=d16a0dad-c693-4bf1-968b-69fd3ba944c9&dcuid=a8dbd3dd-04cf-4d28-86cc-b2d35277ba1b&dpro=2&dcit=36&dcou=381&dade=9dfc3dcb-8687-40b9-a85f-889ac8e1d7e9&dage=2e76b7a0-71b0-4860-b347-8f67e11c8672&daduid=cd346c3c-eb3b-473b-8c67-556ee2a7a64c&pmp=null&userip=10.223.139.234\",\"winnotice_param\":\"id=njhjri3ypryl6&bidid=20190123112538711963389cf-b952-41a4-b370-183d04ac8a4d&impid=0&price=__WIN_PRICE__&act=20190123112538711&adx=4&did=fa6531e5a360c24755c90593adde2d97&device=fa6531e5a360c24755c90593adde2d97&app=&appn=com.qq.e.union.demo&appv=null&pf=0.3&ddem=d16a0dad-c693-4bf1-968b-69fd3ba944c9&dcuid=a8dbd3dd-04cf-4d28-86cc-b2d35277ba1b&dpro=2&dcit=36&dcou=381&dade=9dfc3dcb-8687-40b9-a85f-889ac8e1d7e9&dage=2e76b7a0-71b0-4860-b347-8f67e11c8672&daduid=cd346c3c-eb3b-473b-8c67-556ee2a7a64c&pmp=null&userip=10.223.139.234\"}],\"impression_id\":\"0\"}]}";
//        String json="{\"id\": \"unea4fqyc36j6\",\"impressions\": [{\"id\": \"1060741621409059\",\"placement_id\": 1060741621409059,\"creative_specs\": [148,364],\"bid_floor\": 150,\"natives\": [{\"required_fields\": 15,\"type\": \"LINK\"},{\"required_fields\": 15,\"type\": \"APP\"}],\"blocking_keyword\": [\"安全\",\"清理\",\"丰胸\",\"偷窥\",\"神器\",\"骚扰\",\"一夜情\",\"下葬\",\"CPU\",\"女优\",\"死尸\",\"小三\",\"粗\",\"骨灰盒\",\"SM\",\"省电\",\"寂寞交友\",\"棺材\",\"专清\",\"应用锁\",\"又粗又大\",\"午夜\",\"尖叫\",\"不射\",\"坟墓\",\"杀毒\",\"病毒\",\"屏保\",\"瘦\",\"僵尸\",\"传销\",\"卫士\",\"傻逼\",\"降温\",\"射精\",\"治疗\",\"增高\",\"血腥\",\"查杀\",\"丧尸\",\"减肥\",\"锁屏\",\"约吗\",\"粗大\"],\"pretargeting_ids\": [\"9477746\"],\"product_types\": [\"PRODUCT_TYPE_PAGE_LINK\",\"PRODUCT_TYPE_ANDROID_APP\",\"PRODUCT_TYPE_IOS_APP\"], \"16\": [1]}],\"device\": {\"id\": \"5398b8a4a06447047f6726a507829b3d\",\"device_type\": \"DEVICETYPE_MOBILE\",\"os\": \"OS_ANDROID\",\"os_version\": \"6.0\",\"user_agent\": \"GDTADNetClient-[Dalvik/2.1.0 (Linux; U; Android 6.0; HUAWEI MT7-TL10 Build/HuaweiMT7-TL10)]\",\"screen_width\": 1080,\"screen_height\": 1920,\"connection_type\": \"CONNECTIONTYPE_WIFI\",\"brand_and_model\": \"HUAWEI MT7-TL10\",\"language\": \"zh\",\"manufacturer\": \"HUAWEI\",\"android_id\": \"6414a82a7419f072d0f5bf0145cdfec6\"},\"ip\": \"123.192.65.77\",\"geo\": {\"latitude\": 25089638,\"longitude\": 121529764,\"accuracy\": 40.0},\"app\": {\"industry_id\": 50205,\"app_bundle_id\": \"com.cleanmaster.mguard_cn\"},\"support_deep_link\": true}";
        String json="{\"id\": \"sddz3lxm74jua\",\"impressions\": [{\"id\": \"3000544378524080\",\"placement_id\": 3000544378524080,\"creative_specs\": [148],\"bid_floor\": 150,\"natives\": [{\"required_fields\": 15,\"type\": \"LINK\"},{\"required_fields\": 15,\"type\": \"APP\"}],\"pretargeting_ids\": [\"9477746\"],\"product_types\": [\"PRODUCT_TYPE_PAGE_LINK\",\"PRODUCT_TYPE_ANDROID_APP\",\"PRODUCT_TYPE_IOS_APP\"], \"16\": [1]}],\"device\": {\"id\": \"7f16b947e11a3db09dea2fd21b0e3d30\",\"device_type\": \"DEVICETYPE_MOBILE\",\"os\": \"OS_ANDROID\",\"os_version\": \"4.3\",\"user_agent\": \"GDTADNetClient-[Dalvik/1.6.0 (Linux; U; Android 4.3; GT-N7100 Build/JSS15J)]\",\"screen_width\": 720,\"screen_height\": 1280,\"connection_type\": \"CONNECTIONTYPE_WIFI\",\"brand_and_model\": \"GT-N7100\",\"language\": \"zh\",\"manufacturer\": \"samsung\",\"android_id\": \"cf6f6835164531df5a89a040e1939966\"},\"ip\": \"111.241.215.67\",\"geo\": {\"latitude\": 24999593,\"longitude\": 121619597,\"accuracy\": 22.593000411987305},\"app\": {\"industry_id\": 51403,\"app_bundle_id\": \"com.yr.azj\"},\"support_deep_link\": true}";
        JSONObject jsonObject = JSON.parseObject(json);
        System.out.println(jsonObject);

        TencentBidRequest bidRequestBean = JSON.parseObject(json, TencentBidRequest.class);
        System.out.println(bidRequestBean);
//        String s = "click_param";
//        if (response.contains(s)) {
//            String substring = response.substring(response.indexOf(s));
//            System.out.println(substring);
//            String tencentexp = substring.substring( substring.indexOf("id="));
//            String serviceUrl = "http://59.110.220.112:9880/tencentexp?"+tencentexp;
//            System.out.println(serviceUrl);
////            String tencentexpUrl = tencentexp + price + pf;
////            Boolean flag = sendGetUrl(tencentexpUrl);
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
