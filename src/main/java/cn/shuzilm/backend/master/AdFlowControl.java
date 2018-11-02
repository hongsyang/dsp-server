package cn.shuzilm.backend.master;

import cn.shuzilm.backend.master.db.DetailDataInToDBTask;
import cn.shuzilm.backend.master.db.LogDataInToDBTask;
import cn.shuzilm.backend.master.node.GainDataFromPIXCELQueue;
import cn.shuzilm.backend.master.node.GainDataFromRTBQueue;
import cn.shuzilm.backend.queue.DataTransQueue;
import cn.shuzilm.bean.control.*;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.TimeSchedulingUtil;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 广告流量控制
 * Created by thunders on 2018/7/11.
 */
public class AdFlowControl {
    private static AdFlowControl control;
    private static AdPropertyHandler adProperty;
    private static CPCHandler cpcHandler;


    public static AdFlowControl getInstance() {
        if (control == null) {
            control = new AdFlowControl();
            adProperty = new AdPropertyHandler(control);
            cpcHandler = new CPCHandler(control);
            new Thread(new LogDataInToDBTask()).start();
            new Thread(new DetailDataInToDBTask()).start();
        }
        return control;
    }

    private static final Logger myLog = LoggerFactory.getLogger(AdFlowControl.class);

    public static ArrayList<WorkNodeBean> nodeList = null;
    
    public static ArrayList<String> nodeRealList = new ArrayList<String>();
    
    /**
     * 主控定期从数据库中读取的间隔 单位：分钟
     */
    private static final int INTERVAL = 10 * 60 * 1000;
    private static TaskServicve taskService = new TaskServicve();
    
    /**
     * 节点判定宕机时间周期
     */
    private static final int NODE_DOWN_INTERVAL = 30 * 60 * 1000;

//    /**
//     * 广告主对应的广告 MAP
//     */
//    private static HashMap<String,ArrayList<String>> adviserMap = null;

    public ConcurrentHashMap<String, AdBean> getMapAd(){
        return mapAd;
    }

    public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorHour(){
        return mapMonitorHour;
    }

    public ConcurrentHashMap<String, ReportBean> getReportMapHour(){
        return reportMapHour;
    }
    
    public ConcurrentHashMap<String,Long> getNodeStatusMap(){
    	return nodeStatusMap;
    }

    /**
     * 广告资源管理
     */
    private static ConcurrentHashMap<String, AdBean> mapAd = null;

    /**
     * 广告任务管理
     */
    private static ConcurrentHashMap<String, TaskBean> mapTask = null;

    private static ConcurrentHashMap<String, ReportBean> reportMapTotal = null;
    private static ConcurrentHashMap<String, ReportBean> reportMapHour = null;

    /**
     * 广告组与广告的对应关系
     */
    private static ConcurrentHashMap<String, GroupAdBean> mapAdGroup = null;

    /**
     * 广告永久的指标监控
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorTotal = null;
    /**
     * 广告每天的指标监控
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorDaily = null;
    /**
     * 广告每小时的指标监控
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorHour = null;

    /**
     * 广告组的监视器
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorAdGroupTotal = null;
    
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorAdGroupRealTotal = null;

    /**
     * 数据库中设定的设计流控指标（天 最高限）
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapThresholdDaily = null;

    /**
     * 数据库中设定的设计流控指标（小时 最高限）
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapThresholdHour = null;

    /**
     * 广告主设定的总流量和金额 (最高限)
     */
    private static ConcurrentHashMap<String, AdFlowStatus> adverConsumeMapCurr = null;
    
    private static ConcurrentHashMap<String,Long> nodeStatusMap = null;
    
    private DataTransQueue queue;
    
    private static ConcurrentHashMap<String,Float> cpcClieckRatioMap = null; 
    
    private static final float maxCpcClieckRatio = 0.03f;

    public AdFlowControl() {

        mapAd = new ConcurrentHashMap<>();
        mapTask = new ConcurrentHashMap<>();

        mapMonitorDaily = new ConcurrentHashMap<>();
        mapMonitorHour = new ConcurrentHashMap<>();
        adverConsumeMapCurr = new ConcurrentHashMap<>();
        mapThresholdDaily = new ConcurrentHashMap<>();
        mapThresholdHour = new ConcurrentHashMap<>();
        mapAdGroup = new ConcurrentHashMap<>();
        mapMonitorTotal = new ConcurrentHashMap<>();
        reportMapHour = new ConcurrentHashMap<>();
        mapMonitorAdGroupTotal = new ConcurrentHashMap<>();
        mapMonitorAdGroupRealTotal = new ConcurrentHashMap<>();
        nodeStatusMap = new ConcurrentHashMap<>();
        cpcClieckRatioMap = new ConcurrentHashMap<>();
        queue = DataTransQueue.getInstance();
//        adviserMap = new HashMap<>();


    }

    public void trigger() {
        // 5 s 触发
        pullAndUpdateTask(true);
        // 10 min 触发
        loadAdInterval(true);
        //每小时触发
        resetHourMonitor();
        //每天触发
        resetDayMonitor();

    }

