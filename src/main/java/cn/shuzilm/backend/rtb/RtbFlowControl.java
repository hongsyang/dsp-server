package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdBidBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.FlowTaskBean;
import cn.shuzilm.bean.control.Material;
import cn.shuzilm.bean.control.NodeStatusBean;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AreaBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.GpsGridBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.MathTools;
import cn.shuzilm.util.TimeUtil;
import cn.shuzilm.util.geo.GeoHash;
import cn.shuzilm.util.geo.GridMark;
import cn.shuzilm.util.geo.GridMark2;

import org.python.jline.internal.Log;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Created by thunders on 2018/7/17.
 */
public class RtbFlowControl {
    private static final org.slf4j.Logger myLog = LoggerFactory.getLogger(AdFlowControl.class);
    private static RtbFlowControl rtb = null;

    private SimpleDateFormat dateFm = new SimpleDateFormat("EEEE_HH");
    

    public static RtbFlowControl getInstance() {
        if (rtb == null) {
            rtb = new RtbFlowControl();
        }
        return rtb;
    }

    private RtbConstants constant;
    

    public ConcurrentHashMap<String, AdBean> getAdMap() {
        return mapAd;
    }

    public ConcurrentHashMap<String, List<String>> getMaterialMap() {
        return mapAdMaterial;
    }

    public ConcurrentHashMap<String, List<String>> getMaterialRatioMap() {
        return mapAdMaterialRatio;
    }
    
    public ConcurrentHashMap<String, Set<String>> getMaterialByRatioMap() {
        return mapMaterialRatio;
    }

    public ConcurrentHashMap<String, Set<String>> getAreaMap() {
        return areaMap;
    }

    public ConcurrentHashMap<String, Set<String>> getDemographicMap() {
        return demographicMap;
    }
    
    public ConcurrentHashMap<String,Long> getBidMap(){
    	return bidMap;
    }
    
    public ConcurrentHashMap<String,Long> getAdxFlowMap(){
    	return adxFlowMap;
    }
    
    public ConcurrentHashMap<String,Long> getAppFlowMap(){
    	return appFlowMap;
    }
    
    public ArrayList<AdBidBean> getBidList(){
    	return bidList;
    }

    public ConcurrentHashMap<String, FlowTaskBean> getMapFlowTask() {
		return mapFlowTask;
	}

	private String nodeName;
    /**
     * 广告资源管理 key: aduid : taskBean
     */
    private static ConcurrentHashMap<String, AdBean> mapAd = null;

    /**
     * 广告任务管理
     */
    private static ConcurrentHashMap<String, TaskBean> mapTask = null;
    
    private static ConcurrentHashMap<String, FlowTaskBean> mapFlowTask = null;
    /**
     * 广告资源的倒置 key: 广告类型 + 广告宽 + 广告高 value: list<aduid>
     */
    private static ConcurrentHashMap<String, List<String>> mapAdMaterial = null;

    /**
     * 广告资源的倒置 key: 广告类型 + (广告宽/广告高) value: list<aduid>
     */
    private static ConcurrentHashMap<String, List<String>> mapAdMaterialRatio = null;
    
    /**
     * 广告资源的倒置 key: 广告类型 + (广告宽/广告高) value: set<String>
     */
    private static ConcurrentHashMap<String, Set<String>> mapMaterialRatio = null;

    /**
     * 省级、地级、县级 map key: 北京市北京市海淀区 河北省廊坊地区广平县 value：aduid
     */
    private static ConcurrentHashMap<String, Set<String>> areaMap = null;

    private static ConcurrentHashMap<String, Set<String>> demographicMap = null;
    
    private static Map<String,String> redisGeoMap = null;

    // 判断标签坐标是否在 广告主的选取范围内
    private static HashMap<Integer, GridMark2> gridMap = null;
    
    //每个广告5秒内的rtb请求数量
    private static ConcurrentHashMap<String,Long> bidMap = null;
    
    //5秒内adx流量数
    private static ConcurrentHashMap<String,Long> adxFlowMap = null;
    
    //5秒内app流量数
    private static ConcurrentHashMap<String,Long> appFlowMap = null;
    
    private static ArrayList<AdBidBean> bidList = null;
    
    private static ConcurrentHashMap<String,Integer> weekAndDayNumMap = null;

