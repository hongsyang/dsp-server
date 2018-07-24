package cn.shuzilm.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by thunders on 2018/7/24.
 * https://github.com/lettuce-io/lettuce-core
 * 异步同时获取 IP 黑名单和 设备标签~~
 *
 * https://github.com/lettuce-io/lettuce-core/wiki/Asynchronous-API
 *
 */
public class AsyncRedisClient {
    private StatefulRedisClusterConnection<String, String> connection = null;
    public AsyncRedisClient(String[] nodes,int port){
        ArrayList<RedisURI> nodeList = new ArrayList<>();
        for(String node : nodes){
            RedisURI nodeUri = RedisURI.create(node, port);
            nodeList.add(nodeUri);
        }

        RedisClusterClient clusterClient = RedisClusterClient.create(nodeList);
        connection = clusterClient.connect();

    }

    public static void main(String[] args) {
        String[] urls = new String[]{"192.168.1.241","101.200.56.200"};
        AsyncRedisClient client = new AsyncRedisClient(urls,6379);
        String key1 = "180.212.6.151";
        String key2 = "113.88.87.72";
        Object[] objects = client.get(key1,key2);
        System.out.println(objects[0] + " " + objects[1]);
    }

    public Object[] get(String key1 , String key2){
        RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
        RedisFuture<String> future1 = commands.get(key1);
        RedisFuture<String> future2 = commands.get(key2);
        try{
            String value = future1.get(100, TimeUnit.MILLISECONDS );
            String value2 = future2.get(100, TimeUnit.MILLISECONDS );
            return new Object[]{value,value2};
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }



}