    /**
     * 将 RTB 数量更新到天 、小时和总监视器中
     *
     * @param adUid
     * @param addBidNums
     */
    public void updateBids(String adUid, long addBidNums) {
    	MDC.put("sift", "control");
        AdFlowStatus statusHour = mapMonitorHour.get(adUid);
        if(statusHour != null)
        	statusHour.setBidNums(statusHour.getBidNums() + addBidNums);
        AdFlowStatus statusDaily = mapMonitorDaily.get(adUid);
        if(statusDaily != null)
        	statusDaily.setBidNums(statusDaily.getBidNums() + addBidNums);
        AdFlowStatus statusTotal = mapMonitorTotal.get(adUid);
        if(statusTotal != null)
        	statusTotal.setBidNums(statusTotal.getBidNums() + addBidNums);
    }

    /**
     * 将 曝光和产生的费用更新到 天和小时监视器中
     *
     * @param adUid
     * @param addWinNoticeNums
     * @param addMoney
     * @param type             0 hour  1 daily  2 total -1 全部都更新
     * @param clickNums 点击次数
     * @param pixelType pixcel 类型，曝光 0 和 点击 1
     * @param isLower true 表示允许继续投放 false 表示竞价价格过低，要求提升报价
     */
    public void updatePixel(String adUid, long addWinNoticeNums, float addMoney, int type , long clickNums, int pixelType, boolean isLower) {
        //cpc 定价计算逻辑
    	MDC.put("sift", "control");
    	AdBean adBean = mapAd.get(adUid);
        if(adBean.getMode()!= null && adBean.getMode().equals("cpc")){
        	cpcHandler.updatePixel(adBean,adUid,addWinNoticeNums,addMoney,clickNums,pixelType,maxCpcClieckRatio,cpcClieckRatioMap);
        }       
        switch (type) {
            case 0:
                AdFlowStatus statusHour = mapMonitorHour.get(adUid);
                if (statusHour == null)
                    break;
                //只计算和统计计小时点击率就可以
                if(pixelType == 0){
                    statusHour.setUid(adUid);
                    statusHour.setWinNums(statusHour.getWinNums() + addWinNoticeNums);
                    statusHour.setMoney(statusHour.getMoney() + addMoney);
                }else if(pixelType == 1){
                    statusHour.setUid(adUid);
                    statusHour.setClickNums(statusHour.getClickNums() +  clickNums);
                }

                break;
            case 1:
                AdFlowStatus statusDaily = mapMonitorDaily.get(adUid);
                if (statusDaily == null)
                    break;
                statusDaily.setUid(adUid);
                statusDaily.setWinNums(statusDaily.getWinNums() + addWinNoticeNums);
                statusDaily.setMoney(statusDaily.getMoney() + addMoney);
                if(mapAd.containsKey(adUid)){
                	String groupId = mapAd.get(adUid).getGroupId();
                	AdFlowStatus statusGroupAll = mapMonitorAdGroupTotal.get(groupId);
                	statusGroupAll.setMoney(statusGroupAll.getMoney() + addMoney);
                }
                break;
            case 2:
                //更新历史全部状态
                AdFlowStatus statusAll = mapMonitorTotal.get(adUid);
                if (statusAll == null)
                    break;
                statusAll.setUid(adUid);
                statusAll.setWinNums(statusAll.getWinNums() + addWinNoticeNums);
                statusAll.setMoney(statusAll.getMoney() + addMoney);
                //更新广告组金额状态
                if(mapAd.containsKey(adUid)){   
                	String groupId = mapAd.get(adUid).getGroupId();
                	AdFlowStatus statusTotalGroupAll = mapMonitorAdGroupRealTotal.get(groupId);
                	statusTotalGroupAll.setMoney(statusTotalGroupAll.getMoney() + addMoney);
                }
                break;
            case -1:
                statusHour = mapMonitorHour.get(adUid);
                if (statusHour != null) {
                    statusHour.setUid(adUid);
                    statusHour.setWinNums(statusHour.getWinNums() + addWinNoticeNums);
                    statusHour.setMoney(statusHour.getMoney() + addMoney);
                }

                statusDaily = mapMonitorDaily.get(adUid);
                if (statusDaily != null) {
                    statusDaily.setUid(adUid);
                    statusDaily.setWinNums(statusDaily.getWinNums() + addWinNoticeNums);
                    statusDaily.setMoney(statusDaily.getMoney() + addMoney);
                }

                statusAll = mapMonitorTotal.get(adUid);
                if (statusAll != null) {
                    statusAll.setUid(adUid);
                    statusAll.setWinNums(statusAll.getWinNums() + addWinNoticeNums);
                    statusAll.setMoney(statusAll.getMoney() + addMoney);
                }

                //更新广告组金额状态
                if(mapAd.containsKey(adUid)){
                	String groupId = mapAd.get(adUid).getGroupId();
                	AdFlowStatus statusGroupAll = mapMonitorAdGroupTotal.get(groupId);
                	statusGroupAll.setMoney(statusGroupAll.getMoney() + addMoney);
                	AdFlowStatus statusTotalGroupAll = mapMonitorAdGroupRealTotal.get(groupId);
                	statusTotalGroupAll.setMoney(statusTotalGroupAll.getMoney() + addMoney);
                }
                
                if(!isLower && mapTask.containsKey(adUid)){
                	String reason = "["+adUid+"]竞价价格过低，请提升报价";
                	stopAd(adUid, reason, false,0);
                }
                
              //拿当前的指标跟当前的阀值比较，如果超出阀值，则立刻停止任务，并下发任务停止命令
                    
                if(mapAd.containsKey(adUid)){
                	AdBean ad = mapAd.get(adUid);
                	String mode = ad.getMode();
                	if("cpc".equalsIgnoreCase(mode)){
                	//监测 CPC 类型的广告是否可以投放
                    String isOk = cpcHandler.checkAvailable(adUid);
                    if(isOk != null){
                        //String reason = "### cpc 价格设置 过低，超过了成本线，停止广告投放 ###" + auid;
                        stopAd(adUid, isOk, false,0);
                    }
                	}
                    AdFlowStatus threshold = mapThresholdHour.get(adUid);
                    AdFlowStatus monitor = mapMonitorHour.get(adUid);
                    //每小时曝光超过了设置的最大阀值，则终止该小时的广告投放
                    if (threshold != null && monitor != null && threshold.getWinNums() != 0 && monitor.getWinNums() >= threshold.getWinNums()) {
                        String reason = "#### 小时 CPM 超限，参考指标：" + threshold.getWinNums() + "\t" + monitor.getWinNums() + " ### " ;
                        pauseAd(adUid, reason, true);
                        myLog.error(monitor.toString() + "\t" + reason);
                    }
                    /*if (threshold.getMoney() != 0 && monitor.getMoney() >= threshold.getMoney()) {
                    //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                    String reason = "#### 小时 金额 超限，参考指标：" + threshold.getMoney() + "\t" + monitor.getMoney() + " ###";
                    pauseAd(auid, reason, true);
                    myLog.error(monitor.toString() + "\t" + reason);

                	}*/

                    String groupId = mapAd.get(adUid).getGroupId();
                    AdFlowStatus monitorAdGroup = mapMonitorAdGroupTotal.get(groupId);
                    AdFlowStatus monitorTotalAdGroup = mapMonitorAdGroupRealTotal.get(groupId);
                    double thresholdGroupMoney = mapAdGroup.get(groupId).getQuotaMoney().doubleValue()*1000;
                    double thresholdTotalGroupMoney = mapAdGroup.get(groupId).getQuotaTotalMoney().doubleValue()*1000;
                    int quota = mapAdGroup.get(groupId).getQuota();
                    int quotaTotal = mapAdGroup.get(groupId).getQuota_total();
                    if(monitorAdGroup != null && thresholdGroupMoney != 0 && quota == 1 && monitorAdGroup.getMoney() >= thresholdGroupMoney * 0.9){
                        //广告组每日限额超限，则发送停止命令，终止该广告投放
                        String reason = "#### 广告组 每日限额 超限，参考指标：" + thresholdGroupMoney + "元(CPM)\t" + monitorAdGroup.getMoney() + "元 (CPM)###";
                        stopAd(adUid, reason, false,0);
                        myLog.error(adUid + "\t" + reason);
                    }
                    
                    if(monitorTotalAdGroup != null && thresholdTotalGroupMoney != 0 && quotaTotal == 1 && monitorTotalAdGroup.getMoney() >= thresholdTotalGroupMoney * 0.9){
                        //广告组总限额超限，则发送停止命令，终止该广告投放
                        String reason = "#### 广告组 总限额 超限，参考指标：" + thresholdTotalGroupMoney + "元(CPM)\t" + monitorTotalAdGroup.getMoney() + "元(CPM) ###";
                        stopAd(adUid, reason, false,0);
                        myLog.error(adUid + "\t" + reason);
                    }

                //账户的余额和每日的限额都在这里做适配，以最低的为准，
                //其中来自于三个地方： 2 个是 balance 表的 balance 字段 和 quota_amount 字段 ，
                // 还有一个地方来自于 广告组限额
                //跟每日监控作比对
                    AdFlowStatus thresholdDaily = mapThresholdDaily.get(adUid);
                    AdFlowStatus monitorDaily = mapMonitorDaily.get(adUid);
                    if (monitorDaily != null && thresholdDaily != null && thresholdDaily.getMoney() != 0 && monitorDaily.getMoney() >= thresholdDaily.getMoney() * 0.9) {
                        //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                        String reason = "#### 每日金额 超限，参考指标：" + thresholdDaily.getMoney() + "元(CPM)\t" + monitorDaily.getMoney() + "元(CPM) ###";
                        stopAd(adUid, reason, false,0);
                        myLog.error(monitorDaily.toString() + "\t" + reason);
                    }
                    
                    if (monitorDaily != null && thresholdDaily != null && thresholdDaily.getWinNums() != 0 && monitorDaily.getWinNums() >= thresholdDaily.getWinNums() * 0.9) {
                        String reason = "#### 每日 CPM 超限，参考指标：" + thresholdDaily.getWinNums() + "元(CPM)\t" + monitorDaily.getWinNums() + "元(CPM) ### " ;
                        stopAd(adUid, reason, true,0);
                        myLog.error(monitorDaily.toString() + "\t" + reason);
                    }

                }
                
                
                break;
        }
    }


