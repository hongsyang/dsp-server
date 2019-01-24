import bidserver.BidserverSsp;
import com.google.protobuf.InvalidProtocolBufferException;

public class TestSet {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        String response="{\"request_id\":\"njhjri3ypryl6\",\"seat_bids\":[{\"bids\":[{\"bid_price\":1500,\"click_param\":\"id=njhjri3ypryl6&bidid=20190123112538711963389cf-b952-41a4-b370-183d04ac8a4d&impid=0&act=20190123112538711&adx=4&did=fa6531e5a360c24755c90593adde2d97&device=fa6531e5a360c24755c90593adde2d97&app=&appn=com.qq.e.union.demo&appv=null&ddem=d16a0dad-c693-4bf1-968b-69fd3ba944c9&dcuid=a8dbd3dd-04cf-4d28-86cc-b2d35277ba1b&dpro=2&dcit=36&dcou=381&dade=9dfc3dcb-8687-40b9-a85f-889ac8e1d7e9&dage=2e76b7a0-71b0-4860-b347-8f67e11c8672&daduid=cd346c3c-eb3b-473b-8c67-556ee2a7a64c&pmp=null&userip=10.223.139.234\",\"creaive_id\":\"4_113\",\"impression_param\":\"id=njhjri3ypryl6&bidid=20190123112538711963389cf-b952-41a4-b370-183d04ac8a4d&impid=0&price=__WIN_PRICE__&act=20190123112538711&adx=4&did=fa6531e5a360c24755c90593adde2d97&device=fa6531e5a360c24755c90593adde2d97&app=&appn=com.qq.e.union.demo&appv=null&pf=0.3&ddem=d16a0dad-c693-4bf1-968b-69fd3ba944c9&dcuid=a8dbd3dd-04cf-4d28-86cc-b2d35277ba1b&dpro=2&dcit=36&dcou=381&dade=9dfc3dcb-8687-40b9-a85f-889ac8e1d7e9&dage=2e76b7a0-71b0-4860-b347-8f67e11c8672&daduid=cd346c3c-eb3b-473b-8c67-556ee2a7a64c&pmp=null&userip=10.223.139.234\",\"winnotice_param\":\"id=njhjri3ypryl6&bidid=20190123112538711963389cf-b952-41a4-b370-183d04ac8a4d&impid=0&price=__WIN_PRICE__&act=20190123112538711&adx=4&did=fa6531e5a360c24755c90593adde2d97&device=fa6531e5a360c24755c90593adde2d97&app=&appn=com.qq.e.union.demo&appv=null&pf=0.3&ddem=d16a0dad-c693-4bf1-968b-69fd3ba944c9&dcuid=a8dbd3dd-04cf-4d28-86cc-b2d35277ba1b&dpro=2&dcit=36&dcou=381&dade=9dfc3dcb-8687-40b9-a85f-889ac8e1d7e9&dage=2e76b7a0-71b0-4860-b347-8f67e11c8672&daduid=cd346c3c-eb3b-473b-8c67-556ee2a7a64c&pmp=null&userip=10.223.139.234\"}],\"impression_id\":\"0\"}]}";
        String s = "click_param";
        if (response.contains(s)) {
            String substring = response.substring(response.indexOf(s));
            System.out.println(substring);
            String tencentexp = substring.substring( substring.indexOf("id="));
            String serviceUrl = "http://59.110.220.112:9880/tencentexp?"+tencentexp;
            System.out.println(serviceUrl);
//            String tencentexpUrl = tencentexp + price + pf;
//            Boolean flag = sendGetUrl(tencentexpUrl);
        }

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
