import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.util.MD5Util;
import com.google.common.hash.HashCode;
import org.python.modules._hashlib;

import java.util.List;

/**
 * Created by thunders on 2018/8/7.
 */
public class RtbControlTest {
    public void test(){


    }

    public static void main(String[] args) {
        String code ="20181028-103344_bidreq_175-2460-Ievx-222";
//        String replace = code.replace("-", "").replace("_", "");
        System.out.println(MD5Util.MD5(code).equals(MD5Util.MD5("20181028-103344_bidreq_175-2460-Ievx-222")));
        System.out.println(MD5Util.MD5(MD5Util.MD5(code)));
//        System.out.println(hashCode);
//        System.out.println(  JedisQueueManager.getLength("EXP_ERROR"));
    }
}
