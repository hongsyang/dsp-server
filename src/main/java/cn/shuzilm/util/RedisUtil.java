package cn.shuzilm.util;

import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.RtbJedisManager;
import cn.shuzilm.common.redis.RedisQueueManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nutz.ssdb4j.impl.SimpleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtil {
    private static final Logger log = LoggerFactory.getLogger(SSDBUtil.class);

    private static final String FILTER_CONFIG = "configs_rtb_redis.properties";

    private static AppConfigs redisConfigs = AppConfigs.getInstance(FILTER_CONFIG);

    private static JedisPool resource = new JedisPool(redisConfigs.getString("REDIS_SERVER_HOST"), redisConfigs.getInt("REDIS_SERVER_PORT"));

    private static Jedis jedis = new Jedis(redisConfigs.getString("REDIS_SERVER_HOST"), redisConfigs.getInt("REDIS_SERVER_PORT"));



    public static void main(String[] args) {
        DUFlowBean duFlowBean = getDUFlowBean("20190327-175606_bidreq_176-1090-n70g-32");
        if (duFlowBean==null){

            System.out.println("null1111");
        }else {

            System.out.println(duFlowBean);
        }
    }

    /**
     * 把生成的内部流转DUFlowBean上传到redis服务器 设置60分钟失效
     *
     * @param targetDuFlowBean
     */
    public static void pushRedis(DUFlowBean targetDuFlowBean) {
        MDC.put("sift", "redis");
        try {
            if (jedis != null) {
                String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
                Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 60 * 60);//设置超时时间为60分钟
                log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{},RequestId;{}", set, expire, targetDuFlowBean.getRequestId());
            } else {
                jedis = new JedisPool(redisConfigs.getString("REDIS_SERVER_HOST"), redisConfigs.getInt("REDIS_SERVER_PORT")).getResource();
                String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
                Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 60 * 60);//设置超时时间为60分钟
                log.debug("jedis为空：{},重新加载", jedis);
                log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{},RequestId;{}", set, expire, targetDuFlowBean.getRequestId());
            }
        } catch (Exception e) {
//            resource.returnBrokenResource(jedis);
            MDC.put("sift", "redis");
            log.error(" jedis Exception :{}", e);
        } finally {
//            resource.returnResource(jedis);
            jedis.close();
        }

    }


    /**
     * 根据requestId获取DUFlowBea
     *
     * @param requestId
     */
    public static DUFlowBean getDUFlowBean(String requestId) {
        MDC.put("sift", "redis");
        DUFlowBean duFlowBean = null;
        try {
            if (jedis != null) {
                if (jedis.get(requestId) != null) {
                    String duFlowBeanJson = jedis.get(requestId);
                    duFlowBean = JSONObject.parseObject(duFlowBeanJson, DUFlowBean.class);
                    log.debug("duFlowBeanJson：{},", duFlowBeanJson);
                }

            } else {
                if (jedis.get(requestId) != null) {
                    String duFlowBeanJson = jedis.get(requestId);
                    duFlowBean = JSONObject.parseObject(duFlowBeanJson, DUFlowBean.class);
                    log.debug("duFlowBeanJson：{},", duFlowBeanJson);
                }
            }
        } catch (Exception e) {
            resource.returnBrokenResource(jedis);
            MDC.put("sift", "redis");
            log.error(" jedis Exception :{}", e);
        } finally {
            resource.returnResource(jedis);
        }
        return duFlowBean;
    }

}

