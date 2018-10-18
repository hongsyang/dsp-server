import cn.shuzilm.common.jedis.JedisQueueManager;

import java.time.LocalDateTime;
import java.util.*;

public class TestSet {
    public static void main(String[] args) {
        System.out.println(  JedisQueueManager.getLength("Exp"));


        Date date = new Date();//时间小时数
        System.out.println(LocalDateTime.now().getHour());


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