    /**
     * 每隔 5 秒钟从消息中心获得所有节点的当前任务，并与当前两个 MAP monitor 进行更新
     * 每个节点单独启线程收取任务
     */
    public void pullAndUpdateTask(boolean isInit) {
    	MDC.put("sift", "control");
        //分发任务
        //1、根据当前各个节点消耗的情况，进行扣减，如：之前已经有该广告在投放了，后来调整了配额或金额，则从当前的额度中减掉已经消耗的部分（每小时和每天的），然后剩余的作为任务重新分发下去
        //更新当前广告主报价，资金池，流量池,广告打分
    	
    	List<String> nodeNameList = new ArrayList<String>();
    	if(isInit){
    		for(WorkNodeBean node: nodeList){
    			nodeNameList.add(node.getName());
    			nodeRealList.add(node.getName());
    		}
    	}else{
    		for(WorkNodeBean node: nodeList){
    			if(!nodeRealList.contains(node.getName())){
    				nodeNameList.add(node.getName());
    				nodeRealList.add(node.getName());
    			}
    		}
    	}
    	
    	for(String nodeName: nodeNameList){
    		if(nodeName != null && nodeName.contains("rtb-")){
    			myLog.info("从"+nodeName+"中获取bids个数线程开启......");
    			new Thread(new GainDataFromRTBQueue(nodeName)).start();
    		}
    		if(nodeName != null && nodeName.contains("pixel-")){
    			myLog.info("从"+nodeName+"中获取最新 wins 和 金额消费情况线程开启......");
    			new Thread(new GainDataFromPIXCELQueue(nodeName)).start();
    		}
    	}
    	

    }

