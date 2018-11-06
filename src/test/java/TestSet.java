import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.MD5Util;
import cn.shuzilm.util.aes.AES;

import java.time.LocalDateTime;
import java.util.*;

public class TestSet {
    public static void main(String[] args) {
//        String s ="xb4i-wntscmk1sosgpnm15bwqstj-fizotzeoabylhg";
        System.out.println(MD5Util.MD5(MD5Util.MD5("y109_8081-t19-1541435572-96-639")));
        String s ="EzAXnvO2KYXln_X8HNg2blTVrnOoRY0ob6aVXwaMaTo";
        System.out.println(AES.decrypt(s.replace("-",""), "af36ec6c77c042b5a5e49e6414fb436f"));
//        Help.sendAlert("houkp2");

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
