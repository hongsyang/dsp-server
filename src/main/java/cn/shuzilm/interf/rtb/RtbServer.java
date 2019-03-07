package cn.shuzilm.interf.rtb;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.redis.RedisManager;
import cn.shuzilm.interf.pixcel.parser.ParameterParser;
import cn.shuzilm.interf.rtb.parser.RequestService;
import cn.shuzilm.util.IpBlacklistUtil;
import io.netty.channel.EventLoopGroup;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @Author： Wang Haiting
 * @Version： May 6, 20132:48:04 PM
 **/
public class RtbServer {




    private static RuleMatching ruleMatching;

    private static JedisManager jedisManager;

    private static RedisManager redisManager;

    private static IpBlacklistUtil ipBlacklist;


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = null;

    private static final Logger log = LoggerFactory.getLogger(RtbServer.class);

    //主线程
    private EventLoopGroup bossGroup;
    //工作线程
    private EventLoopGroup workerGroup;
    //超时线程池
    private ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("RTB_EXECUTOR_THREADS"));

    //扫描包
    private static Reflections reflections = new Reflections("cn.shuzilm.interf.rtb.parser");
    //加载所有的实现接口的类
    private static Set<Class<? extends RequestService>> subTypesOf;
    //创建requestParser 解析的map
    private static ConcurrentHashMap<String, Object> requestParser = null;






    public static void main(String[] args) {
        try {
       configs = AppConfigs.getInstance(FILTER_CONFIG);
            //初始化 加载配置
            ipBlacklist = IpBlacklistUtil.getInstance();
            jedisManager = JedisManager.getInstance();
            redisManager=RedisManager.getInstance();
            ruleMatching = RuleMatching.getInstance();
            subTypesOf = reflections.getSubTypesOf(RequestService.class);
//            for (Class<? extends RequestService> aClass : subTypesOf) {
//                System.out.println(aClass.getName());
//            }
            requestParser = createMap(subTypesOf);

            RtbServer server = new RtbServer();
            server.start(configs.getInt("RTB_PORT"));

        } catch (Exception e) {
            log.error("", e);
        }

    }

    /**
     * 扫描宝加入实现类
     * @param subTypesOf
     * @return
     */
    private static ConcurrentHashMap<String,Object> createMap(Set<Class<? extends RequestService>> subTypesOf) {
        ConcurrentHashMap map = new ConcurrentHashMap();
        String oldChar = "class cn.shuzilm.interf.rtb.parser.";
        for (Object o : subTypesOf) {
            map.put(o.toString().replace(oldChar, "").toLowerCase().replace("requestserviceimpl", ""), o.toString().replace("class ", ""));
        }
        log.debug("map:{}" , map);
        return map;
    }


    public void start(int port) {

        log.debug("admin start on :{}", port);
    }


}