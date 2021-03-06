package cn.shuzilm.common.jedis;

import cn.shuzilm.common.AppConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashMap;


/**
 * jedis管理
 * User: Administrator
 * Date: 18-7-23
 * Time: 上午10:39
 * To change this template use File | Settings | File Templates.
 */
public class JedisManager {
    private static final Logger log = LoggerFactory.getLogger(JedisManager.class);

    private AppConfigs configs = null;

    private static final Object lock = new Object();

    private static final Object lock2 = new Object();

    private static final String FILE_REDIS_CONFIG = "configs_redis.properties";

    private static HashMap<String,JedisManager> instanceMap = new HashMap<String,JedisManager>();
    private static volatile JedisManager ourInstance = null;


    private static final int REDIS_POOL_DEFAULT_TIMEOUT = 20000;

    private  static JedisPool jedisPool;

    private JedisManager(String configFileName) {
        this.configs = AppConfigs.getInstance(configFileName);
        // load config files for redis configuration
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(configs.getInt("REDIS_POOL_MAX_ACTIVE"));
        config.setMinIdle(configs.getInt("REDIS_POOL_MIN_IDLE"));
        config.setMaxIdle(configs.getInt("REDIS_POOL_MAX_IDLE"));
        config.setMaxWaitMillis(configs.getInt("REDIS_POOL_MAX_WAIT"));

        log.info( " REDIS_SERVER_HOST =" + configs.getString("REDIS_SERVER_HOST") + ",REDIS_SERVER_PORT = " + configs.getInt("REDIS_SERVER_PORT"));
        // init a jedis connection pool
        this.jedisPool = new JedisPool(config,configs.getString("REDIS_SERVER_HOST"),configs.getInt("REDIS_SERVER_PORT"),REDIS_POOL_DEFAULT_TIMEOUT);
    }
    public static JedisManager getInstance( String redisConfigName ) {
        if(!instanceMap.containsKey(redisConfigName)){
            JedisManager ins = null;
            synchronized (lock2) {
                ins = new JedisManager(redisConfigName);
                instanceMap.put(redisConfigName,ins);
            }
            return ins;
        }else
            return instanceMap.get(redisConfigName);
    }

    /**
     * Get a single instance
     *
     * @return The instance
     */
    public static JedisManager getInstance() {
        if (ourInstance == null) {
            synchronized (lock) {
                if (ourInstance == null) {
                    ourInstance = new JedisManager(FILE_REDIS_CONFIG);
                }
            }
        }
        return ourInstance;
    }

    /**
     * Get a source for jedis, if no more resouce exists, exception be throw
     *
     * @return A jedis client instance
     */
    public Jedis getResource() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
//            jedis.auth(configs.getString("REDIS_PASSWORD"));
//            jedis.select(configs.getInt("REDIS_DBNUM"));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to get jedis resource, reason is :" + e.getMessage());
        }
        return jedis;
    }

    /**
     * Get a source for jedis, if no more resouce exists, exception be throw
     *
     * @return A jedis client instance
     */
    public Jedis getResource(String configFileName) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.auth(configs.getString("REDIS_PASSWORD"));
            jedis.select(configs.getInt("REDIS_DBNUM"));
        } catch (Exception e) {
            log.error("Failed to get jedis resource, reason is :" + e.getMessage());
        }
        return jedis;
    }

    /**
     * Return a resource
     *
     * @param jedis
     */
    public void returnResource(Jedis jedis) {        
        	if(jedis != null){
        		jedis.close();
        	}
    }
}
