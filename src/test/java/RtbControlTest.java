import cn.shuzilm.common.jedis.JedisQueueManager;

import java.util.List;

/**
 * Created by thunders on 2018/8/7.
 */
public class RtbControlTest {
    public void test(){


    }

    public static void main(String[] args) {
        List<Object> exp = JedisQueueManager.getAllElement("EXP");
        Object exp1 = JedisQueueManager.getElementFromQueue("EXP");
        //        JedisQueueManager.removeAll("EXP");
        System.out.println(exp1);
        System.out.println(exp.size());
        for (Object o : exp) {
            System.out.println(o+"11111111111111111");
        }
    }
}
