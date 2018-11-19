package cn.shuzilm.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    private static AsyncRedisClient redisClient = null;
    
    public static AsyncRedisClient getInstance(String[] nodes){
    	if(redisClient == null){
    		redisClient = new AsyncRedisClient(nodes);
    	}
    	return redisClient;
    }

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
		double[] work = { 116.355617f, 39.981743f };
		double[] residence = { 116.355617f, 39.981743f };
		double[] activity = { 116.355617f, 39.981743f };
		tagBean.setWork(work);
		tagBean.setResidence(residence);
		tagBean.setActivity(activity);

		tagBean.setProvinceId(2);
		tagBean.setCityId(36);
		tagBean.setCountyId(381);

		tagBean.setIncomeId(2);
		tagBean.setAppPreferenceIds("eat food");
		tagBean.setPlatformId(1);
		tagBean.setBrand("335");
		tagBean.setPhonePrice(3);
		tagBean.setNetworkId(2);
		tagBean.setCarrierId("4");
		tagBean.setTagIdList("3_3");
		tagBean.setCompanyIdList("2_1133091,2,2_1196028");
		
		String ss = JSON.toJSONString(tagBean);
		String nodeStr = RtbConstants.getInstance().getRtbStrVar(RtbConstants.REDIS_CLUSTER_URI);
		System.out.println(nodeStr);
		String nodes [] = nodeStr.split(";");
    	AsyncRedisClient redis1 = AsyncRedisClient.getInstance(nodes);
    	
    	//Map<String,String> map = new HashMap<String,String>();
//    	for(int i=0;i<10000;i++){
//    		map.put(""+i, ""+(i+5));
//    	}
    	//redis1.setHMAsync("geoTest", map);
    	//commands.hmset("geoTest", map);
//    	Map<String,String> future = redis1.getHMAsync("geoTest");
//    	try {
//			System.out.println(future.get("2"));
//			//116.6403_22.8523_1000
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	 redis1.setAsync("a24e0e337853d4d9da28769d4bf83577", ss);
    	redis1.delAsync("a24e0e337853d4d9da28769d4bf83577");
//    	String json = redis1.getAsync("00000e3de16c8a7e80d2cca6976fafcf");
//    	TagBean tagBean1 = JSON.parseObject(json, TagBean.class);
    	
    	 System.out.println(redis1.getAsync("a24e0e337853d4d9da28769d4bf83577"));
    	 
    	 
//    	 String tagJson = redis1.getAsync("a24e0e337853d4d9da28769d4bf83577");
// 		TagBean tagBean1 = JSON.parseObject(tagJson, TagBean.class);
// 		
// 		Set<String> audienceTagIdSet = null;
// 		String audienceTagIdStr = tagBean1.getAudienceTagIdList();
//		if(audienceTagIdStr != null){
//			String audienceTagIds[] = audienceTagIdStr.split(",");
//			List<String> audienceTagIdList = Arrays.asList(audienceTagIds);
//			audienceTagIdSet = new HashSet<String>(audienceTagIdList);
//		}
//		System.out.println(audienceTagIdSet);
    	//commands.set("97C304E-4C8E-4872-8666-03FE67DC15DG", ss);	
    	//System.out.println(commands.get("97C304E-4C8E-4872-8666-03FE67DC15DG"));
    	//commands.set("a", "b");
    	//System.out.println(commands.get("a"));
    	//String s = redis.getAsync("116.6403_22.8523_1000");
    	
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

    public void setAsync(String key,String value){
    	RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
    	commands.set(key, value);
    }
    
    public void delAsync(String key){
    	RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
    	commands.del(key);
    }
    
    public void setHAsync(String key,String field,String value){
    	RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
    	commands.hset(key, field, value);
    }
    
    public void setHMAsync(String key,Map<String,String> map){
    	RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
    	commands.hmset(key, map);
    }
    
    public Map<String,String> getHMAsync(String key){
    	RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
    	RedisFuture<Map<String,String>> future = commands.hgetall(key);
    	Map<String,String> map = null;
    	try {
			map = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    	
    	return map;
    }
    
    public String getHAsync(String key,String field){
    	RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
    	RedisFuture<String> future = commands.hget(key,field);
    	String value = null;
    	try {
			value = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
    	
    	return value;
    }
    
}
