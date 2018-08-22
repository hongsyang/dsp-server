package cn.shuzilm;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Redis {

    private  static JedisCluster jedisCluster;

    public Redis() {
        String redisString[] = {"172.17.129.116,7001", "172.17.129.116,7002", "172.17.129.116,7003", "172.17.129.116,7004", "172.17.129.116,7005", "172.17.129.116,7006"};
      //  String redisString = "redisCluster=192.168.100.46:30601,192.168.100.46:30602,192.168.100.46:30603,192.168.100.46:30604,192.168.100.46:30605,192.168.100.46:30606";

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();

        //配置redis集群
        for (String host : redisString) {
            String[] detail = host.split(",");
            nodes.add(new HostAndPort(detail[0], Integer.parseInt(detail[1])));
        }

        jedisCluster = new JedisCluster(nodes);
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
