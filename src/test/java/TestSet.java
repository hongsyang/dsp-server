import bidserver.BidserverSsp;
import cn.shuzilm.interf.pixcel.parser.AdViewExpParameterParserImpl;
import cn.shuzilm.interf.pixcel.parser.AdViewNurlParameterParserImpl;
import com.google.protobuf.InvalidProtocolBufferException;

public class TestSet {
    public static void main(String[] args) throws InvalidProtocolBufferException {
       String url ="lingjiexp?id=y187_8081-t29-1547711542-84-588&bidid=94aa1bf3d065a6c31588f995cc70f06d&impid=1d528cb87ffc4e6faf7bd3765ae76bbc&price=EkTwjiCQRjI2DnbpKSUDtysa9U2K6kEmCyoazh89drg&act=20190117155221&adx=1&did=ef06d16193b6295de8518bbab415f3e7&device=ef06d16193b6295de8518bbab415f3e7&app=%E7%AC%94%E8%B6%A3%E9%98%81%E5%B0%8F%E8%AF%B4&appn=cn.xbqg&appv=null&pf=1.0&ddem=902ce044-b7f6-41e9-a980-e8bb0c96591c&dcuid=ba0bd9d1-d479-4399-bc4d-196bfbfa3237&dpro=4&dcit=38&dcou=439&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=ce58ab5e-a042-4f0f-9e6f-331ed7993cd1&pmp=null&userip=183.197.23.159&remoteIp=183.197.23.159";
        AdViewExpParameterParserImpl.parseUrlStr(url);
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