    /**
     * 小时计数器清零
     * 每个小时重置一次 重置每个小时的投放状态，如果为暂停状态，且作用域为小时，则下一个小时可以继续开始
     */
    public void resetHourMonitor() {
    	MDC.put("sift", "control");
        //清理小时计数器
    	myLog.info("开始启动小时计数器清零......");
        for (String key : mapMonitorHour.keySet()) {
            AdFlowStatus status = mapMonitorHour.get(key);
            status.setBidNums(0);
            status.setMoney(0);
            status.setWinNums(0);
        }
        
        for (String auid : mapTask.keySet()) {
        	TaskBean bean = mapTask.get(auid);
        	int scope = bean.getScope();
            int commandCode = bean.getCommand();
            if (scope == TaskBean.SCOPE_HOUR && commandCode == TaskBean.COMMAND_PAUSE) {
                bean.setCommand(TaskBean.COMMAND_START);
                putDataToAdLogQueue(auid, "小时计数器清零,广告开启", 1);
            }
        }
    }


    /**
     * 每天初始化一次小时 和 天计数器
     */
    public void resetDayMonitor() {
    	MDC.put("sift", "control");
    	myLog.info("开始启动天计数器清零......");
        long time = 0;
        ResultList rl = null;
        try {
            rl = taskService.queryAdByUpTime(time);
            mapMonitorDaily.clear();
            mapMonitorHour.clear();
            mapMonitorAdGroupTotal.clear();
            for (ResultMap map : rl) {
                String auid = map.getString("uid");
                String name = map.getString("name");
//                int winNumsHour = map.getInteger("cpm_hourly");
//                int winNumsDaily = map.getInteger("cpm_daily");
//                BigDecimal money = map.getBigDecimal("quota_amount");

                //初始化天监视器
                AdFlowStatus status = new AdFlowStatus();
                status.reset();
                status.setUid(auid);
                status.setName(name);
                mapMonitorDaily.put(auid, status);
                //初始化小时监视器
                AdFlowStatus status2 = new AdFlowStatus();
                status2.reset();
                status2.setUid(auid);
                status2.setName(name);
                mapMonitorHour.put(auid, status2);
                
             // 初始化广告组监视器
                AdFlowStatus status3 = new AdFlowStatus();
                status3.reset();
                status3.setUid(auid);
                status3.setName(name);
                mapMonitorAdGroupTotal.put(auid, status3);

                
                if(mapTask.containsKey(auid)){
                	TaskBean task = mapTask.get(auid);
                	task.setCommand(TaskBean.COMMAND_START);
                	putDataToAdLogQueue(auid, "天计数器清零,广告开启", 1);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 每隔 10 分钟更新一次天和小时的阀值
     * 返回 余额为 0 的广告
     */
    private HashSet<String> updateIndicator(ResultList adList) {
    	MDC.put("sift", "control");
        HashSet<String> lowBalanceAdSet = new HashSet<>();
        try {
            for (ResultMap map : adList) {
                String auid = map.getString("uid");
                String name = map.getString("name");
                String adviserId = map.getString("advertiser_uid");
                ResultMap balanceMap = taskService.queryAdviserAccountById(adviserId);
                if(balanceMap == null)
                    continue;
                //广告主账户中的余额
                BigDecimal balance = balanceMap.getBigDecimal("balance");
                //如果余额小于 200 块钱，则不进行广告投放
                if(balance.doubleValue() < 200){
                    lowBalanceAdSet.add(auid);
                }else{
                	if(mapTask.containsKey(auid)){
                		TaskBean task = mapTask.get(auid);
                		if(task.getCommandResonStatus() == 1){
                			task.setCommand(TaskBean.COMMAND_START);
                			putDataToAdLogQueue(auid, "广告主已充值,广告开启", 1);
                		}
                	}
                }
                //广告主账户的每日限额
                BigDecimal quotaMoneyPerDay = balanceMap.getBigDecimal("quota_amount");



                int winNumsHour = map.getInteger("cpm_hourly");
                int winNumsDaily = map.getInteger("cpm_daily");
                // 当前广告表中的针对广告的限额
                BigDecimal money = map.getBigDecimal("quota_amount");

                // 如果广告主中的每日限额比广告的还小，以小的为准
                if (quotaMoneyPerDay.floatValue() != 0 && quotaMoneyPerDay.floatValue() <= money.floatValue()) {
                    money = quotaMoneyPerDay;
                }
                //如果这个账户的余额比每天或小时的限额还小，则赋予小的值
                if (balance.floatValue() != 0 && balance.floatValue() <= money.floatValue()) {
                    money = balance;
                }


                //重新加载 天 参考指标
                AdFlowStatus status3 = new AdFlowStatus();
                status3.reset();
                status3.setUid(auid);
                status3.setName(name);
                status3.setWinNumsByThousand(winNumsDaily);
                status3.setMoney(money.floatValue()*1000);
                mapThresholdDaily.put(auid, status3);
                //重新加载 小时 参考指标
                AdFlowStatus status4 = new AdFlowStatus();
                status4.setUid(auid);
                status4.setName(name);
                status4.setWinNumsByThousand(winNumsHour);
                status4.setMoney(money.floatValue()*1000);
                mapThresholdHour.put(auid, status4);

                //写入广告主每日限额
                ReportBean report = reportMapHour.get(auid);
                if(report != null){
                    report.setBalance(balance);

                    // 如果当前广告设定限额为 0 ，则以该账户的每日限额为准， 如果该账户每日限额为 0 ， 则以余额为准
                    if(quotaMoneyPerDay.doubleValue() > 0)
                        report.setMoneyQuota(quotaMoneyPerDay);
                    else
                        report.setMoneyQuota(balance);
                }
            }
            return lowBalanceAdSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
//
//    /**
//     * 统计每一个广告主的消费情况，
//     */
//    public void statConsumeByAdver(){
//
//    }
    
    /**
     * 每隔10分钟读取库中广告，判断是否有广告被关闭
     */
    public void updateCloseAdInterval(){
    	MDC.put("sift", "control");  
        try {
        	//加载广告信息
			ResultList adList = taskService.queryAdByUpTime(0);
			Set<String> adUidSet = new HashSet<String>();
			for (ResultMap map : adList) {
				adUidSet.add(map.getString("uid"));
			}
			Iterator iter = mapAd.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, AdBean> entry = (Map.Entry) iter.next();
				String adUid = entry.getKey();
				if(!adUidSet.contains(adUid)){				
					stopAd(adUid, adUid+"广告被关闭", false,0);
				}
			}
		} catch (Exception e) {
			myLog.error("移除广告更新缓存异常:"+e.getMessage());
		}
    }
    
    /**
     * 每隔5分钟更新广告中有变化的人群包、创意、物料
     * 每隔5分钟更新广告主、代理商
     */
    public void updateAdMapInterval(){
    	MDC.put("sift", "control"); 
    	try{
    		//开始更新人群包
    		List<AudienceBean> audienceList = taskService.queryAudienceByUpTime();
    		for(AudienceBean audience:audienceList){
    			String adUid = audience.getAdUid();
    			if(mapAd.containsKey(adUid)){
    				AdBean ad = mapAd.get(adUid);
    				List<AudienceBean> audienceTempList = ad.getAudienceList();
    				ListIterator<AudienceBean> it = audienceTempList.listIterator();
    				while(it.hasNext()){
    					AudienceBean audienceBean = it.next();
    					if(audienceBean.getUid().equals(audience.getUid())){
    						it.remove();
    						it.add(audience);
    					}
    				}
    			}
    		}
    		
    		//开始更新创意、物料
    		List<CreativeBean> creativeList = taskService.queryCreativeByUpTime();
    		for(CreativeBean creative:creativeList){
    			String adUid = creative.getRelatedAdUid();
    			List<Material> materialList = taskService.queryMaterialByCreativeId(creative.getUid());
    			creative.setMaterialList(materialList);
    			if(mapAd.containsKey(adUid)){
    				AdBean ad = mapAd.get(adUid);
    				List<CreativeBean> creativeTempList = ad.getCreativeList();
    				ListIterator<CreativeBean> it = creativeTempList.listIterator();
    				while(it.hasNext()){
    					CreativeBean creativeBean = it.next();
    					if(creativeBean.getUid().equals(creative.getUid())){
    						it.remove();
    						it.add(creative);
    					}
    				}
    			}
    			
    		}
    		//开始更新广告主、代理商
    		List<AdvertiserBean> advertiserList = taskService.queryAdverByUpTime();
    		for(AdvertiserBean advertiser:advertiserList){
    			Iterator it = mapAd.entrySet().iterator();
    			while(it.hasNext()){
    				Map.Entry<String, AdBean> entry = (Entry<String, AdBean>) it.next();
    				AdBean ad = entry.getValue();
    				AdvertiserBean adver = ad.getAdvertiser();
    				if(advertiser.getUid().equals(adver.getUid())){
    					ad.setAdvertiser(advertiser);
    				}
    			}
    		}
    	}catch(Exception e){
    		myLog.error("更新广告异常:"+e.getMessage());
    	}
    }


    /**
     * 每隔 10 分钟
     * 从数据库中加载所有的广告,广告主、广告素材和广告配额
     */
    public void loadAdInterval(boolean isInitial) {
    	MDC.put("sift", "control");
    	ConcurrentHashMap<String, ReportBean> reportMapDaily = null;
        long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        //取出所有的广告，并取出变动的部分，如果是配额和金额发生变化，则需要重新分配任务
        try {
            //加载 主机节点信息
            nodeList = taskService.getWorkNodeAll();

            //加载最新广告信息
            if (isInitial) {
                timeBefore = 0;
            }
            //加载广告组信息
            ArrayList<GroupAdBean> groupList = taskService.queryAdGroupAll(timeBefore);
            if (groupList != null) {
                for (GroupAdBean group : groupList) {
                    mapAdGroup.put(group.getGroupId(), group);
                }
            }
            //加载广告信息
            ResultList adList = taskService.queryAdByUpTime(timeBefore);

            //加载 小时 历史消费金额
            reportMapHour = taskService.statAdCostHour();
            //加载 天 历史消费金额
            reportMapDaily = taskService.statAdCostDaily();
            //加载 总 历史消费金额
            reportMapTotal = taskService.statAdCostTotal();


            //更新监视器阀值信息
            HashSet<String> lowBalanceAdList = null;
            if(isInitial){
            	lowBalanceAdList = updateIndicator(adList);
            }else{
            	ResultList adAllList = taskService.queryAdByUpTime(0);
            	lowBalanceAdList = updateIndicator(adAllList);
            }

            int counter = 0;


            for (ResultMap map : adList) {
                AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));
                //广告组
                String groupId = map.getString("group_uid");
                ad.setGroupId(groupId);
                String adUid = ad.getAdUid();

                String adverUid = map.getString("advertiser_uid");

                //根据 广告主ID 获得 广告主
                AdvertiserBean adver = taskService.queryAdverByUid(adverUid);
                ad.setAdvertiser(adver);
                ad.setName(map.getString("name"));
                //每天限制
                ad.setCpmDailyLimit(map.getInteger("cpm_daily"));
                //每小时限制
                ad.setCpmHourLimit(map.getInteger("cpm_hourly"));


                //获得人群
                List<AudienceBean> audience = taskService.queryAudienceByUpTime(adUid);
                ad.setAudienceList(audience);

                String creativeUid = map.getString("creative_uid");
                //根据 广告创意ID 获得广告创意
                CreativeBean creativeBean = taskService.queryCreativeUidByAid(creativeUid);
                //根据创意 ID 查询 物料
                List<Material> materialList = taskService.queryMaterialByCreativeId(creativeUid);
                creativeBean.setMaterialList(materialList);

                ArrayList<CreativeBean> creaList = new ArrayList<>();
                creaList.add(creativeBean);
                ad.setCreativeList(creaList);
                ad.setEndTime(new Date(map.getInteger("e")));
                ad.setFrqDaily(map.getInteger("frq_daily"));
                ad.setFrqHour(map.getInteger("frq_hourly"));
                String mode = map.getString("mode");
                ad.setMode(mode);
                // 设置广告的可拖欠的额度
                ad.setMoneyArrears(map.getInteger("money_arrears"));
                //出价模式
                ad.setMode(mode);
                if("cpc".equalsIgnoreCase(mode)){
                	if(cpcClieckRatioMap.containsKey(ad.getAdUid())){
                		float clieckRatio = cpcClieckRatioMap.get(ad.getAdUid());
                		ad.setPrice(map.getBigDecimal("price").floatValue() * clieckRatio * 1000);
                	}else{
                		cpcClieckRatioMap.put(ad.getAdUid(), maxCpcClieckRatio);
                		ad.setPrice(map.getBigDecimal("price").floatValue() * maxCpcClieckRatio * 1000);
                	}                	
                }else{
                	ad.setPrice(map.getBigDecimal("price").floatValue());
                }

//                ad.setPriority(map.getInteger("priority"));
                //限额
                // 如果当前广告设定限额为 0 ，则以该账户的每日限额为准，
                BigDecimal quotaAmount = map.getBigDecimal("quota_amount");
                if(quotaAmount.doubleValue() <= 0 && reportMapHour.containsKey(adUid)){
                    ad.setQuotaAmount(reportMapHour.get(adUid).getMoneyQuota());
                }else{
                    ad.setQuotaAmount(quotaAmount);
                }
                ad.setSpeedMode(map.getInteger("speed"));
                ad.setStartTime(new Date(map.getInteger("s")));
                String timeScheTxt = map.getString("time");
                int[][] timeScheduling = TimeSchedulingUtil.timeTxtToMatrix(timeScheTxt);
                ad.setTimeSchedulingArr(timeScheduling);
                ad.setTimestamp(map.getInteger("created_at"));
                //如果是价格和配额发生了变化，直接通知
                //如果素材发生了变化，直接通知
                mapAd.put(adUid, ad);
                
                if (isInitial ||!mapMonitorTotal.containsKey(adUid)) {
                    //初始化所有的监控
                    AdFlowStatus statusHour = new AdFlowStatus();
                    mapMonitorHour.put(adUid,statusHour);
                    AdFlowStatus statusDay = new AdFlowStatus();
                    mapMonitorDaily.put(adUid,statusDay);
                    AdFlowStatus statusAll = new AdFlowStatus();
                    mapMonitorTotal.put(adUid,statusAll);
                    AdFlowStatus statusAdGroup = new AdFlowStatus();
                    mapMonitorAdGroupTotal.put(groupId,statusAdGroup);
                    mapMonitorAdGroupRealTotal.put(groupId, statusAdGroup);

                    if (reportMapHour.size() > 0) {
                        ReportBean report = reportMapHour.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, 0, expense.floatValue(), 0,0,0,true);
                        }
                    }

                    if (reportMapDaily.size() > 0) {
                        ReportBean report = reportMapDaily.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, 0, expense.floatValue(), 1,0,0,true);
                        }
                    }

                    if (reportMapTotal.size() > 0) {
                        ReportBean report = reportMapTotal.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, 0, expense.floatValue(), 2,0,0,true);
                        }
                    }
                    
                    putDataToAdLogQueue(adUid, "广告首次下发,广告开启", 1);
                }
                
                
              //AdBean 里面的加速投放为 0 ,对应数据库里面 1（ALL） ，匀速相反
                TaskBean task = null;
                if(mapTask.containsKey(adUid)){
                    task = mapTask.get(adUid);
                    task.setCommand(TaskBean.COMMAND_START);
                    putDataToAdLogQueue(adUid, "广告被修改,广告开启", 1);
                    int scope = -1;
                    if(ad.getSpeedMode() == 0)
                        scope = 1;
                    else if(ad.getSpeedMode() == 1)
                        scope = 0;
                    task.setScope(scope);
                }else{
                    task = new TaskBean(adUid);
                    cpcHandler.updateIndicator(adUid);
                    putDataToAdLogQueue(adUid, "广告首次下发,广告开启", 1);
                }
                mapTask.put(adUid, task);
                counter++;

