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
import java.text.SimpleDateFormat;
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
    private static CheckAdStatus checkAdStatus;

    public static AdFlowControl getInstance() {
        if (control == null) {
            control = new AdFlowControl();
            adProperty = new AdPropertyHandler(control);
            cpcHandler = new CPCHandler(control);
            checkAdStatus = CheckAdStatus.getInstance();
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
    
    private SimpleDateFormat hourDateFM = new SimpleDateFormat("HH");
    
    /**
     * 节点判定宕机时间周期
     */
    private static final int NODE_DOWN_INTERVAL = 30 * 60 * 1000;
    
    
    /**
     * RTB节点线程数
     */
    private static final int RTB_NODE_THREAD_NUMS = 300;
    
    /**
     * PIXCEL节点线程数
     */
    private static final int PIXCEL_NODE_THREAD_NUMS = 1000;

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
    
    public ConcurrentHashMap<String, TaskBean> getMapTask(){
    	return mapTask;
    }
    

    public ConcurrentHashMap<String, AdBean> getMapAdAll() {
		return mapAdAll;
	}

	public ConcurrentHashMap<String, FlowTaskBean> getMapFlowTask() {
		return mapFlowTask;
	}

	public ConcurrentHashMap<String, GroupAdBean> getMapAdGroup() {
		return mapAdGroup;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorTotal() {
		return mapMonitorTotal;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorDaily() {
		return mapMonitorDaily;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorAdGroupTotal() {
		return mapMonitorAdGroupTotal;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorAdGroupRealTotal() {
		return mapMonitorAdGroupRealTotal;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorAdvertiserDaily() {
		return mapMonitorAdvertiserDaily;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapMonitorAdvertiserThresholdDaily() {
		return mapMonitorAdvertiserThresholdDaily;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapThresholdDaily() {
		return mapThresholdDaily;
	}

	public ConcurrentHashMap<String, AdFlowStatus> getMapThresholdHour() {
		return mapThresholdHour;
	}

	/**
     * 广告资源管理
     */
    private static ConcurrentHashMap<String, AdBean> mapAd = null;
    
    private static ConcurrentHashMap<String, AdBean> mapAdAll = null;

    /**
     * 广告任务管理
     */
    private static ConcurrentHashMap<String, TaskBean> mapTask = null;
    
    private static ConcurrentHashMap<String, FlowTaskBean> mapFlowTask = null;

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
     * 广告主的监视器
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorAdvertiserDaily = null;
    
    /**
     * 数据库中设定的广告主日限额指标（天 最高限）
     */
    private static ConcurrentHashMap<String, AdFlowStatus> mapMonitorAdvertiserThresholdDaily = null;

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
    
    private static ConcurrentHashMap<Integer,Long> flowMap = null;
    
    /**
     * adx流控指标
     */
    private static ConcurrentHashMap<String,FlowControlBean> adxFlowControlThresholdMap = null;
    
    /**
     * app流控指标
     */
    private static ConcurrentHashMap<String,FlowControlBean> appFlowControlThresholdMap = null;
    
    /**
     * adx流量缓存
     */
    private static ConcurrentHashMap<String,Long> adxFlowControlMap = null;
    
    /**
     * app流量缓存
     */
    private static ConcurrentHashMap<String,Long> appFlowControlMap = null;
    
    /**
     * 每个广告单元最小竞价次数限制
     */
    private static ConcurrentHashMap<String,Float> adMinBidNumsLimitMap = null;
    
    /**
     * 广告主余额缓存
     */
    private static ConcurrentHashMap<String,Float> advertiserBalanceMap = null;
        
    private DataTransQueue queue;
    
    private static ConcurrentHashMap<String,Float> cpcClieckRatioMap = null; 
    
    private static final float maxCpcClieckRatio = 0.006f;
    
    private static final float maxLimitBidNums = 100;
    
    /**
     * 广告因竞价次数限制停止时间
     */
    private static ConcurrentHashMap<String,Long> adStopTimeMap = null;

    public AdFlowControl() {

        mapAd = new ConcurrentHashMap<>();
        mapAdAll = new ConcurrentHashMap<>();
        mapTask = new ConcurrentHashMap<>();
        mapFlowTask = new ConcurrentHashMap<>();
        mapMonitorDaily = new ConcurrentHashMap<>();
        mapMonitorHour = new ConcurrentHashMap<>();
        adverConsumeMapCurr = new ConcurrentHashMap<>();
        mapThresholdDaily = new ConcurrentHashMap<>();
        mapThresholdHour = new ConcurrentHashMap<>();
        mapAdGroup = new ConcurrentHashMap<>();
        mapMonitorTotal = new ConcurrentHashMap<>();
        reportMapHour = new ConcurrentHashMap<>();
        mapMonitorAdGroupTotal = new ConcurrentHashMap<>();
        mapMonitorAdvertiserDaily = new ConcurrentHashMap<>();
        mapMonitorAdGroupRealTotal = new ConcurrentHashMap<>();
        mapMonitorAdvertiserThresholdDaily = new ConcurrentHashMap<>();
        nodeStatusMap = new ConcurrentHashMap<>();
        cpcClieckRatioMap = new ConcurrentHashMap<>();
        flowMap = new ConcurrentHashMap<>();
        adxFlowControlThresholdMap = new ConcurrentHashMap<>();
        appFlowControlThresholdMap = new ConcurrentHashMap<>();
        adxFlowControlMap = new ConcurrentHashMap<>();
        appFlowControlMap = new ConcurrentHashMap<>();
        adMinBidNumsLimitMap = new ConcurrentHashMap<>();
        advertiserBalanceMap = new ConcurrentHashMap<>();
        adStopTimeMap = new ConcurrentHashMap<>();
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
        
        if(adMinBidNumsLimitMap.containsKey(adUid)){
        	adMinBidNumsLimitMap.put(adUid, adMinBidNumsLimitMap.get(adUid)-addBidNums);
        	float limitBidNums = adMinBidNumsLimitMap.get(adUid);
        	myLog.info("缓存中"+adUid+"竞价次数:"+limitBidNums);
        	if(limitBidNums != 0 && (limitBidNums<0 || (limitBidNums >0 && (limitBidNums <= maxLimitBidNums)))){
        		stopAd(adUid,adUid + "\t可竞得次数剩余100次,停止广告",false,4);
        		adStopTimeMap.put(adUid, System.currentTimeMillis());
        		//重新计算竞价次数限制
//        		checkAdCpmLimit(false);
//        		limitBidNums = adMinBidNumsLimitMap.get(adUid);
//        		if(limitBidNums != 0 && (limitBidNums<0 || (limitBidNums >0 && (limitBidNums <= maxLimitBidNums)))){
//        			stopAd(adUid,adUid + "\t可竞得次数剩余100次,停止广告",false,4);
//        		}
        		
        	}
        	
        }
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
    	       
        switch (type) {
            case 0:
                AdFlowStatus statusHour = mapMonitorHour.get(adUid);
                if (statusHour == null)
                    break;
                //只计算和统计计小时点击率就可以
//                if(pixelType == 0){
//                    statusHour.setUid(adUid);
//                    statusHour.setWinNums(statusHour.getWinNums() + addWinNoticeNums);
//                    statusHour.setMoney(statusHour.getMoney() + addMoney);
//                }else if(pixelType == 1){
//                    statusHour.setUid(adUid);
//                    statusHour.setClickNums(statusHour.getClickNums() +  clickNums);
//                }
                
                statusHour.setUid(adUid);
                statusHour.setWinNums(statusHour.getWinNums() + addWinNoticeNums);
                statusHour.setMoney(statusHour.getMoney() + addMoney);
                statusHour.setClickNums(statusHour.getClickNums() +  clickNums);
                
                break;
            case 1:
                AdFlowStatus statusDaily = mapMonitorDaily.get(adUid);
                if (statusDaily == null)
                    break;
                statusDaily.setUid(adUid);
                statusDaily.setWinNums(statusDaily.getWinNums() + addWinNoticeNums);
                statusDaily.setMoney(statusDaily.getMoney() + addMoney);
                if(mapAdAll.containsKey(adUid)){
                	String groupId = mapAdAll.get(adUid).getGroupId();
                	AdFlowStatus statusGroupAll = mapMonitorAdGroupTotal.get(groupId);
                	statusGroupAll.setMoney(statusGroupAll.getMoney() + addMoney);
                	String advertiserId = mapAdAll.get(adUid).getAdvertiser().getUid();
                	AdFlowStatus statusAdvertiser = mapMonitorAdvertiserDaily.get(advertiserId);
                	statusAdvertiser.setMoney(statusAdvertiser.getMoney() + addMoney);
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
                if(mapAdAll.containsKey(adUid)){   
                	//AdBean adBean = mapAd.get(adUid);
                	AdBean adBeanAllTemp = mapAdAll.get(adUid);
                	String groupId = adBeanAllTemp.getGroupId();
                	AdFlowStatus statusTotalGroupAll = mapMonitorAdGroupRealTotal.get(groupId);
                	statusTotalGroupAll.setMoney(statusTotalGroupAll.getMoney() + addMoney);
                	String mode = adBeanAllTemp.getMode();
                	if("cpc".equals(mode)){
                		cpcHandler.updatePixel(adBeanAllTemp,adUid,addWinNoticeNums,addMoney,clickNums,-1,maxCpcClieckRatio,cpcClieckRatioMap);
                	}
                }
                break;
            case -1:
            	long startTime = System.currentTimeMillis();
            	//CPC模式
            	AdBean adBean = mapAd.get(adUid);
                if(adBean != null && adBean.getMode()!= null && adBean.getMode().equals("cpc")){
                	cpcHandler.updatePixel(adBean,adUid,addWinNoticeNums,addMoney,clickNums,pixelType,maxCpcClieckRatio,cpcClieckRatioMap);
                }
                
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

                //更新广告组和广告主金额状态
                if(mapAd.containsKey(adUid)){
                	String groupId = mapAd.get(adUid).getGroupId();
                	AdFlowStatus statusGroupAll = mapMonitorAdGroupTotal.get(groupId);
                	if(statusGroupAll != null){
                		statusGroupAll.setMoney(statusGroupAll.getMoney() + addMoney);
                	}
                	AdFlowStatus statusTotalGroupAll = mapMonitorAdGroupRealTotal.get(groupId);
                	if(statusTotalGroupAll != null){
                		statusTotalGroupAll.setMoney(statusTotalGroupAll.getMoney() + addMoney);
                	}
                	String advertiserId = mapAd.get(adUid).getAdvertiser().getUid();
                	AdFlowStatus statusAdvertiser = mapMonitorAdvertiserDaily.get(advertiserId);
                	if(statusAdvertiser != null){
                		statusAdvertiser.setMoney(statusAdvertiser.getMoney() + addMoney);
                	}
                }
                
//                if(!isLower && mapTask.containsKey(adUid)){
//                	String reason = "["+adUid+"]竞价价格过低，请提升报价";
//                	stopAd(adUid, reason, false,0);
//                }
                
              //拿当前的指标跟当前的阀值比较，如果超出阀值，则立刻停止任务，并下发任务停止命令
                    
                if(mapAd.containsKey(adUid)){
                	AdBean ad = mapAd.get(adUid);
                	String mode = ad.getMode();

                    AdFlowStatus threshold = mapThresholdHour.get(adUid);
                    AdFlowStatus monitor = mapMonitorHour.get(adUid);
                    //每小时曝光超过了设置的最大阀值，则终止该小时的广告投放
                    if (threshold != null && monitor != null && threshold.getWinNums() != 0 && monitor.getWinNums() >= threshold.getWinNums() * 0.85) {
                        String reason = "#### 小时 CPM 超限，参考指标：" + threshold.getWinNums() + "(CPM)\t" + monitor.getWinNums() + "(CPM) ### " ;
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
                    
                    if(monitorAdGroup != null && thresholdGroupMoney != 0 && quota == 1 && monitorAdGroup.getMoney() >= thresholdGroupMoney * 0.85){
                        //广告组每日限额超限，则发送停止命令，终止该广告投放
                        String reason = "#### 广告组 每日限额 超限，参考指标：" + thresholdGroupMoney + "元(CPM)\t" + monitorAdGroup.getMoney() + "元 (CPM)###";
                        stopAd(adUid, reason, false,0);
                        myLog.error(adUid + "\t" + reason);
                    }
                    
                    if(monitorTotalAdGroup != null && thresholdTotalGroupMoney != 0 && quotaTotal == 1 && monitorTotalAdGroup.getMoney() >= thresholdTotalGroupMoney * 0.85){
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
                    String advertiserId = mapAd.get(adUid).getAdvertiser().getUid();
                    
                    //实时更新余额表
                    if(advertiserBalanceMap.containsKey(advertiserId)){
                    	advertiserBalanceMap.put(advertiserId, advertiserBalanceMap.get(advertiserId)-addMoney);
                    }
                    
                    AdFlowStatus advertiserDaily = mapMonitorAdvertiserDaily.get(advertiserId);
                    AdFlowStatus thresholdAdvertiser = mapMonitorAdvertiserThresholdDaily.get(advertiserId);
                    
                    if(advertiserDaily != null && thresholdAdvertiser != null && thresholdAdvertiser.getMoney() != 0 && advertiserDaily.getMoney() >= thresholdAdvertiser.getMoney() * 0.85){
                    	String reason = "#### 广告主每日金额 超限，参考指标：" + thresholdAdvertiser.getMoney() + "元(CPM)\t" + advertiserDaily.getMoney() + "元(CPM) ###";
                        stopAd(adUid, reason, false,0);
                        myLog.error(advertiserDaily.toString() + "\t" + reason);
                    }
                    if (monitorDaily != null && thresholdDaily != null && thresholdDaily.getMoney() != 0 && monitorDaily.getMoney() >= thresholdDaily.getMoney() * 0.85) {
                        //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                        String reason = "#### 每日金额 超限，参考指标：" + thresholdDaily.getMoney() + "元(CPM)\t" + monitorDaily.getMoney() + "元(CPM) ###";
                        stopAd(adUid, reason, false,0);
                        myLog.error(monitorDaily.toString() + "\t" + reason);
                    }
                    
                    if (monitorDaily != null && thresholdDaily != null && thresholdDaily.getWinNums() != 0 && monitorDaily.getWinNums() >= thresholdDaily.getWinNums() * 0.85) {
                        String reason = "#### 每日 CPM 超限，参考指标：" + thresholdDaily.getWinNums() + "(CPM)\t" + monitorDaily.getWinNums() + "(CPM) ### " ;
                        stopAd(adUid, reason, true,0);
                        myLog.error(monitorDaily.toString() + "\t" + reason);
                    }
                    
                    if("cpc".equalsIgnoreCase(mode)){
                    	//监测 CPC 类型的广告是否可以投放
                        String isOk = cpcHandler.checkAvailable(adUid);
                        if(isOk != null){
                            //String reason = "### cpc 价格设置 过低，超过了成本线，停止广告投放 ###" + auid;
                            stopAd(adUid, isOk, false,0);
                        }
                    	}

                }
                
                myLog.info("监控器监控处理时长:"+(System.currentTimeMillis()-startTime)+"ms");
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
    	
    	//暂时线程启动为如下，后续改为线程池
    	
    	for(String nodeName: nodeNameList){
    		if(nodeName != null && nodeName.contains("rtb-")){
    			myLog.info("从"+nodeName+"中获取bids个数线程(线程数:"+RTB_NODE_THREAD_NUMS+")开启......");
    			for(int i=0;i<RTB_NODE_THREAD_NUMS;i++){
    				new Thread(new GainDataFromRTBQueue(nodeName)).start();
    			}
    		}
    		if(nodeName != null && nodeName.contains("pixel-")){
    			myLog.info("从"+nodeName+"中获取最新 wins 和 金额消费情况线程(线程数:"+PIXCEL_NODE_THREAD_NUMS+")开启......");
    			for(int i=0;i<PIXCEL_NODE_THREAD_NUMS;i++){
    				new Thread(new GainDataFromPIXCELQueue(nodeName)).start();
    			}
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
    	
    	putAdDetailIndbPerHour();
    	
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
            if (scope == TaskBean.SCOPE_HOUR && commandCode == TaskBean.COMMAND_PAUSE && checkAdStatus.getAdStatus(auid, false)) {
                bean.setCommand(TaskBean.COMMAND_START);
                putDataToAdLogQueue(auid, "小时计数器清零,广告开启", 1);
            }
        }
        
        ArrayList<FlowTaskBean> flowTaskList  = new ArrayList<FlowTaskBean>();
        
        adxFlowControlMap.clear();
        appFlowControlMap.clear();
        
        for(String aid:mapFlowTask.keySet()){
        	FlowTaskBean bean = mapFlowTask.get(aid);
        	if(bean != null && bean.getCommand() == FlowTaskBean.COMMAND_PAUSE){
        		bean.setCommand(FlowTaskBean.COMMAND_START);
        		flowTaskList.add(bean);
        		myLog.info("ADX OR APP ID["+aid+"],小时计数器清零,流控重新清算!");
        	}
        }
        
        if(!flowTaskList.isEmpty()){
        	for(WorkNodeBean node:nodeList){
        		if(node.getName().contains("rtb")){
        			pushFlowTask(node.getName(), flowTaskList);
        		}
        	}
        }

        //}
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
            for (String key : mapMonitorDaily.keySet()) {
                AdFlowStatus status = mapMonitorDaily.get(key);
                status.setBidNums(0);
                status.setMoney(0);
                status.setWinNums(0);
            }
            for (String key : mapMonitorHour.keySet()) {
                AdFlowStatus status = mapMonitorHour.get(key);
                status.setBidNums(0);
                status.setMoney(0);
                status.setWinNums(0);
            }
            for (String key : mapMonitorAdGroupTotal.keySet()) {
                AdFlowStatus status = mapMonitorAdGroupTotal.get(key);
                status.setBidNums(0);
                status.setMoney(0);
                status.setWinNums(0);
            }
            for (String key : mapMonitorAdvertiserDaily.keySet()) {
                AdFlowStatus status = mapMonitorAdvertiserDaily.get(key);
                status.setBidNums(0);
                status.setMoney(0);
                status.setWinNums(0);
            }
//            mapMonitorDaily.clear();
//            mapMonitorHour.clear();
//            mapMonitorAdGroupTotal.clear();
            for (ResultMap map : rl) {
                String auid = map.getString("uid");
                String name = map.getString("name");
                String groupId = map.getString("group_uid");
                String adverUid = map.getString("advertiser_uid");
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
                mapMonitorAdGroupTotal.put(groupId, status3);
                
                AdFlowStatus status4 = new AdFlowStatus();
                status4.reset();
                status4.setUid(auid);
                status4.setName(name);
                mapMonitorAdvertiserDaily.put(adverUid, status4);

                
                if(mapTask.containsKey(auid)){
                	TaskBean task = mapTask.get(auid);
                	if (task.getCommand() != TaskBean.COMMAND_START && checkAdStatus.getAdStatus(auid, false)) {
                		if(task.getCommandResonStatus() == 4){
                			//广告不开启
//                			checkAdCpmLimit(false);
//                			float limitBidNums = adMinBidNumsLimitMap.get(auid);
//                			if(limitBidNums != 0 && (limitBidNums<0 || (limitBidNums >0 && (limitBidNums <= maxLimitBidNums)))){
//                				//广告不开启
//                			}else{
//                				task.setCommand(TaskBean.COMMAND_START);
//                				task.setCommandResonStatus(0);
//                    			putDataToAdLogQueue(auid, "天计数器清零,广告开启", 1);
//                			}
                		}else{
                			task.setCommand(TaskBean.COMMAND_START);
                			putDataToAdLogQueue(auid, "天计数器清零,广告开启", 1);
                		}
                	}
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
        long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        timeBefore  = timeBefore / 1000;
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
                if(balance.doubleValue() <= 0){
                    lowBalanceAdSet.add(auid);
                }else{
                	if(mapTask.containsKey(auid)){
                		TaskBean task = mapTask.get(auid);
                		if(task.getCommand() == TaskBean.COMMAND_STOP && task.getCommandResonStatus() == 1){
                			task.setCommand(TaskBean.COMMAND_START);
                			task.setCommandResonStatus(0);
                			myLog.info("广告["+auid+"]广告主已充值,广告开启");
                			putDataToAdLogQueue(auid, "广告主已充值,广告开启", 1);
                		}
                	}
                }
                //广告主账户的每日限额
                BigDecimal quotaMoneyPerDay = balanceMap.getBigDecimal("quota_amount");
                boolean advertiserQuota = balanceMap.getBoolean("quota");


                int winNumsHour = map.getInteger("cpm_hourly");
                int winNumsDaily = map.getInteger("cpm_daily");
                // 当前广告表中的针对广告的限额
                BigDecimal money = map.getBigDecimal("quota_amount");
                
                boolean adQuota = map.getBoolean("quota");

                // 如果广告主中的每日限额比广告的还小，以小的为准
                if (advertiserQuota && adQuota && quotaMoneyPerDay.floatValue() <= money.floatValue()) {
                    money = quotaMoneyPerDay;
                }
                if(advertiserQuota && !adQuota){
                	money = quotaMoneyPerDay;
                }
                //如果这个账户的余额比每天或小时的限额还小，则赋予小的值
                if (balance.floatValue() != 0 && balance.floatValue() <= money.floatValue()) {
                    money = balance;
                }
                
                Integer updatedAt = balanceMap.getInteger("updated_at");

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
               
                AdFlowStatus status5 = new AdFlowStatus();                
                status5.setMoney(quotaMoneyPerDay.floatValue()*1000);
                mapMonitorAdvertiserThresholdDaily.put(adviserId, status5);

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
                
                if(updatedAt != null && updatedAt != 0 && updatedAt >= timeBefore){
                	if(mapTask.containsKey(auid)){
                		TaskBean task = mapTask.get(auid);
                		if(task.getCommand() != TaskBean.COMMAND_START && checkAdStatus.getAdStatus(auid, false)){
                			if(task.getCommandResonStatus() == 4){
                				//广告不开启
//                    			checkAdCpmLimit(false);
//                    			float limitBidNums = adMinBidNumsLimitMap.get(auid);
//                    			if(limitBidNums != 0 && (limitBidNums<0 || (limitBidNums >0 && (limitBidNums <= maxLimitBidNums)))){
//                    				//广告不开启
//                    			}else{
//                    				task.setCommand(TaskBean.COMMAND_START);
//                    				task.setCommandResonStatus(0);
//                    				putDataToAdLogQueue(auid, "广告主信息修改,广告开启", 1);
//                    			}
                    		}else{
                    			task.setCommand(TaskBean.COMMAND_START);
                    			putDataToAdLogQueue(auid, "广告主信息修改,广告开启", 1);
                    		}
                			
                		}
                	}
                }
            }
            return lowBalanceAdSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    
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
					stopAd(adUid, adUid+"广告被关闭", false,2);
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
    
    public void loadAllAdInterval(boolean isInitial){
    	MDC.put("sift", "control");
    	ConcurrentHashMap<String, ReportBean> reportMapDaily = null;
    	long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        try{
        	if (isInitial) {
                timeBefore = 0;
            }
        	ResultList adList = taskService.queryAllAdTotal(timeBefore);
        	//加载 小时 历史消费金额
            reportMapHour = taskService.statAdCostHour();
            //加载 天 历史消费金额
            reportMapDaily = taskService.statAdCostDaily();
            //加载 总 历史消费金额
            reportMapTotal = taskService.statAdCostTotal();
            for (ResultMap map : adList) {
                AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));
                //广告组
                String groupId = map.getString("group_uid");
                ad.setGroupId(groupId);
                String adUid = ad.getAdUid();

                String adverUid = map.getString("advertiser_uid");
                
                String mode = map.getString("mode");
                ad.setMode(mode);
                if("cpc".equalsIgnoreCase(mode)){
                	ad.setCpcPrice(map.getBigDecimal("price").floatValue());
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

                //根据 广告主ID 获得 广告主
                AdvertiserBean adver = taskService.queryAdverByUid(adverUid);
                ad.setAdvertiser(adver);
                mapAdAll.put(adUid, ad);
                if (isInitial ||!mapMonitorTotal.containsKey(adUid)) {
                    //初始化所有的监控
                	if(!mapMonitorHour.containsKey(adUid)){
                		AdFlowStatus statusHour = new AdFlowStatus();
                		mapMonitorHour.put(adUid,statusHour);
                	}
                	if(!mapMonitorDaily.containsKey(adUid)){
                		AdFlowStatus statusDay = new AdFlowStatus();
                		mapMonitorDaily.put(adUid,statusDay);
                	}
                	if(!mapMonitorTotal.containsKey(adUid)){
                		AdFlowStatus statusAll = new AdFlowStatus();
                		mapMonitorTotal.put(adUid,statusAll);
                	}
                	if(!mapMonitorAdGroupTotal.containsKey(groupId)){
                		AdFlowStatus statusAdGroup = new AdFlowStatus();
                		mapMonitorAdGroupTotal.put(groupId,statusAdGroup);
                	}
                	if(!mapMonitorAdGroupRealTotal.containsKey(groupId)){
                		AdFlowStatus statusAdGroup = new AdFlowStatus();
                		mapMonitorAdGroupRealTotal.put(groupId, statusAdGroup);
                	}
                	if(!mapMonitorAdvertiserDaily.containsKey(adverUid)){
                		AdFlowStatus statusAdver = new AdFlowStatus();
                		mapMonitorAdvertiserDaily.put(adverUid, statusAdver);
                	}

                    if (reportMapHour.size() > 0) {
                        ReportBean report = reportMapHour.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, report.getImpNums(), expense.floatValue() * 1000, 0,report.getClickNums(),0,true);
                        }
                    }

                    if (reportMapDaily.size() > 0) {
                        ReportBean report = reportMapDaily.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, report.getImpNums(), expense.floatValue() * 1000, 1,report.getClickNums(),0,true);
                        }
                    }

                    if (reportMapTotal.size() > 0) {
                        ReportBean report = reportMapTotal.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, report.getImpNums(), expense.floatValue() * 1000, 2,report.getClickNums(),0,true);
                            
                        }
                    }
                    
                }
                
            }
        	myLog.info("广告单元每日监视器:"+mapMonitorDaily);
        	myLog.info("广告组每日监视器:"+mapMonitorAdGroupTotal);
        	myLog.info("广告主每日监视器:"+mapMonitorAdvertiserDaily);
        	myLog.info("广告组总监视器:"+mapMonitorAdGroupRealTotal);
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每隔 10 分钟
     * 从数据库中加载所有的广告,广告主、广告素材和广告配额
     */
    public void loadAdInterval(boolean isInitial) {
    	MDC.put("sift", "control");
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
                    if(!isInitial){
                    List<String> adUidList = taskService.queryAdByGroupId(group.getGroupId());
                    for(String uid:adUidList){
                    	if(mapTask.containsKey(uid)){
                    		TaskBean task = mapTask.get(uid);
                    		if(task.getCommand() != TaskBean.COMMAND_START){
                    			task.setCommand(TaskBean.COMMAND_START);
                    		}
                    	}
                    }
                    }
                }
            }
            //加载广告信息
            ResultList adList = taskService.queryAdByUpTime(timeBefore);
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
                int moneyArrears = map.getInteger("money_arrears");
                ad.setMoneyArrears(moneyArrears * 1000);
                //出价模式
                ad.setMode(mode);
                if("cpc".equalsIgnoreCase(mode)){
                	ad.setCpcPrice(map.getBigDecimal("price").floatValue());
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
                ad.setScheduleTime(timeScheTxt);
                int[][] timeScheduling = TimeSchedulingUtil.timeTxtToMatrix(timeScheTxt);
                ad.setTimeSchedulingArr(timeScheduling);
                ad.setTimestamp(map.getInteger("created_at"));
                //如果是价格和配额发生了变化，直接通知
                //如果素材发生了变化，直接通知
                mapAd.put(adUid, ad);
              
              //AdBean 里面的加速投放为 0 ,对应数据库里面 1（ALL） ，匀速相反
                TaskBean task = null;
                if(mapTask.containsKey(adUid)){
                    task = mapTask.get(adUid);
                    if(task.getCommand() != TaskBean.COMMAND_START && checkAdStatus.getAdStatus(adUid, true)){
                    	if(task.getCommandResonStatus() == 4){
                    		//广告不开启
//                			checkAdCpmLimit(false);
//                			float limitBidNums = adMinBidNumsLimitMap.get(adUid);
//                			if(limitBidNums != 0 && (limitBidNums<0 || (limitBidNums >0 && (limitBidNums <= maxLimitBidNums)))){
//                				//广告不开启
//                			}else{
//                				task.setCommand(TaskBean.COMMAND_START);
//                				task.setCommandResonStatus(0);
//                				putDataToAdLogQueue(adUid, "广告被修改,广告开启", 1);
//                    		
//                			}
                		}else{
                			task.setCommand(TaskBean.COMMAND_START);
                			putDataToAdLogQueue(adUid, "广告被修改,广告开启", 1);
                		}
                    //task.setCommand(TaskBean.COMMAND_START);
                    if(task.getCommandResonStatus() == 2){
                    	task.setCommandResonStatus(0);
                    }
                    
                	}
                    int scope = -1;
                    if(ad.getSpeedMode() == 0)
                        scope = 1;
                    else if(ad.getSpeedMode() == 1)
                        scope = 0;
                    task.setScope(scope);
                }else{
                    task = new TaskBean(adUid);
                    cpcHandler.updateIndicator(adUid);
                    if(isInitial){
                    	if(!checkAdStatus.getAdStatus(adUid, false)){
                    		task.setCommand(TaskBean.COMMAND_STOP);
                    	}
                    }else{
                    putDataToAdLogQueue(adUid, "广告首次下发,广告开启", 1);
                    }
                }
                mapTask.put(adUid, task);
                counter++;

//                if(lowBalanceAdList!= null && lowBalanceAdList.contains(adUid)){
//                    stopAd(adUid,adUid + "\t广告余额不足，请联系广告主充值。。",false,1);
////                    myLog.error(adUid + "\t广告余额不足，请联系广告主充值。。");
//                    continue;
//                }
                
            }
            
            if(lowBalanceAdList!= null){
            	for(String adUid:lowBalanceAdList){
            		if(mapAd.containsKey(adUid)){
            		stopAd(adUid,adUid + "\t广告余额不足，请联系广告主充值。。",false,1);
            		myLog.error(adUid + "\t广告余额不足，请联系广告主充值。。");
            		}
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
                       
            if(task.getCommand() == TaskBean.COMMAND_START && task.getCommandResonStatus() != 4){
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
     * 10秒钟检查一次广告是否因竞价次数限制停止超过1个小时
     */
    public void checkAdLimitStop(){
    	
    	Iterator iter = adStopTimeMap.entrySet().iterator();
    	long now = System.currentTimeMillis();
		while(iter.hasNext()){
			Map.Entry<String, Long> entry = (Map.Entry) iter.next();
			String adUid = entry.getKey();
			long stopTime = entry.getValue();
			if(now - stopTime >= 1 * 60 * 60 * 1000){
			myLog.info(adUid+"开始重新更新竞价次数缓存");
			adStopTimeMap.put(adUid, now);
			checkSingleAdCpmLimit(adUid);
			float limitBidNums = adMinBidNumsLimitMap.get(adUid);   			
        	myLog.info("缓存中"+adUid+"竞价次数:"+limitBidNums);
        	if(limitBidNums != 0 && (limitBidNums<0 || (limitBidNums >0 && (limitBidNums <= maxLimitBidNums)))){
        		myLog.info(adUid+"因竞价次数限制已经停止,不重复停止!");
        	}else{
        		TaskBean task = mapTask.get(adUid);
        		if( task != null && task.getCommand() !=TaskBean.COMMAND_START  && (task.getCommandResonStatus() == 4)){
        			task.setCommand(TaskBean.COMMAND_START);
    				task.setCommandResonStatus(0);
    				putDataToAdLogQueue(adUid, "达到1小时,广告竞价次数还未到限制,广告开启", 1);
        		}
        	}
			}
		}
    }
    
    /**
     * 检查广告单元CPM限制
     */
    public void checkAdCpmLimit(boolean isInit){
    	MDC.put("sift", "control");
    	Iterator iter = mapAd.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, AdBean> entry = (Map.Entry) iter.next();
			String adUid = entry.getKey();
			AdBean ad = mapAd.get(adUid);
			String groupId = ad.getGroupId();
			String advertiserId = ad.getAdvertiser().getUid();
			float tempMoney = -1f;
			AdFlowStatus adtThresholdDaily = mapThresholdDaily.get(adUid);
			float adtThresholdDailyMoney = 0.0f;
			if(adtThresholdDaily != null)
				adtThresholdDailyMoney =  adtThresholdDaily.getMoney();
			if(adtThresholdDailyMoney != 0){
				AdFlowStatus adMonitorDaily = mapMonitorDaily.get(adUid);
				float adMonitorDailyMoney = 0.0f;
				if(adMonitorDaily != null)
					adMonitorDailyMoney = adMonitorDaily.getMoney();
				//广告单元每日限额余额
				float adDailyMoney = adtThresholdDailyMoney - adMonitorDailyMoney;
				tempMoney = adDailyMoney;
			}
			
			float adGroupThresholdDailyMoney = mapAdGroup.get(groupId).getQuotaMoney().floatValue()*1000;
                        
            if(adGroupThresholdDailyMoney != 0){
            	 AdFlowStatus adGroupMonitorDaily = mapMonitorAdGroupTotal.get(groupId);
            	 float adGroupMonitorDailyMoney = 0.0f;
            	 if(adGroupMonitorDaily != null){
            		 adGroupMonitorDailyMoney = adGroupMonitorDaily.getMoney();
            		 //广告组每日限额余额
            		 float adGroupDailyMoney = adGroupThresholdDailyMoney - adGroupMonitorDailyMoney;
            		 if(tempMoney > adGroupDailyMoney){
            			 tempMoney = adGroupDailyMoney;
            		 }
            		 
            	 }
            }
            
            float adGroupThresholdTotalMoney = mapAdGroup.get(groupId).getQuotaTotalMoney().floatValue()*1000;
            
            if(adGroupThresholdTotalMoney != 0){
            	AdFlowStatus adGroupMonitorTotal = mapMonitorAdGroupRealTotal.get(groupId);
            	float adGroupMonitorTotalMoney = 0.0f;
            	if(adGroupMonitorTotal != null){
            		adGroupMonitorTotalMoney = adGroupMonitorTotal.getMoney();
            		//广告组总限额余额
            		float adGroupTotalMoney = adGroupThresholdTotalMoney - adGroupMonitorTotalMoney;
            		if(tempMoney > adGroupTotalMoney){
           			 tempMoney = adGroupTotalMoney;
           		 }
            	}
            }
            
            AdFlowStatus advertiserThresholdDaily = mapMonitorAdvertiserThresholdDaily.get(advertiserId);
            if(advertiserThresholdDaily != null){
            	float advertiserThresholdDailyMoney = advertiserThresholdDaily.getMoney();
            	if(advertiserThresholdDailyMoney != 0){
            		AdFlowStatus advertiserMonitorDaily = mapMonitorAdvertiserDaily.get(advertiserId);
            		float advertiserMonitorDailyMoney = 0.0f;
            		if(advertiserMonitorDaily != null){
            			advertiserMonitorDailyMoney = advertiserMonitorDaily.getMoney();
            			//广告主日限额余额
            			float advertiserDailyMoney = advertiserThresholdDailyMoney - advertiserMonitorDailyMoney;
            			if(tempMoney > advertiserDailyMoney){
            				tempMoney = advertiserDailyMoney;
            			}
            		}
            	}
            }
            
            if(isInit){
	            ResultMap balanceMap = taskService.queryAdviserAccountById(advertiserId);
	            if(balanceMap != null){
	            	//广告主账户中的余额
	            	BigDecimal balance = balanceMap.getBigDecimal("balance");
	            	float balancePrice = balance.floatValue()*1000;
	            	advertiserBalanceMap.put(advertiserId, balancePrice);
	            	if(tempMoney > balancePrice){
	            		tempMoney = balancePrice;
	            	}
	            }
            }else{
            	if(advertiserBalanceMap.containsKey(advertiserId)){
            		float balancePrice = advertiserBalanceMap.get(advertiserId);
            		if(tempMoney > balancePrice){
            			tempMoney = balancePrice;
            		}
            	}
            }
            
            float bidNums = (float) (tempMoney / (10 * 0.7));
            
            adMinBidNumsLimitMap.put(adUid, bidNums);
		}
		
		myLog.info("广告可竞价次数:"+adMinBidNumsLimitMap);
    }
    
    /**
     * 检查单个广告单元CPM限制
     */
    public void checkSingleAdCpmLimit(String adUid){
    	MDC.put("sift", "control");
			AdBean ad = mapAd.get(adUid);
			String groupId = ad.getGroupId();
			String advertiserId = ad.getAdvertiser().getUid();
			float tempMoney = -1f;
			AdFlowStatus adtThresholdDaily = mapThresholdDaily.get(adUid);
			float adtThresholdDailyMoney = 0.0f;
			if(adtThresholdDaily != null)
				adtThresholdDailyMoney =  adtThresholdDaily.getMoney();
			if(adtThresholdDailyMoney != 0){
				AdFlowStatus adMonitorDaily = mapMonitorDaily.get(adUid);
				float adMonitorDailyMoney = 0.0f;
				if(adMonitorDaily != null)
					adMonitorDailyMoney = adMonitorDaily.getMoney();
				//广告单元每日限额余额
				float adDailyMoney = adtThresholdDailyMoney - adMonitorDailyMoney;
				tempMoney = adDailyMoney;
			}
			
			float adGroupThresholdDailyMoney = mapAdGroup.get(groupId).getQuotaMoney().floatValue()*1000;
                        
            if(adGroupThresholdDailyMoney != 0){
            	 AdFlowStatus adGroupMonitorDaily = mapMonitorAdGroupTotal.get(groupId);
            	 float adGroupMonitorDailyMoney = 0.0f;
            	 if(adGroupMonitorDaily != null){
            		 adGroupMonitorDailyMoney = adGroupMonitorDaily.getMoney();
            		 //广告组每日限额余额
            		 float adGroupDailyMoney = adGroupThresholdDailyMoney - adGroupMonitorDailyMoney;
            		 if(tempMoney > adGroupDailyMoney){
            			 tempMoney = adGroupDailyMoney;
            		 }
            		 
            	 }
            }
            
            float adGroupThresholdTotalMoney = mapAdGroup.get(groupId).getQuotaTotalMoney().floatValue()*1000;
            
            if(adGroupThresholdTotalMoney != 0){
            	AdFlowStatus adGroupMonitorTotal = mapMonitorAdGroupRealTotal.get(groupId);
            	float adGroupMonitorTotalMoney = 0.0f;
            	if(adGroupMonitorTotal != null){
            		adGroupMonitorTotalMoney = adGroupMonitorTotal.getMoney();
            		//广告组总限额余额
            		float adGroupTotalMoney = adGroupThresholdTotalMoney - adGroupMonitorTotalMoney;
            		if(tempMoney > adGroupTotalMoney){
           			 tempMoney = adGroupTotalMoney;
           		 }
            	}
            }
            
            AdFlowStatus advertiserThresholdDaily = mapMonitorAdvertiserThresholdDaily.get(advertiserId);
            if(advertiserThresholdDaily != null){
            	float advertiserThresholdDailyMoney = advertiserThresholdDaily.getMoney();
            	if(advertiserThresholdDailyMoney != 0){
            		AdFlowStatus advertiserMonitorDaily = mapMonitorAdvertiserDaily.get(advertiserId);
            		float advertiserMonitorDailyMoney = 0.0f;
            		if(advertiserMonitorDaily != null){
            			advertiserMonitorDailyMoney = advertiserMonitorDaily.getMoney();
            			//广告主日限额余额
            			float advertiserDailyMoney = advertiserThresholdDailyMoney - advertiserMonitorDailyMoney;
            			if(tempMoney > advertiserDailyMoney){
            				tempMoney = advertiserDailyMoney;
            			}
            		}
            	}
            }
            	if(advertiserBalanceMap.containsKey(advertiserId)){
            		float balancePrice = advertiserBalanceMap.get(advertiserId);
            		if(tempMoney > balancePrice){
            			tempMoney = balancePrice;
            		}
            	}
            
            float bidNums = (float) (tempMoney / (10 * 0.7));
            
            adMinBidNumsLimitMap.put(adUid, bidNums);
		
		myLog.info("广告可竞价次数:"+adMinBidNumsLimitMap);
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
     * 每隔1小时记录一次广告曝光、点击、请求明细
     */
    public void putAdDetailIndbPerHour(){
    	MDC.put("sift", "control");
    	Iterator iter = mapMonitorHour.entrySet().iterator();
    	while (iter.hasNext()) {
    		Map.Entry entry = (Map.Entry) iter.next();
    		String key = (String) entry.getKey();
    		AdFlowStatus status = (AdFlowStatus) entry.getValue();
    		if(mapAd.containsKey(key)){
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
    
    /**
     * 每隔一天获取一次流量小时明细
     */
    public void updateFlow(){
    	MDC.put("sift", "control");
    	ConcurrentHashMap<Integer,Long> map = taskService.getNoticeDetailByHourPerDay();
    	
    	//二十四小时流量明细
    	for(int i=0;i<24;i++){
    		if(map.containsKey(i)){
    			flowMap.put(i, map.get(i));
    		}else{
    			flowMap.put(i, 0L);
    		}
    	}
    }
    
    /**
     * 每隔10分钟更新一次ADX和APP流量控制
     */
    public void updateAdxAndAppFlowControl(boolean isInit){
    	MDC.put("sift", "control");
    	long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        if(isInit)
        	timeBefore = 0;
    	ArrayList<FlowTaskBean> flowTaskList = new ArrayList<FlowTaskBean>();
    	List<FlowControlBean> adxFlowControlList = taskService.getAdxFlowControl(timeBefore);
    	List<FlowControlBean> appFlowControlList = taskService.getAppFlowControl(timeBefore);
    	if(adxFlowControlList != null && !adxFlowControlList.isEmpty()){
    		for(FlowControlBean adxFlowControl:adxFlowControlList){
    			//首次启动或者缓存中没有ADXID
    			if(isInit || !mapFlowTask.containsKey(adxFlowControl.getAid())){
    				FlowTaskBean flowTask = new FlowTaskBean();
    				flowTask.setAid(adxFlowControl.getAid());
    				if(adxFlowControl.getPutAdStatus() == 0){
    					flowTask.setCommand(FlowTaskBean.COMMAND_STOP);
    				}else{
    					flowTask.setCommand(FlowTaskBean.COMMAND_START);
    				}   				
    				mapFlowTask.put(adxFlowControl.getAid(), flowTask);
    				flowTaskList.add(flowTask);
    			}else{
    				if(adxFlowControl.getPutAdStatus() == 0){//ADX广告投放关闭
    					FlowTaskBean task = mapFlowTask.get(adxFlowControl.getAid());
    					if(task.getCommand() != FlowTaskBean.COMMAND_STOP){//缓存中ADX广告任务为未关闭
    						task.setCommand(FlowTaskBean.COMMAND_STOP);
    						myLog.info("AID["+adxFlowControl.getAid()+"]ADX投放任务关闭!");
    						flowTaskList.add(task);
    					}
    				}else if(adxFlowControl.getPutAdStatus() == 1){//ADX广告投放开启
    					FlowTaskBean task = mapFlowTask.get(adxFlowControl.getAid());
    					if(task.getCommand() != FlowTaskBean.COMMAND_START){//缓存中ADX广告任务为未开启
    						task.setCommand(FlowTaskBean.COMMAND_START);
    						myLog.info("AID["+adxFlowControl.getAid()+"]ADX投放任务开启!");
    						flowTaskList.add(task);
    					}
    				}
    			}
    			adxFlowControlThresholdMap.put(adxFlowControl.getAid(), adxFlowControl);
    		}
    	}
    	if(appFlowControlList != null && !appFlowControlList.isEmpty()){
    		for(FlowControlBean appFlowControl:appFlowControlList){
    			//首次启动或者缓存中没有APPID
    			if(isInit ||!mapFlowTask.containsKey(appFlowControl.getAid())){
    				FlowTaskBean flowTask = new FlowTaskBean();
    				flowTask.setAid(appFlowControl.getAid());
    				flowTask.setAid(appFlowControl.getAid());
    				if(appFlowControl.getPutAdStatus() == 0){
    					flowTask.setCommand(FlowTaskBean.COMMAND_STOP);
    				}else{
    					flowTask.setCommand(FlowTaskBean.COMMAND_START);
    				}
    				mapFlowTask.put(appFlowControl.getAid(), flowTask);
    				flowTaskList.add(flowTask);
    			}else{
    				if(appFlowControl.getPutAdStatus() == 0){//APP广告投放关闭
    					FlowTaskBean task = mapFlowTask.get(appFlowControl.getAid());
    					if(task.getCommand() != FlowTaskBean.COMMAND_STOP){//缓存中APP广告任务为未停止
    						task.setCommand(FlowTaskBean.COMMAND_STOP);
    						myLog.info("AID["+appFlowControl.getAid()+"]APP投放任务关闭!");
    						flowTaskList.add(task);
    					}
    				}else if(appFlowControl.getPutAdStatus() == 1){//APP广告投放开启
    					FlowTaskBean task = mapFlowTask.get(appFlowControl.getAid());
    					if(task.getCommand() != FlowTaskBean.COMMAND_START){//缓存中APP广告任务为未开启
    						task.setCommand(FlowTaskBean.COMMAND_START);
    						myLog.info("AID["+appFlowControl.getAid()+"]APP投放任务开启!");
    						flowTaskList.add(task);
    					}
    				}
    			}
    			appFlowControlThresholdMap.put(appFlowControl.getAid(), appFlowControl);
    		}
    	}
    	
    	if(!flowTaskList.isEmpty()){
        	for(WorkNodeBean node:nodeList){
        		if(node.getName().contains("rtb")){
        			pushFlowTask(node.getName(), flowTaskList);
        		}
        	}
        }
    	
    }
    
    /**
     * 每隔10分钟下发一次任务关闭的adx或app
     */
    public void pushFlowTaskPerTenMinute(){
    	ArrayList<FlowTaskBean> flowTaskList = new ArrayList<FlowTaskBean>();
    	Iterator iter = mapFlowTask.entrySet().iterator();
    	while(iter.hasNext()){
    		Map.Entry entry = (Map.Entry) iter.next();
    		String aid = (String) entry.getKey();
    		FlowTaskBean task = mapFlowTask.get(aid);
    		if(task.getCommand() != FlowTaskBean.COMMAND_START){
    			flowTaskList.add(task);
    		}
    	}
    	if(!flowTaskList.isEmpty()){
    		for(WorkNodeBean node:nodeList){
        		if(node.getName().contains("rtb")){
        			pushFlowTask(node.getName(), flowTaskList);
        		}
        	}
    	}
    	
    	flowTaskList = null;
    }
    
    /**
     * 每隔30秒获取一次ADX与APP流量,检查流量是否超限
     */
    public void pullAndCheckFlowControl(){
    	MDC.put("sift", "control");
    	for(WorkNodeBean node:nodeList){
    		if(node.getName().contains("rtb")){
    			
    			//更新ADX流量缓存
    			Map<String,Long> adxMap = MsgControlCenter.recvAdxFlow(node.getName());
    			if(adxMap !=null && !adxMap.isEmpty()){    				
	    			Iterator iterAdx = adxMap.entrySet().iterator();
	    			while(iterAdx.hasNext()){
	    				Map.Entry entry = (Map.Entry) iterAdx.next();
	    				String key = (String) entry.getKey();
	    				Long value = (Long) entry.getValue();
	    				Long flows = adxFlowControlMap.get(key);
	    				if(flows != null){
	    					adxFlowControlMap.put(key, adxFlowControlMap.get(key) + value);
	    				}else{
	    					adxFlowControlMap.put(key, value);
	    				}
	    			}
    			}
    			
    			//更新APP流量缓存
    			Map<String,Long> appMap = MsgControlCenter.recvAppFlow(node.getName());
    			if(appMap !=null && !appMap.isEmpty()){
	    			Iterator iterApp = appMap.entrySet().iterator();
	    			while(iterApp.hasNext()){
	    				Map.Entry entry = (Map.Entry) iterApp.next();
	    				String key = (String) entry.getKey();
	    				Long value = (Long) entry.getValue();
	    				Long flows = appFlowControlMap.get(key);
	    				if(flows != null){
	    					appFlowControlMap.put(key, appFlowControlMap.get(key) + value);
	    				}else{
	    					appFlowControlMap.put(key, value);
	    				}
	    			}
    			}
    		}
    	}
    	
    	//检查流量是否超限
    	checkFlowQuota();
    }
    
    /**
     * 检查流量是否超限
     */
    public void checkFlowQuota(){
    	MDC.put("sift", "control");
    	String hour = hourDateFM.format(new Date());
    	long totalFlows = flowMap.get(Integer.parseInt(hour));  	
    	ArrayList<FlowTaskBean> flowTaskList = new ArrayList<FlowTaskBean>();
    	
    	//检查adx流量是否超限
    	Iterator iterAdxThreshold = adxFlowControlThresholdMap.entrySet().iterator();
    	
    	while(iterAdxThreshold.hasNext()){
    		Map.Entry entry = (Map.Entry) iterAdxThreshold.next();
    		String key = (String) entry.getKey();
    		FlowControlBean flowControlBean = (FlowControlBean) entry.getValue();
    		int status = flowControlBean.getStatus();
    		int lowFlows = flowControlBean.getLowFlows();
    		int ratio = flowControlBean.getFlowControlRatio();
    		long tempFlows = (long) (totalFlows * (ratio/100.0f));
    		Long flows = adxFlowControlMap.get(key);
    		if(flows != null && status == 1 && totalFlows != 0 && ratio != 0 && flows >= lowFlows && flows >= tempFlows){
    			//停止该ADX的流量
    			FlowTaskBean bean = mapFlowTask.get(key);
    			if(bean != null && bean.getCommand() == FlowTaskBean.COMMAND_START){
    				bean.setCommand(FlowTaskBean.COMMAND_PAUSE);
    				myLog.info("开始暂停ADX["+key+"]流量");
    				flowTaskList.add(bean);
    			}
    		}
    	}
    	
    	//检查app流量是否超限
    	Iterator iterAppThreshold = appFlowControlThresholdMap.entrySet().iterator();
    	while(iterAppThreshold.hasNext()){
    		Map.Entry entry = (Map.Entry) iterAppThreshold.next();
    		String key = (String) entry.getKey();
    		FlowControlBean flowControlBean = (FlowControlBean) entry.getValue();
    		int status = flowControlBean.getStatus();
    		int lowFlows = flowControlBean.getLowFlows();
    		int ratio = flowControlBean.getFlowControlRatio();
    		long tempFlows = (long) (totalFlows * (ratio/100.0f));
    		Long flows = appFlowControlMap.get(key);
    		if(flows != null && status == 1 && totalFlows != 0 && ratio != 0 && flows >= lowFlows && flows >= tempFlows){
    			//停止该APP的流量
    			FlowTaskBean bean = mapFlowTask.get(key);
    			if(bean != null && bean.getCommand() == FlowTaskBean.COMMAND_START){
    				bean.setCommand(FlowTaskBean.COMMAND_PAUSE);
    				myLog.info("开始暂停APP["+key+"]流量");
    				flowTaskList.add(bean);
    			}
    		}
    	}
    	
    	if(!flowTaskList.isEmpty()){
        	for(WorkNodeBean node:nodeList){
        		if(node.getName().contains("rtb")){
        			pushFlowTask(node.getName(), flowTaskList);
        		}
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

    private void pushFlowTask(String nodeName,ArrayList<FlowTaskBean> beanList){
    	MsgControlCenter.sendFlowTask(nodeName, beanList, Priority.NORM_PRIORITY);
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
