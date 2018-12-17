package cn.shuzilm.rtb;

import cn.shuzilm.backend.master.TaskServicve;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.util.AsyncRedisClient;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import io.lettuce.core.ScoredValue;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/7/23.
 */
public class RtbFlowControlTest {

    private static AsyncRedisClient redis;
    private static final org.slf4j.Logger myLog = LoggerFactory.getLogger(RtbFlowControlTest.class);

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        for(int i=0;i<100;i++) {
            System.out.println(i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 待测试：
     * 1. 过期机制
     * 2. 封装 pix set数据的方法
     * 2. 全流程跑一遍
     * @param args
     */

    public static void main(String[] args) throws InterruptedException {

        String nodeStr = "172.17.129.121,7001;172.17.129.121,7002;172.17.129.122,7003;172.17.129.122,7004;172.17.129.123,7005;172.17.129.123,7006";
        String nodes [] = nodeStr.split(";");
        redis = AsyncRedisClient.getInstance(nodes);

    /*    Double a = new Double(1D);
        int b = 1;
        System.out.println(a == b);*/

        /*redis.delAsync("078d9598-cee1-44a3-a6fd-526b84bcd4a6_DAYLY");
        redis.delAsync("31b3c292-a19d-4747-bf0e-9e012aa60902_DAYLY");
        redis.delAsync("48146b7a-632f-4d2b-98a9-1339612bc0b0_DAYLY");

        redis.delAsync("078d9598-cee1-44a3-a6fd-526b84bcd4a6_HOURLY");
        redis.delAsync("31b3c292-a19d-4747-bf0e-9e012aa60902_HOURLY");
        redis.delAsync("48146b7a-632f-4d2b-98a9-1339612bc0b0_HOURLY");*/

        //Double d = redis.zIncrbyAsync("078d9598-cee1-44a3-a6fd-526b84bcd4a6"+REDIS_KEY_POSTFIX_DAILY, 0, "test");
        //System.out.println(d);


        /*List<String> deviceIds = redis.zRangeByScoreAsync("adfanfasdf", 0, -1);
        System.out.println(deviceIds);*/

        // 加载广告
        pullAdFromDB();
        // 广告投放限制
        adLimit();
        // 模拟曝光
        //expOrClick();
        // 更新超限Map
        updateDeviceLimitMap();
        // 打印redis 数据
        printRedis();
        // 打印超限map
        printMap();


      /*  expOrClick();
        updateDeviceLimitMap();
        printMap();
        adLimit();*/



        // 初始化mapAd
        /*HashMap<String,AdBean> mapAd = new HashMap();
        for(int i=0;i<20;i++) {
            AdBean adBean = new AdBean();
            adBean.setAdUid(i+"");
            adBean.setFrqDaily(i);
            mapAd.put(i+"",adBean);
        }

        String redisKey = "DEVICE_LIMIT_TEST";
        String nodeStr = "172.17.129.121,7001;172.17.129.121,7002;172.17.129.122,7003;172.17.129.122,7004;172.17.129.123,7005;172.17.129.123,7006";
        String nodes [] = nodeStr.split(";");
        AsyncRedisClient redis = AsyncRedisClient.getInstance(nodes);

        //Double newscore = redis.zIncrbyAsync(redisKey, 1, "a");
        //redis.expire(redisKey,10);
        //redis.zIncrbyAsync(redisKey, 2, "b");
        //redis.zIncrbyAsync(redisKey, 3, "c");

        redis.zRangeWithScoreAsync(redisKey, 0, -1).forEach((score) -> {
            System.out.println(score.getValue() + " : " + score.getScore());
        });

        for(int i=0;i<10;i++) {
            System.out.println("-------------  "+ i +"  -------------");
            redis.zRangeByScoreAsync(redisKey, i, 10000000).forEach((deviceId) -> {
                System.out.print(deviceId + "  ");
            });
            System.out.println("");
        }
        Thread.sleep(15000);
        redis.zRangeWithScoreAsync(redisKey, 0, -1).forEach((score) -> {
            System.out.println(score.getValue() + " : " + score.getScore());
        });*/
    }

    private static ConcurrentHashMap<String, AdBean> mapAd = new ConcurrentHashMap<>();
    private static TaskServicve taskService = new TaskServicve();


    /* 广告超投设备 */
    private static HashMap<String,HashSet<String>> deviceLimitMapDaiyly = new HashMap<>();
    private static HashMap<String,HashSet<String>> deviceLimitMapHourly = new HashMap<>();

    private static final String REDIS_KEY_POSTFIX_DAILY = "_DAYLY";
    private static final String REDIS_KEY_POSTFIX_HOURLY = "_HOURLY";

    private static final int DEFAULT_LIMIT_DAILY = 20;
    private static final int DEFAULT_LIMIT_HOURLY = 5;

    /* end 广告超投设备 */
    public static void updateDeviceLimitMap() {
        MDC.put("sift", "rtb");
        myLog.info("开始更新广告超投设备");
        // 删除失效的广告数据
        Iterator<String> iterator = deviceLimitMapDaiyly.keySet().iterator();
        while (iterator.hasNext()) {
            String adId = iterator.next();
            // 清除掉 deviceLimitMap 中不参与投放的广告
            if(!mapAd.contains(adId)) {
                iterator.remove();
                myLog.info("删除不投放的广告： {}", adId);
            }
        }


        // 更新广告超投设备
        mapAd.forEach((adUid,adBean) -> {
            try{
                // 获取广告设置的投放频率限制
                Integer frqDaily = adBean.getFrqDaily() == 0 ? DEFAULT_LIMIT_DAILY : adBean.getFrqDaily();
                Integer frqHourly = adBean.getFrqHour() == 0 ? DEFAULT_LIMIT_HOURLY : adBean.getFrqHour();

                // 更新超投设备 Map
                myLog.info("开始更新天的超投 Map");
                updateDeviceLimitMap(adUid, frqDaily, deviceLimitMapDaiyly, REDIS_KEY_POSTFIX_DAILY);

                myLog.info("开始更新小时的超投 Map");
                updateDeviceLimitMap(adUid, frqHourly, deviceLimitMapHourly, REDIS_KEY_POSTFIX_HOURLY);

            }catch (Exception e) {
                myLog.error("更新广告超投设备出错，广告id： " + adUid ,e);
            }
        });
    }


    /**
     * 单个设备频率控制 —— 更新超投设备 Map
     * @param adUid     广告id
     * @param frq       频率限制
     * @param limitMap  超投设备Map
     * @param postfix   redis key 后缀：
     *                  (1). 按天的累计曝光次数，常量： REDIS_KEY_POSTFIX_DAILY
     *                  (2). 按小时的累计曝光次数，常量： REDIS_KEY_POSTFIX_HOURLY
     */
    private static void updateDeviceLimitMap (String adUid, int frq, HashMap<String,HashSet<String>> limitMap, String postfix) {
        HashSet<String> deviceIdSets = new HashSet();
        List<String> deviceIds = redis.zRangeByScoreAsync(adUid + postfix, frq, 100000000);
        // 为了快速随机读取，将list转成HashSet
        deviceIds.forEach((value) -> {
            deviceIdSets.add(value);
        });
        if(deviceIds != null && deviceIds.size() > 0) {
            limitMap.put(adUid, deviceIdSets);
            myLog.info("更新: 广告 {}  超投设备 {}", adUid, deviceIdSets.toString());
        }else {
            // 如果没有找到超投设备，则从map中移除
            limitMap.remove(adUid);
            myLog.info("移除: 广告 {}", adUid);
        }
        // myLog.info("更新: 广告 {}  超投设备 {}", adUid, deviceIdSets.toString());
    }

    public static void incrDeviceExpOrClickTime(String adUid,int amount, String imeiMd5) {

        ZoneOffset zone = ZoneOffset.of("+8");
        LocalDateTime now = LocalDateTime.now();
        long epochSecond = now.toEpochSecond(zone);
        LocalDateTime expiredTime = null;
        long time = 0L;
        Double d = redis.zIncrbyAsync(adUid+REDIS_KEY_POSTFIX_DAILY, amount, imeiMd5);
        if (d == amount) {
            // 计算到当天24点的秒数
            expiredTime = LocalDateTime.of(now.getYear(),now.getMonth(),now.getDayOfMonth(), 23, 59, 59);
            time = expiredTime.toEpochSecond(zone) - epochSecond;
            redis.expire(adUid+REDIS_KEY_POSTFIX_DAILY, time);
        }
        Double h = redis.zIncrbyAsync(adUid+REDIS_KEY_POSTFIX_HOURLY, amount, imeiMd5);
        if (h == amount) {
            // 计算到前小时结束的秒数
            expiredTime = LocalDateTime.of(now.getYear(),now.getMonth(),now.getDayOfMonth(), now.getHour(), 59, 59);
            time = expiredTime.toEpochSecond(zone) - epochSecond;
            redis.expire(adUid+REDIS_KEY_POSTFIX_HOURLY, time);
        }
    }

    public static  void pullAdFromDB(){
        MDC.put("sift", "pixel");
        try {
            ResultList adList = taskService.queryAllAd();
            for(ResultMap map:adList){
                try{
                    AdBean ad = new AdBean();
                    ad.setAdUid(map.getString("uid"));
                    String adverUid = map.getString("advertiser_uid");
                    AdvertiserBean adver = taskService.queryAdverByUid(adverUid);
                    ad.setAdvertiser(adver);
                    String mode = map.getString("mode");
                    ad.setMode(mode);
                    if("cpc".equalsIgnoreCase(mode)){
                        ad.setPrice(map.getBigDecimal("price").floatValue() * 0.03f * 1000);
                    }else{
                        ad.setPrice(map.getBigDecimal("price").floatValue());
                    }
                    ad.setFrqDaily(map.getInteger("frq_daily"));
                    mapAd.put(ad.getAdUid(), ad);

                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
            mapAd.remove("078d9598-cee1-44a3-a6fd-526b84bcd4a6");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 测试用 模拟广告曝光
     */

    public static void expOrClick(){
        int i = 0;
        for(String adUid : mapAd.keySet()) {
            incrDeviceExpOrClickTime(adUid, (int)(Math.random() * 10 + 1), "test");
            incrDeviceExpOrClickTime(adUid, (int)(Math.random() * 10 + 1), (i++) + "");
        }

    }


    public static void adLimit(){
        int i = 0;
        System.out.println("----------------- 广告投放限制 --------------------");
        for(String adUid : mapAd.keySet()) {
            int dayly = mapAd.get(adUid).getFrqDaily() == 0 ? 20 : mapAd.get(adUid).getFrqDaily();
            int hourly = mapAd.get(adUid).getFrqHour() == 0 ? 5 : mapAd.get(adUid).getFrqHour();
            System.out.println(adUid + "    day: " + dayly + "  hour: " + hourly);
        }
        System.out.println("----------------- 广告投放限制 --------------------");

    }

    public static void printMap (){
        System.out.println("----------------- deviceLimitMapDaiyly --------------------");
        deviceLimitMapDaiyly.forEach((adUid,sets) -> {
            System.out.println(adUid + " " + sets.toString());
        });
        System.out.println("----------------- deviceLimitMapDaiyly --------------------");

        System.out.println("----------------- deviceLimitMapHourly --------------------");
        deviceLimitMapHourly.forEach((adUid,sets) -> {
            System.out.println(adUid + " " + sets.toString());
        });
        System.out.println("----------------- deviceLimitMapHourly --------------------");
    }

    public static void printRedis () {
        System.out.println("----------------- Dayly Redis --------------------");
        for(String adUid : mapAd.keySet()) {
            // 测试用 ： 打印redis中 的数据
            String key = adUid+REDIS_KEY_POSTFIX_DAILY;
            List<ScoredValue<String>> list = redis.zRangeWithScoreAsync(key,0, -1);
            System.out.println(key + " : " + redis.ttl(key));
            for(ScoredValue<String> scoredValue : list) {
                System.out.print( " value: "+scoredValue.getValue() + "  score: " +  scoredValue.getScore());
            }
            System.out.println("\n");
        }
        System.out.println("----------------- Dayly Redis --------------------");

        System.out.println("----------------- Hourly Redis --------------------");
        for(String adUid : mapAd.keySet()) {
            // 测试用 ： 打印redis中 的数据
            String key = adUid+REDIS_KEY_POSTFIX_HOURLY;
            List<ScoredValue<String>> list = redis.zRangeWithScoreAsync(key,0, -1);
            System.out.println(key + " : " + redis.ttl(key));
            for(ScoredValue<String> scoredValue : list) {
                System.out.print( " value: "+scoredValue.getValue() + "  score: " +  scoredValue.getScore());
            }
            System.out.println("\n");
        }
        System.out.println("----------------- Hourly Redisc --------------------");

    }

    public static void printResult () {
        for (String adUid : mapAd.keySet()) {
            int dayly = mapAd.get(adUid).getFrqDaily() == 0 ? 20 : mapAd.get(adUid).getFrqDaily();
            int hourly = mapAd.get(adUid).getFrqHour() == 0 ? 5 : mapAd.get(adUid).getFrqHour();

            System.out.println(adUid + "投放限制： （天："+dayly+"  小时： " + hourly + "）");
            System.out.println("Redis: ");
            String key = adUid+REDIS_KEY_POSTFIX_DAILY;
            List<ScoredValue<String>> list = redis.zRangeWithScoreAsync(key,0, -1);
            System.out.println(key + " : " + redis.ttl(key));
            for(ScoredValue<String> scoredValue : list) {
                System.out.print( " value: "+scoredValue.getValue() + "  score: " +  scoredValue.getScore());
            }
        }
    }
}