    /* 广告超投设备 */
    private static HashMap<String,HashSet<String>> deviceLimitMapDaiyly = new HashMap<>();
    private static HashMap<String,HashSet<String>> deviceLimitMapHourly = new HashMap<>();

    private static final String REDIS_KEY_POSTFIX_DAILY = "_DAYLY";
    private static final String REDIS_KEY_POSTFIX_HOURLY = "_HOURLY";

    private static final int DEFAULT_LIMIT_DAILY = 20;
    private static final int DEFAULT_LIMIT_HOURLY = 5;


    private AsyncRedisClient redis;
    /* end 广告超投设备 */

    private RtbFlowControl() {
        MDC.put("sift", "rtb");
        nodeName = Constants.getInstance().getConf("HOST");
        mapAd = new ConcurrentHashMap<>();
        mapTask = new ConcurrentHashMap<>();
        mapFlowTask = new ConcurrentHashMap<>();
        areaMap = new ConcurrentHashMap<>();
        demographicMap = new ConcurrentHashMap<>();
        mapAdMaterial = new ConcurrentHashMap<>();
        mapAdMaterialRatio = new ConcurrentHashMap<>();
        mapMaterialRatio = new ConcurrentHashMap<>();
        bidMap = new ConcurrentHashMap<>();
        adxFlowMap = new ConcurrentHashMap<>();
        appFlowMap = new ConcurrentHashMap<>();
        bidList = new ArrayList<AdBidBean>();
        weekAndDayNumMap = new ConcurrentHashMap<String,Integer>();
        //redisGeoMap = new ConcurrentHashMap<>();
        // 判断标签坐标是否在 广告主的选取范围内
//        gridMap = new HashMap<>();
        constant = RtbConstants.getInstance();
        String nodeStr = constant.getRtbStrVar(RtbConstants.REDIS_CLUSTER_URI);
        String nodes [] = nodeStr.split(";");
        redis = AsyncRedisClient.getInstance(nodes);
    }

    public void trigger() {
        // 5 s
        pullAndUpdateTask();
        // 10分钟拉取一次最新的广告内容
        pullTenMinutes();
        // 1 hour
        refreshAdStatus();
        //5s上传一次rtb请求数
        pushAdBidNums();
        
        //10分钟上传一次RTB节点心跳
        pushRtbHeart();
        
        //1分钟上报一次ADX与APP流量数
        pushAdxAndAppFlow();
        updateDeviceLimitMap();

        //5秒钟获取一次流量任务
        pullAndUpdateFlowTask();
    }

    /**
     * 检查设备的标签所带的居住地、工作地、活动地坐标
     *
     * @param lng 0 不限 1 居住地 2 工作地 3 活动地：q
     * @param lat 0 不限 1 居住地 2 工作地 3 活动地
     * @return
     */
    public HashSet<String> checkInBound(double[] lng, double[] lat) {
        HashSet<String> allUidSet = new HashSet<>();
        for (int i = 0; i < lng.length; i++) {
            if (lng[i] != 0.0d) {
                ArrayList<String> uidList = gridMap.get(i).findGrid(lng[i], lat[i]);
                allUidSet.addAll(uidList);
            }
        }

        return allUidSet;
    }

