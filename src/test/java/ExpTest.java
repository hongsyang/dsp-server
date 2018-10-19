import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.interf.pixcel.parser.LingJiClickParameterParserImpl;
import cn.shuzilm.util.AsyncRedisClient;
import com.alibaba.fastjson.JSON;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExpTest {

    private static final Logger log = LoggerFactory.getLogger(LingJiClickParameterParserImpl.class);

    public static void main(String[] args) throws InterruptedException {
        Long exp = JedisQueueManager.getLength("Exp");
        Long exp_error = JedisQueueManager.getLength("EXP_ERROR");
        System.out.println(exp);
        System.out.println(exp_error);
    }
}