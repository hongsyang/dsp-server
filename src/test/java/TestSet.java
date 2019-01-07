import bidserver.BidserverSsp;
import cn.shuzilm.util.HttpClientUtil;
import com.google.protobuf.InvalidProtocolBufferException;

public class TestSet {

    static class ThreadDemo extends Thread {
        @Override
        public void run() {
//            String    uri ="http://172.17.129.131:9880/adviewexp?id=20190103-110336_bidreq_174-2623-sVuT-107&bidid=fd5b512c637ab3e8102fd5fe9d50b1b0&impid=20190103-110336_reqimp_174-1809-9ahQ-65&price=iIyqEWgBAABxdFQKZ0JQI4Jy3wA0Dgq9yAoWTw&act=20190103110335&adx=2&did=eafc3db8a9e2d1ac6d4c2874fa884f82&device=eafc3db8a9e2d1ac6d4c2874fa884f82&app=%E8%A1%8C%E5%AE%B6%E8%AF%B4%E8%AF%B4-%E5%AE%89%E5%8D%93&appn=cn.hangjiashuo.app&appv=1.1.3&pf=1.0&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=11&dcit=113&dcou=1187&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=67b25f02-a0f3-4060-9fed-9dbde33d7fa4&pmp=null&userip=117.182.243.158";
            String    uri ="http://172.17.129.131:9880/adviewnurl?id=20190103-110336_bidreq_174-2623-sVuT-107&bidid=fd5b512c637ab3e8102fd5fe9d50b1b0&impid=20190103-110336_reqimp_174-1809-9ahQ-65&price=1000&act=20190103110335&adx=2&did=eafc3db8a9e2d1ac6d4c2874fa884f82&device=eafc3db8a9e2d1ac6d4c2874fa884f82&app=%E8%A1%8C%E5%AE%B6%E8%AF%B4%E8%AF%B4-%E5%AE%89%E5%8D%93&appn=cn.hangjiashuo.app&appv=1.1.3&pf=1.0&ddem=5369c1f4-639f-4801-a022-dd68832e510a&dcuid=19a0c5df-c889-48a5-a6cb-3d2b67ba69a8&dpro=11&dcit=113&dcou=1187&dade=418c1058-1d73-441d-9c54-17c723a381e5&dage=&daduid=6e201ce5-c691-4677-8dac-fd1dbd0c36f2&pmp=null&userip=117.182.243.158";
            for (int i = 0; i <100 ; i++) {
                String s = HttpClientUtil.get(uri);
                System.out.println("次数："+ i +"值："+s);
            }
        }
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
//        ThreadDemo thread1 =new ThreadDemo();
//        ThreadDemo thread2 =new ThreadDemo();
//        ThreadDemo thread3 =new ThreadDemo();
//        ThreadDemo thread4 =new ThreadDemo();
//        ThreadDemo thread5 =new ThreadDemo();
//        ThreadDemo thread6 =new ThreadDemo();
        ThreadDemo thread7 =new ThreadDemo();
//        thread1.start();
//        thread2.start();
//        thread3.start();
//        thread4.start();
//        thread5.start();
//        thread6.start();
        thread7.start();

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
