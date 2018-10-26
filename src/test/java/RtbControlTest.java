import cn.shuzilm.common.jedis.JedisQueueManager;

import java.util.List;

/**
 * Created by thunders on 2018/8/7.
 */
public class RtbControlTest {
    public void test(){


    }

    public static void main(String[] args) {
        System.out.println(  JedisQueueManager.getLength("EXP"));
    }
}
