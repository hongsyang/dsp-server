package cn.shuzilm;

import cn.shuzilm.util.AsyncRedisClient;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Redis {

    private  static JedisCluster jedisCluster;

    public Redis() {
        String redisString[] = {"192.168.200.201,7001", "192.168.200.201,7002", "192.168.200.201,7003", "192.168.200.201,7004", "192.168.200.201,7005", "192.168.200.201,7000"};

        AsyncRedisClient instance = AsyncRedisClient.getInstance(redisString);
        System.out.println(instance);
//        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
//
//        //配置redis集群
//        for (String host : redisString) {
//            String[] detail = host.split(",");
//            nodes.add(new HostAndPort(detail[0], Integer.parseInt(detail[1])));
//        }
//
//        jedisCluster = new JedisCluster(nodes);
    }

    /**
     * 获取redis中指定key的值，value类型为String的使用此方法
     */
    public String get(String key) {
        return jedisCluster.get(key);
    }

    /**
     * 设置redis中指定key的值，value类型为String的使用此方法
     */
    public void set(String key, String value) {
        jedisCluster.set(key, value);
    }

    /**
     * 获取redis中指定key的值,对应的value，value类型为MAP的使用此方法
     */
    public Map<String, String> getMap(String key) {
        return jedisCluster.hgetAll(key);
    }

    /**
     * 删除redis中指定key的值项
     */
    public void del(String key) {
        jedisCluster.del(key);
    }

    public static void main(String args[]) {
        new Redis();
        jedisCluster.hset("aa","houkp","22");
        jedisCluster.hset("aa","houkp1","221");
        jedisCluster.hset("aa","houkp2","222");
        jedisCluster.hset("aa","houkp3","223");
        jedisCluster.hset("ab","houkp4","224");
        String hget = jedisCluster.hget("aa", "houkp");
        Map<String, String> aa = jedisCluster.hgetAll("ab");
        System.out.println(aa);
        System.out.println(hget);
        String value = new Redis().get("CompanyTenantID_10005");
        System.out.println(value);
    }
}