    /**
     * 每隔 10 分钟更新一次广告素材或者人群包
     */
    public void pullTenMinutes() {
    	MDC.put("sift", "rtb");
        // 从 10 分钟的队列中获得广告素材和人群包
    	ArrayList<AdBean> adBeanList = MsgControlCenter.recvAdBean(nodeName);
//        ArrayList<GpsBean> gpsAll = new ArrayList<>();
//        ArrayList<GpsBean> gpsResidenceList = new ArrayList<>();
//        ArrayList<GpsBean> gpsWorkList = new ArrayList<>();
//        ArrayList<GpsBean> gpsActiveList = new ArrayList<>();
        if (adBeanList != null && !adBeanList.isEmpty()) {
        	areaMap.clear();
        	demographicMap.clear();
        	mapAdMaterial.clear();
        	mapAdMaterialRatio.clear();
        	mapMaterialRatio.clear();
            for (AdBean adBean : adBeanList) {
                // 广告ID
                String uid = adBean.getAdUid();

                mapAd.put(uid, adBean);
                List<AudienceBean> audienceList = adBean.getAudienceList();
                if (audienceList.size() == 0) {
                    myLog.error(adBean.getAdUid() + "\t" + adBean.getName() + " 没有设置人群包..");
                }
//                ArrayList<GpsBean> gpsList = null;
                for (AudienceBean audience : audienceList) {
                    if (audience != null) {
                        //加载人群中的GEO位置信息
//                        gpsList = audience.getGeoList();
//                        if (gpsList != null) {
//                            for (GpsBean gps : gpsList) {
//                                gps.setPayload(uid);
//                            }
//                            switch (audience.getMobilityType()) {
//                                case 0:
//                                    gpsResidenceList.addAll(gpsList);
//                                    gpsWorkList.addAll(gpsList);
//                                    gpsActiveList.addAll(gpsList);
//                                    break;
//                                case 1://居住地
//                                    // 将 经纬度坐标装载到 MAP 中，便于快速查找
//                                    gpsResidenceList.addAll(gpsList);
//                                    break;
//                                case 2://工作地
//                                    gpsWorkList.addAll(gpsList);
//                                    break;
//                                case 3://活动地
//                                    // 将 经纬度坐标装载到 MAP 中，便于快速查找
//                                    gpsActiveList.addAll(gpsList);
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
                            // 将 省、地级、县级装载到 MAP 中，便于快速查找
                            List<AreaBean> areaList = audience.getCityList();
                            String key = null;
                            if(areaList == null){
                            	continue;
                            }
                            for (AreaBean area : areaList) {
                                if (area.getProvinceId() == 0) {
                                    // 当省选项为 0 的时候，则认为是匹配全国
                                    key = "china";
                                } else if (area.getCityId() == 0) {
                                    // 当市级选项为 0 的时候，则认为是匹配全省
                                    key = area.getProvinceId() + "";
                                } else if (area.getCountyId() == 0) {
                                    // 当县级选项为 0 的时候，则认为是匹配全市
                                    key = area.getProvinceId() + "_" + area.getCityId();
                                } else {
                                    key = area.getProvinceId() + "_" + area.getCityId() + "_" + area.getCountyId();
                                }

                                if (!areaMap.containsKey(key)) {
                                    Set<String> set = new HashSet<String>();
                                    set.add(adBean.getAdUid());
                                    areaMap.put(key, set);
                                } else {
                                    Set<String> set = areaMap.get(key);
                                    set.add(adBean.getAdUid());
                                }
                            }
                            
                            Set<AreaBean> areaSet = audience.getDemographicCitySet();
                            String demoKey = null;
                            if(areaSet == null){
                            	continue;
                            }
                            for (AreaBean area : areaSet) {
                                if (area.getProvinceId() == 0) {
                                    // 当省选项为 0 的时候，则认为是匹配全国
                                	demoKey = "china";
                                } else if (area.getCityId() == 0) {
                                    // 当市级选项为 0 的时候，则认为是匹配全省
                                	demoKey = area.getProvinceId() + "";
                                } else if (area.getCountyId() == 0) {
                                    // 当县级选项为 0 的时候，则认为是匹配全市
                                	demoKey = area.getProvinceId() + "_" + area.getCityId();
                                } else {
                                	demoKey = area.getProvinceId() + "_" + area.getCityId() + "_" + area.getCountyId();
                                }

                                if (!demographicMap.containsKey(demoKey)) {
                                    Set<String> set = new HashSet<String>();
                                    set.add(adBean.getAdUid());
                                    demographicMap.put(demoKey, set);
                                } else {
                                    Set<String> set = demographicMap.get(demoKey);
                                    set.add(adBean.getAdUid());
                                }
                            }
                        }
                }
                    // 广告内容的更新 ，按照素材的类型和尺寸
                    CreativeBean creative = adBean.getCreativeList().get(0);
                    List<Material> materialList = creative.getMaterialList();
                    for (Material material : materialList) {
                        int width = material.getWidth();
                        int height = material.getHeight();
                        String materialUid = material.getUid();
                        int divisor = MathTools.division(width, height);
//                        String materialKey = creative.getType() + "_" + width + "_" + +height;
//                        String materialRatioKey = creative.getType() + "_" + width / divisor + "/" + height / divisor;
                        
                        String materialKey = width + "_" +height;
                        String materialRatioKey = width / divisor + "/" + height / divisor;

                        if (!mapAdMaterial.containsKey(materialKey)) {
                            List<String> uidList = new ArrayList<String>();
                            uidList.add(uid);
                            mapAdMaterial.put(materialKey, uidList);
                        } else {
                            List<String> uidList = mapAdMaterial.get(materialKey);
                            if (!uidList.contains(uid)) {
                                uidList.add(uid);
                            }
                        }

                        if (!mapAdMaterialRatio.containsKey(materialRatioKey)) {
                            List<String> uidList = new ArrayList<String>();
                            uidList.add(uid);
                            mapAdMaterialRatio.put(materialRatioKey, uidList);
                        } else {
                            List<String> uidList = mapAdMaterialRatio.get(materialRatioKey);
                            if (!uidList.contains(uid)) {
                                uidList.add(uid);
                            }
                        }
                        
                        if (!mapMaterialRatio.containsKey(materialRatioKey)) {
                            Set<String> uidSet = new HashSet<String>();
                            uidSet.add(materialUid);
                            mapMaterialRatio.put(materialRatioKey, uidSet);
                        } else {
                            Set<String> uidSet = mapMaterialRatio.get(materialRatioKey);
                            if (!uidSet.contains(materialUid)) {
                            	uidSet.add(materialUid);
                            }
                        }
                    }
            }
//                gridMap.clear();
//                // 将 GPS 坐标加载到 栅格快速比对处理类中
//                gridMap.put(0, new GridMark2(gpsResidenceList,redisGeoMap));
//                gridMap.put(1, new GridMark2(gpsWorkList,redisGeoMap));
//                gridMap.put(2, new GridMark2(gpsActiveList,redisGeoMap));
                
                myLog.info("广告共计加载条目数 : " + adBeanList.size());
                //myLog.info("广告中的经纬度坐标共计条目数：" + gpsAll.size());

        }

    }

