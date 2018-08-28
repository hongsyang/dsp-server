package cn.shuzilm.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSON;

import cn.shuzilm.backend.rtb.RtbConstants;
import cn.shuzilm.bean.dmp.TagBean;
import cn.shuzilm.common.Constants;

/**
 * Created by thunders on 2018/7/24.
 * https://github.com/lettuce-io/lettuce-core
 * 异步同时获取 IP 黑名单和 设备标签~~
 *
 * https://github.com/lettuce-io/lettuce-core/wiki/Asynchronous-API
 */
public class AsyncRedisClient {
    private StatefulRedisClusterConnection<String, String> connection = null;

    public AsyncRedisClient(String[] nodes){
        ArrayList<RedisURI> nodeList = new ArrayList<>();
        for(String node : nodes){
            String[] nodeArr = node.split(",");
            RedisURI nodeUri = RedisURI.create(nodeArr[0], Integer.parseInt(nodeArr[1]));
            nodeList.add(nodeUri);
        }
        RedisClusterClient clusterClient = RedisClusterClient.create(nodeList);
        connection = clusterClient.connect();
    }
    
    

    public static void main(String[] args) {
//        RedisClient client = RedisClient.create("redis://192.168.1.241");
//        RedisAsyncCommands<String, String> commands = client.connect().async();
//        commands.hset("AA","age","18");
//        commands.hset("AA","income","5000");
//
//        RedisFuture future = commands.get("AA");
////        RedisFuture<String> futureSet =  commands.set("","");
//
//        String value = null;
//        try {
//
//            Object obj = future.get();
//            System.out.println(obj);
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
    	
    	
    	TagBean tagBean = new TagBean();
		tagBean.setTagId(123);
		float[] work = { 11.11f, 22.22f };
		float[] residence = { 33.11f, 44.22f };
		float[] activity = { 55.11f, 66.22f };
		tagBean.setWork(work);
		tagBean.setResidence(residence);
		tagBean.setActivity(activity);

		tagBean.setProvinceId(6);
		tagBean.setCityId(62);
		tagBean.setCountyId(737);

		tagBean.setIncomeId(2);
		tagBean.setAppPreferenceIds("eat food");
		tagBean.setPlatformId(1);
		tagBean.setBrand("335");
		tagBean.setPhonePrice(3);
		tagBean.setNetworkId(2);
		tagBean.setCarrierId(4);
		tagBean.setAppPreferenceId("app");
		tagBean.setTagIdList("2,3");
		tagBean.setCompanyIdList("123,321,222");
		
		String ss = JSON.toJSONString(tagBean);
		String nodeStr = RtbConstants.getInstance().getRtbStrVar(RtbConstants.REDIS_CLUSTER_URI);
		String nodes [] = nodeStr.split(";");
    	AsyncRedisClient redis = new AsyncRedisClient(nodes);
    	RedisAdvancedClusterAsyncCommands<String, String> commands = redis.connection.async();
//    	//commands.hset("3D8A278F33E4F97181DF1EAEFE500D06","test", ss);
    	commands.set("DC7D41E352D13D60765414D53F40BC25", ss);
    	
    	String s = redis.getAsync("DC7D41E352D13D60765414D53F40BC25");
    	System.out.println(s);
    	
       /* String[] urls = new String[]{"192.168.1.241","101.200.56.200"};
        AsyncRedisClient client = new AsyncRedisClient(urls,6379);
        String key1 = "180.212.6.151";
        String key2 = "113.88.87.72";
        Object[] objects = client.get(key1,key2);
        System.out.println(objects[0] + " " + objects[1]);*/
    }

    public Object[] getAsyncDouble(String deviceId,String ip ){
        RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
        RedisFuture<String> future1 = commands.get(deviceId);
        RedisFuture<String> future2 = commands.get(ip);
        try{
            String value = future1.get(100, TimeUnit.MILLISECONDS );
            String value2 = future2.get(100, TimeUnit.MILLISECONDS );
            return new Object[]{value,value2};
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public String getAsync(String deviceId){
        RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
        RedisFuture<String> future1 = commands.get(deviceId);
        String value = null;
        try {
            value = future1.get(100000, TimeUnit.MILLISECONDS );//超时时间修改为100秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return value;
    }

}