                if(lowBalanceAdList!= null && lowBalanceAdList.contains(adUid)){
                    stopAd(adUid,adUid + "\t广告余额不足，请联系广告主充值。。",false,1);
//                    myLog.error(adUid + "\t广告余额不足，请联系广告主充值。。");
                    continue;
                }
                
            }

            //计算权重因子
            adProperty.handle();
            //定期 10 分钟更新 CPC 阀值
            //cpcHandler.updateIndicator(false);

            myLog.info("主控： 开始分发任务，此次有 " + counter + " 个广告需要分发。。。 ");
//            for (int i = 0; i < 10 ; i++) {
            dispatchTask();
//            }

            myLog.info("主控： 开始分发任务，此次有 " + counter + " 分发完毕。。。");
            myLog.info("主控： 共有 " + mapAd.keySet().size() + " 个广告在运行");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 每隔 10 分钟，从数据库中获得所有的广告信息，并进行任务拆解， 同时 通过消息中心将任务下发到每一个节点中
     * 其中包括各种对广告的控制，包括开启广告，暂停广告，终止广告等
     */
    private void dispatchTask() {
    	MDC.put("sift", "control");
        int nodeNums = nodeList.size();
        //遍历所有的广告
        ArrayList<AdBean> adList = new ArrayList<AdBean>();
        ArrayList<TaskBean> taskList = new ArrayList<TaskBean>();
        for (String adUid : mapAd.keySet()) {
            //对任务进行拆解
//            TaskBean task = new TaskBean(adUid);
        	TaskBean task = mapTask.get(adUid);
            AdBean ad = mapAd.get(adUid);
            
            //从小时监控中取出曝光量、点击次数 、点击金额
//            AdFlowStatus statusHour = mapMonitorHour.get(adUid);
//            task.setClickNums(statusHour.getClickNums());
//            task.setExposureNums(statusHour.getWinNums());
//            task.setMoney(statusHour.getMoney());
//
//            //给每一个节点分配自己的 曝光 额度
//            task.setExposureLimitPerHour(ad.getCpmHourLimit() / nodeNums);
//            task.setExposureLimitPerDay(ad.getCpmDailyLimit() / nodeNums);
//            task.setCommand(TaskBean.COMMAND_START);            
            if(task.getCommand() == TaskBean.COMMAND_START){
            	myLog.info("广告["+adUid+"]加入下发队列......");
            	taskList.add(task);
            	adList.add(ad);
            }
           
        }

        for (WorkNodeBean node : nodeList) {
            //发送广告和任务
        	if(!isNodeDown(node.getName())){
        		pushAdSingleNode(node.getName(), adList);
        		pushTaskSingleNode(node.getName(), taskList);
        	}
        }
    }
    
    
    /**
     * 5分钟获取一次RTB和PIXEL节点心跳
     */
    public void updateNodeStatusMap(){
    	MDC.put("sift", "control");
    	for (WorkNodeBean node : nodeList) {
    		while(true){
    		NodeStatusBean nodeStatus = MsgControlCenter.recvNodeStatus(node.getName());
    		if(nodeStatus == null){
    			break;
    		}
    		myLog.info(node.getName()+" 节点运行正常!");
    		nodeStatusMap.put(node.getName(), nodeStatus.getLastUpdateTime());
    		}
    		
    	}
    }
    
    /**
     * 每隔10分钟记录一次广告曝光、点击、请求明细
     */
    public void putAdDetailIndb(){
    	MDC.put("sift", "control");
    	Iterator iter = mapMonitorTotal.entrySet().iterator();
    	while (iter.hasNext()) {
    		Map.Entry entry = (Map.Entry) iter.next();
    		String key = (String) entry.getKey();
    		AdFlowStatus status = (AdFlowStatus) entry.getValue();
    		if(mapAd.containsKey(key) && status.getBidNums() != 0){
    			AdBean ad = mapAd.get(key);
    			AdNoticeDetailBean adNoticeDetail = new AdNoticeDetailBean();
    			adNoticeDetail.setAdUid(ad.getAdUid());
    			adNoticeDetail.setAdName(ad.getName());
    			adNoticeDetail.setAdvertiserUid(ad.getAdvertiser().getUid());
    			adNoticeDetail.setAdvertiserName(ad.getAdvertiser().getName());
    			adNoticeDetail.setWinNums(status.getWinNums());
    			adNoticeDetail.setClickNums(status.getClickNums());
    			adNoticeDetail.setBidNums(status.getBidNums());
    			adNoticeDetail.setWinRatio(status.getBidNums()==0?0:(status.getWinNums()*1.0f/status.getBidNums()));
    			adNoticeDetail.setClickRatio(status.getWinNums()==0?0:(status.getClickNums()*1.0f/status.getWinNums()));
    			queue.put(adNoticeDetail);
    		}
    		
    	}
    }
    
    public boolean isNodeDown(String nodeName){
    	MDC.put("sift", "control");
    	Long lastTime = nodeStatusMap.get(nodeName);
    	if(lastTime == null || lastTime == 0){
    		return false;
    	}
    	long nowTime = System.currentTimeMillis();
    	if(nowTime - lastTime >= NODE_DOWN_INTERVAL){//判定节点宕机
    		myLog.info(nodeName+" 节点宕机,移除该节点堆积的任务和广告!");
    		MsgControlCenter.removeAll(nodeName);//移除该节点堆积的任务和广告
    		return true;
    	}
    	
    	return false;
    }

    /**
     * 下发任务 pixel不需要接收任务
     * @param nodeName
     * @param taskList
     */
    private void pushTaskSingleNode(String nodeName, ArrayList<TaskBean> taskList) {
    	if(nodeName != null && !nodeName.contains("pixel"))
    		MsgControlCenter.sendTask(nodeName, taskList, Priority.NORM_PRIORITY);
    }
    
    /**
     * 下发任务 pixel不需要接收任务
     * @param nodeName
     * @param task
     */
    private void pushTaskSingleNode(String nodeName, TaskBean task) {
    	if(nodeName != null && !nodeName.contains("pixel"))
    		MsgControlCenter.sendTask(nodeName, task, Priority.NORM_PRIORITY);
    }

    /**
     * @param nodeName
     * @param beanList
     */
    private void pushAdSingleNode(String nodeName, ArrayList<AdBean> beanList) {
        MsgControlCenter.sendAdBean(nodeName, beanList, Priority.NORM_PRIORITY);
    }


    /**
     * @param adUid
     * @param reason
     * @param isHourOrAll 如果是小时，则只停止该小时的投放，如果是全部，则马上停止后续小时的所有的投放
     */
    public void pauseAd(String adUid, String reason, boolean isHourOrAll) {
    	MDC.put("sift", "control");
    	ArrayList<TaskBean> taskList = new ArrayList<TaskBean>();
            TaskBean task = mapTask.get(adUid);
            if(task.getCommand() == TaskBean.COMMAND_PAUSE ||
            		task.getCommand() == TaskBean.COMMAND_STOP){
            	return;
            }
            putDataToAdLogQueue(adUid, reason, 0);
            myLog.info(reason);
            task.setCommandMemo(reason);
            task.setCommand(TaskBean.COMMAND_PAUSE);
            task.setScope(isHourOrAll ? TaskBean.SCOPE_HOUR : TaskBean.SCOPE_ALL);
            taskList.add(task);
            for (WorkNodeBean node : nodeList) {
            	pushTaskSingleNode(node.getName(), taskList);
            }

    }

    /**
     * @param adUid
     * @param reason
     * @param isHourOrAll 如果是小时，则只停止该小时的投放，如果是全部，则马上停止后续小时的所有的投放
     */
    public void stopAd(String adUid, String reason, boolean isHourOrAll,float reasonStatus) {
    	MDC.put("sift", "control");
    	ArrayList<TaskBean> taskList = new ArrayList<TaskBean>();
            TaskBean task = mapTask.get(adUid);
            if(task.getCommand() == TaskBean.COMMAND_STOP){           
            	return;
            }
            putDataToAdLogQueue(adUid, reason, 0);
            myLog.info(reason);
            task.setCommandMemo(reason);
            task.setCommandResonStatus(reasonStatus);
            task.setCommand(TaskBean.COMMAND_STOP);
            task.setScope(isHourOrAll ? TaskBean.SCOPE_HOUR : TaskBean.SCOPE_ALL);
            taskList.add(task);
            for (WorkNodeBean node : nodeList) {
            	pushTaskSingleNode(node.getName(), taskList);
            }

    }
    
    /**
     * 将广告日志存入广告队列中
     * @param adUid
     * @param reason
     * @param status
     */
    public void putDataToAdLogQueue(String adUid, String reason,int status){
    	AdLogBean adLog = new AdLogBean();
        AdBean ad = mapAd.get(adUid);
        adLog.setAdUid(adUid);
        adLog.setAdName(ad.getName());
        if(ad.getAdvertiser() == null){
        	return;
        }
        adLog.setAdvertiserUid(ad.getAdvertiser().getUid());
        adLog.setAdvertiserName(ad.getAdvertiser().getName());
        adLog.setCreatedAt(new Date());
        adLog.setReason(reason);
        adLog.setStatus(status);
        queue.put(adLog);
    }

}
