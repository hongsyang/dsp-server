package cn.shuzilm.util;

import io.lettuce.core.*;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.alibaba.fastjson.JSON;

import cn.shuzilm.backend.rtb.RtbConstants;
import cn.shuzilm.backend.rtb.RuleMatching;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(AsyncRedisClient.class);
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
    	MDC.put("sift", "rtb");
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
		
		tagBean.setDemographicProvinceId(3);
		tagBean.setDemographicCityId(37);
		tagBean.setDemographicCountyId(395);

		tagBean.setProvinceId(2);
		tagBean.setCityId(36);
		tagBean.setCountyId(381);

		tagBean.setIncomeId(2);
		tagBean.setAppPreferenceIds("4,25");
		tagBean.setPlatformId(1);
		tagBean.setBrand("335");
		tagBean.setPhonePrice(3);
		tagBean.setNetworkId(2);
		tagBean.setCarrierId("3");
		tagBean.setTagIdList("3_3");
		tagBean.setCompanyIdList("1_18106");
		tagBean.setIp("49.5.2.83");
		
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
//    	 redis1.setAsync("5e8657a04ff75703f4fa9a3f95d106e1", ss);
 //   	redis1.delAsync("a24e0e337853d4d9da28769d4bf83577");
//    	String json = redis1.getAsync("00000e3de16c8a7e80d2cca6976fafcf");
//    	TagBean tagBean1 = JSON.parseObject(json, TagBean.class);
//    	while(true){   	
//    	 try {
//    		 long startTime = System.currentTimeMillis();
        	 System.out.println(redis1.getAsync("040041d1482718ea1bce9a664e3b5f61"));
//        	 LOG.info("读取耗时:"+(System.currentTimeMillis()-startTime));
//			Thread.sleep(5 * 1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	}
    	    	 
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
            value = future1.get(30, TimeUnit.MILLISECONDS );//超时时间修改为100秒
        } catch (Exception e) {
            return null;
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


	public List<String> zRangeByScoreAsync(String key, int start, int end){
		RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
		Range<Integer> range = Range.create(start,end);
		RedisFuture<List<String>> future = commands.zrangebyscore(key, range);
		List<String> value = null;
		try {
			value = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return value;
	}

	public List<ScoredValue<String>> zRangeWithScoreAsync(String key, long start, long end){
		RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
		RedisFuture<List<ScoredValue<String>>> future = commands.zrangeWithScores(key,start, end);
		List<ScoredValue<String>> value = null;
		try {
			value = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return value;
	}

	public Double zIncrbyAsync(String key, long amount, String member){
		RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
		RedisFuture<Double> future = commands.zincrby(key, amount, member);
		Double value = null;
		try {
			value = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return value;
	}

	public void expire(String key,long seconds){
		RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
		commands.expire(key,seconds);
	}

	public Long ttl(String key){

		RedisAdvancedClusterAsyncCommands<String, String> commands = connection.async();
		RedisFuture<Long> future = commands.ttl(key);
		Long value = null;
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