    /**
     * 每隔 5 秒钟从消息中心获得当前节点的当前任务，并与当前两个 MAP monitor 进行更新 不包括素材
     */
    public void pullAndUpdateTask() {
    	MDC.put("sift", "rtb");
    	List<TaskBean> taskList = MsgControlCenter.recvTask(nodeName);
        if (taskList == null || taskList.size() == 0) {
            return;
        }
        for(TaskBean task:taskList){
            // 把最新的任务更新到 MAP task 中
            mapTask.put(task.getAdUid(), task);
        }
        
        Iterator iter = mapTask.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, TaskBean> entry = (Map.Entry) iter.next();
			String adUid = entry.getKey();
			TaskBean task = entry.getValue();
			if(task.getCommand() == TaskBean.COMMAND_STOP){
				mapAd.remove(adUid);
				iter.remove();
				myLog.info("广告ID["+adUid+"]停止,被移除!");
			}
		}
    }
    
    
    /**
     * 每隔5秒钟获取流量任务
     */
    public void pullAndUpdateFlowTask(){
    	MDC.put("sift", "rtb");
    	List<FlowTaskBean> flowTaskList = MsgControlCenter.recvFlowTask(nodeName);
    	if(flowTaskList == null || flowTaskList.isEmpty()){
    		return;
    	}
    	for(FlowTaskBean flowTask:flowTaskList){
    		mapFlowTask.put(flowTask.getAid(), flowTask);
    		myLog.info(flowTask.getAid()+"\t"+flowTask.getCommand());
    	}
    }
    
    /**
     * 每隔 5 秒钟上报rtb引擎中广告的rtb请求数
     */
    public void pushAdBidNums() {
    	MDC.put("sift", "rtb");
    	bidList.clear();
    	if(bidMap != null && !bidMap.isEmpty()){
    		Iterator iter = bidMap.entrySet().iterator();
    		while(iter.hasNext()){
    			Map.Entry entry = (Map.Entry) iter.next();
    			String key = (String) entry.getKey();
    			long value = (long) entry.getValue();
    			if(value != 0){
    				AdBidBean bid = new AdBidBean();
    				bid.setUid(key);
    				bid.setBidNums(value);
    				bidList.add(bid);
    			}
    		}
    		
    	}
    	if(bidList.size() > 0){
    		NodeStatusBean bean = new NodeStatusBean();
    		bean.setBidList(bidList);
    		MsgControlCenter.sendBidStatus(nodeName, bean);
    	}
    	
    	bidMap.clear();
        
    }

    /**
     * 每隔一分钟，更新广告的超投设备
     */
    public void updateDeviceLimitMap() {
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
               /* Integer frqDaily = adBean.getFrqDaily() == 0 ? DEFAULT_LIMIT_DAILY : adBean.getFrqDaily();
                Integer frqHourly = adBean.getFrqHour() == 0 ? DEFAULT_LIMIT_HOURLY : adBean.getFrqHour();*/

                Integer frqDaily = adBean.getFrqDaily();
                Integer frqHourly = adBean.getFrqHour();
                if(frqDaily == 0 && frqHourly == 0) {
                    frqDaily = DEFAULT_LIMIT_DAILY;
                    frqHourly = DEFAULT_LIMIT_HOURLY;
                }

                if (frqDaily != 0) {
                    // 更新超投设备 Map
                    myLog.info("开始更新天的超投 Map");
                    updateDeviceLimitMap(adUid, frqDaily, deviceLimitMapDaiyly, REDIS_KEY_POSTFIX_DAILY);
                }else {
                    deviceLimitMapDaiyly.remove(adUid);
                }

                if(frqHourly != 0) {
                    myLog.info("开始更新小时的超投 Map");
                    updateDeviceLimitMap(adUid, frqHourly, deviceLimitMapHourly, REDIS_KEY_POSTFIX_HOURLY);
                }else {
                    deviceLimitMapHourly.remove(adUid);
                }

                /*if(frqDaily != null && frqDaily > 0 ) {
                    // 超投设备id
                    HashSet<String> deviceIdSets = new HashSet();
                    List<String> deviceIds = redis.zRangeByScoreAsync(adUid+REDIS_KEY_POSTFIX_DAILY, frqDaily, 100000000);
                    // 为了快速随机读取，将list转成HashSet
                    deviceIds.forEach((value) -> {
                        deviceIdSets.add(value);
                    });
                    if(deviceIds != null && deviceIds.size() > 0) {
                        deviceLimitMapDaiyly.put(adUid, deviceIdSets);
                        myLog.info("更新: 广告 {}  超投设备 {}", adUid, deviceIdSets.toString());
                    }else {
                        // 如果没有找到超投设备，则从map中移除
                        deviceLimitMapDaiyly.remove(adUid);
                        myLog.info("移除: 广告 {}", adUid);
                    }
                    myLog.info("更新: 广告 {}  超投设备 {}", adUid, deviceIdSets.toString());
                }*/
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
    private void updateDeviceLimitMap (String adUid, int frq, HashMap<String,HashSet<String>> limitMap, String postfix) {
        HashSet<String> deviceIdSets = new HashSet();
        List<String> deviceIds = redis.zRangeByScoreAsync(adUid + postfix, frq, 100000000);
        // 为了快速随机读取，将list转成HashSet
        deviceIds.forEach((value) -> {
            deviceIdSets.add(value);
        });
        if(deviceIds != null && deviceIds.size() > 0) {
            limitMap.put(adUid, deviceIdSets);
            myLog.info("更新: 广告 {} 投放限制 {} 超投设备 {}", adUid,frq, deviceIdSets.toString());
        }else {
            // 如果没有找到超投设备，则从map中移除
            limitMap.remove(adUid);
            myLog.info("移除: 广告 {} 投放限制 {}", adUid, frq);
        }
    }

    
    /**
     * 每隔1分钟上报ADX与APP流量数
     */
    public void pushAdxAndAppFlow(){
    	MDC.put("sift", "rtb");
    	if(adxFlowMap != null && !adxFlowMap.isEmpty()){
    		MsgControlCenter.sendAdxFlow(nodeName, adxFlowMap);
    	}
    	adxFlowMap.clear();
    	if(appFlowMap != null && !appFlowMap.isEmpty()){
    		MsgControlCenter.sendAppFlow(nodeName, appFlowMap);
    	}
    	appFlowMap.clear();
    }
    
    /**
     * 每隔 10分钟上报rtb引擎心跳
     */
    public void pushRtbHeart(){
    	MDC.put("sift", "rtb");
    	NodeStatusBean bean = new NodeStatusBean();
    	bean.setLastUpdateTime(System.currentTimeMillis());
    	MsgControlCenter.sendNodeStatus(nodeName, bean);
    }

    /**
     * 每个小时重置一次 重置每个小时的投放状态，如果为暂停状态，且作用域为小时，则下一个小时可以继续开始
     */
    public void refreshAdStatus() {
    	MDC.put("sift", "rtb");
    	
    	Date date = new Date();
		String time = dateFm.format(date);
		String splitTime[] = time.split("_");
		int weekNum = TimeUtil.weekDayToNum(splitTime[0]);
		int dayNum = Integer.parseInt(splitTime[1]);
		if (dayNum == 24)
			dayNum = 0;
		
		weekAndDayNumMap.put("EEEE", weekNum);
		weekAndDayNumMap.put("HH", dayNum);
    	
        for (String auid : mapTask.keySet()) {
            TaskBean bean = mapTask.get(auid);
            AdBean ad = mapAd.get(auid);
            if(ad != null){
            ad.getPropertyBean();

            int scope = bean.getScope();
            int commandCode = bean.getCommand();
            if (scope == TaskBean.SCOPE_HOUR && commandCode == TaskBean.COMMAND_PAUSE) {
                bean.setCommand(TaskBean.COMMAND_START);
            }
            }
        }
    }

    /**
     * 监测广告是否可用
     *
     * @param auid
     * @return
     */
    public boolean checkAvalable(String auid,String deviceId) {
    	MDC.put("sift", "rtb");
        TaskBean bean = mapTask.get(auid);
        if (bean != null) {
            int commandCode = bean.getCommand();
            // int scope = bean.getScope();
            // public static final int TASK_STATE_READY = 0;
            // public static final int TASK_STATE_RUNNING = 1;
            // public static final int TASK_STATE_FINISHED = 2;
            // public static final int TASK_STATE_PAUSED = 3;
            // public static final int TASK_STATE_STOPED = 4;
            switch (commandCode) {
                case TaskBean.COMMAND_PAUSE:
                    return false;
                case TaskBean.COMMAND_STOP:
                    return false;
                default:
                    break;
            }
        }else{
        	return false;
        }
        
        // 匹配广告投放时间窗
        AdBean adBean = mapAd.get(auid);
        if (adBean != null) {
        	//判断广告开始时间和结束时间
        	long startTime = adBean.getStartTime().getTime();
        	long endTime = adBean.getEndTime().getTime();
        	long now = System.currentTimeMillis()/1000;
        	if(startTime > now || endTime < now){
        		return false;
        	}
        	//判断广告投放时间窗
        	String scheduleTime = adBean.getScheduleTime();
        	//代表时间窗选择了不限
        	if(scheduleTime != null && scheduleTime.trim().equals("{}")){
        		return true;
        	}
        	
        	if(deviceId != null && deviceLimitMapDaiyly.get(auid) != null && 
        			deviceLimitMapDaiyly.get(auid).contains(deviceId)){
        		myLog.info("广告["+auid+"]设备ID["+deviceId+"已达到天频次");
        		return false;
        	}
        	
        	if(deviceId != null && deviceLimitMapHourly.get(auid) != null && 
        			deviceLimitMapHourly.get(auid).contains(deviceId)){
        		myLog.info("广告["+auid+"]设备ID["+deviceId+"已达到小时频次");
        		return false;
        	}
        	        	
        	Integer weekNum = weekAndDayNumMap.get("EEEE");
        	Integer dayNum = weekAndDayNumMap.get("HH");
        	if(weekNum == null || dayNum == null){
        		weekNum = -1;
        	}        	        	
            int[][] timeSchedulingArr = adBean.getTimeSchedulingArr();           
            if(timeSchedulingArr != null){
            for (int i = 0; i < timeSchedulingArr.length; i++) {
                if (weekNum != i){
                    continue;
                }
                for (int j = 0; j < timeSchedulingArr[i].length; j++) {
                    if (dayNum == j) {
                        if (timeSchedulingArr[i][j] == 1) {
                            return true;                            
                        } else {
                            return false;
                        }
                    }
                }
            }
            }
            
        }else{
        	return false;
        }
        return false;
    }
}
