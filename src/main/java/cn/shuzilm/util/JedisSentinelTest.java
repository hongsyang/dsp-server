package cn.shuzilm.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class JedisSentinelTest {
    public static void main(String[] args) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);
        Set<String> sentinels = new HashSet<String>();
        sentinels.add("192.168.1.241:7001");
        sentinels.add("192.168.1.241:7002");
        sentinels.add("192.168.1.241:7003");
        Jedis jedis =new Jedis("192.168.1.241",7001);
        jedis.set("hello", "world");
        String value = jedis.get("hello");
        System.out.println(format + ' ' + value);
       /* JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("mymaster", sentinels);
        while (true) {
            Thread.sleep(1000);

            try {
                jedis = jedisSentinelPool.getResource();


                jedis.set("hello", "world");
                String value = jedis.get("hello");
                System.out.println(format + ' ' + value);

            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (jedis != null)
                    try {
                        jedis.close();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
            }
        }*/

    }
}
