package cn.shuzilm.backend.master;

import cn.shuzilm.bean.control.*;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.TimeSchedulingUtil;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

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
        }
        return control;
    }

    private static final Logger myLog = LoggerFactory.getLogger(AdFlowControl.class);

    public static ArrayList<WorkNodeBean> nodeList = null;
    /**
     * 主控定期从数据库中读取的间隔 单位：分钟
     */
    private static final int INTERVAL = 10 * 60 * 1000;
    private static TaskServicve taskService = new TaskServicve();

//    /**
//     * 广告主对应的广告 MAP
//     */
//    private static HashMap<String,ArrayList<String>> adviserMap = null;

    public HashMap<String, AdBean> getMapAd(){
        return mapAd;
    }

    public HashMap<String, AdFlowStatus> getMapMonitorHour(){
        return mapMonitorHour;
    }

    public HashMap<String, ReportBean> getReportMapHour(){
        return reportMapHour;
    }

    /**
     * 广告资源管理
     */
    private static HashMap<String, AdBean> mapAd = null;

    /**
     * 广告任务管理
     */
    private static HashMap<String, TaskBean> mapTask = null;

    private static HashMap<String, ReportBean> reportMapTotal = null;
    private static HashMap<String, ReportBean> reportMapHour = null;

    /**
     * 广告组与广告的对应关系
     */
    private static HashMap<String, GroupAdBean> mapAdGroup = null;

    /**
     * 广告永久的指标监控
     */
    private static HashMap<String, AdFlowStatus> mapMonitorTotal = null;
    /**
     * 广告每天的指标监控
     */
    private static HashMap<String, AdFlowStatus> mapMonitorDaily = null;
    /**
     * 广告每小时的指标监控
     */
    private static HashMap<String, AdFlowStatus> mapMonitorHour = null;

    /**
     * 广告组的监视器
     */
    private static HashMap<String, AdFlowStatus> mapMonitorAdGroupTotal = null;

    /**
     * 数据库中设定的设计流控指标（天 最高限）
     */
    private static HashMap<String, AdFlowStatus> mapThresholdDaily = null;

    /**
     * 数据库中设定的设计流控指标（小时 最高限）
     */
    private static HashMap<String, AdFlowStatus> mapThresholdHour = null;

    /**
     * 广告主设定的总流量和金额 (最高限)
     */
    private static HashMap<String, AdFlowStatus> adverConsumeMapCurr = null;

    public AdFlowControl() {
        MDC.put("sift", "control");

        mapAd = new HashMap<>();
        mapTask = new HashMap<>();

        mapMonitorDaily = new HashMap<>();
        mapMonitorHour = new HashMap<>();
        adverConsumeMapCurr = new HashMap<>();
        mapThresholdDaily = new HashMap<>();
        mapThresholdHour = new HashMap<>();
        mapAdGroup = new HashMap<>();
        mapMonitorTotal = new HashMap<>();
        reportMapHour = new HashMap<>();
        mapMonitorAdGroupTotal = new HashMap<>();
//        adviserMap = new HashMap<>();


    }

    public void trigger(int type) {
        // 5 s 触发
        pullAndUpdateTask();
        // 10 min 触发
        loadAdInterval(true);
        //每小时触发
        resetHourMonitor();
        //每天触发
        resetDayMonitor();

    }

    /**
     * 将 RTB 数量更新到天 和小时监视器中
     *
     * @param adUid
     * @param addBidNums
     */
    private void updateBids(String adUid, long addBidNums) {
        AdFlowStatus statusHour = mapMonitorHour.get(adUid);
        statusHour.setBidNums(statusHour.getBidNums() + addBidNums);
        AdFlowStatus statusDaily = mapMonitorDaily.get(adUid);
        statusDaily.setBidNums(statusDaily.getBidNums() + addBidNums);
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
     */
    private void updatePixel(String adUid, long addWinNoticeNums, float addMoney, int type , long clickNums, int pixelType) {
        //cpc 定价计算逻辑
        cpcHandler.updatePixel(adUid,1,addMoney,clickNums,1);

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
                String groupId = mapAd.get(adUid).getGroupId();
                AdFlowStatus statusGroupAll = mapMonitorAdGroupTotal.get(groupId);
                statusGroupAll.setMoney(statusGroupAll.getMoney() + addMoney);
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
                System.out.println(adUid);
                groupId = mapAd.get(adUid).getGroupId();
                statusGroupAll = mapMonitorAdGroupTotal.get(groupId);
                statusGroupAll.setMoney(statusGroupAll.getMoney() + addMoney);

                break;
        }
    }


    /**
     * 每隔 5 秒钟从消息中心获得所有节点的当前任务，并与当前两个 MAP monitor 进行更新
     *
     */
    public void pullAndUpdateTask() {
        //分发任务
        //1、根据当前各个节点消耗的情况，进行扣减，如：之前已经有该广告在投放了，后来调整了配额或金额，则从当前的额度中减掉已经消耗的部分（每小时和每天的），然后剩余的作为任务重新分发下去
        //更新当前广告主报价，资金池，流量池,广告打分

        //从各个 RTB 节点，获得最新的 bids 个数，并更新至内存监控
        for (WorkNodeBean node : nodeList) {
            //持续不断的从队列中获得 RTB 信息
            while(true){
                NodeStatusBean bean = MsgControlCenter.recvBidStatus(node.getName());
                if (bean == null)
                    break;
                ArrayList<AdBidBean> bidList = bean.getBidList();
                for (AdBidBean bid : bidList) {
                    updateBids(bid.getUid(), bid.getBidNums());
                }
            }

        }

        //从各个 PIXCEL 节点获得最新 wins 和 金额消费情况， 并更新至内存监控
        for (WorkNodeBean node : nodeList) {
            //持续不断的从队列中获得 pixcel 信息
            while(true){
                AdPixelBean pix = MsgControlCenter.recvPixelStatus(node.getName());
                if (pix == null)
                    break;
                updatePixel(pix.getAdUid(), pix.getWinNoticeNums(), Float.valueOf(pix.getFinalCost().toString()), -1,pix.getClickNums(),pix.getType());
            }
        }

        //拿当前的指标跟当前的阀值比较，如果超出阀值，则立刻停止任务，并下发任务停止命令
        for (String auid : mapThresholdHour.keySet()) {
            //监测 CPC 类型的广告是否可以投放
            boolean isOk = cpcHandler.checkAvailable(auid);
            if(!isOk){
                String reason = "### cpc 价格设置 过低，超过了成本线，停止广告投放 ###" + auid;
                stopAd(auid, reason, false);
            }
            AdFlowStatus threshold = mapThresholdHour.get(auid);
            AdFlowStatus monitor = mapMonitorHour.get(auid);
            //每小时曝光超过了设置的最大阀值，则终止该小时的广告投放
            if (threshold.getWinNums() != 0 && monitor.getWinNums() >= threshold.getWinNums()) {
                String reason = "#### 小时 CPM 超限，参考指标：" + threshold.getWinNums() + "\t" + monitor.getWinNums() + " ### " ;
                pauseAd(auid, reason, true);
                myLog.error(monitor.toString() + "\t" + reason);
            }
            if (threshold.getMoney() != 0 && monitor.getMoney() >= threshold.getMoney()) {
                //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                String reason = "#### 小时 金额 超限，参考指标：" + threshold.getMoney() + "\t" + monitor.getMoney() + " ###";
                pauseAd(auid, reason, true);
                myLog.error(monitor.toString() + "\t" + reason);

            }

            String groupId = mapAd.get(auid).getGroupId();
            AdFlowStatus monitorAdGroup = mapMonitorAdGroupTotal.get(groupId);
            double thresholdGroupMoney = mapAdGroup.get(groupId).getQuotaMoney().doubleValue();
            if(thresholdGroupMoney != 0 && monitorAdGroup.getMoney() >= thresholdGroupMoney){
                //广告组金额超限，则发送停止命令，终止该广告投放
                String reason = "#### 广告组 金额 超限，参考指标：" + thresholdGroupMoney + "\t" + monitorAdGroup.getMoney() + " ###";
                stopAd(auid, reason, false);
                myLog.error(auid + "\t" + reason);
            }

        }

        //账户的余额和每日的限额都在这里做适配，以最低的为准，
        //其中来自于三个地方： 2 个是 balance 表的 balance 字段 和 quota_amount 字段 ，
        // 还有一个地方来自于 广告组限额
        //跟每日监控作比对
        for (String auid : mapThresholdDaily.keySet()) {
            AdFlowStatus threshold = mapThresholdDaily.get(auid);
            AdFlowStatus monitor = mapMonitorDaily.get(auid);
            if (threshold.getMoney() != 0 && monitor.getMoney() >= threshold.getMoney()) {
                //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                String reason = "#### 每日金额 超限，参考指标：" + threshold.getMoney() + "\t" + monitor.getMoney() + " ###";
                stopAd(auid, reason, false);
                myLog.error(monitor.toString() + "\t" + reason);
            }
        }

        //根据返回的 winnotice 个数 和金额，重新调节和下发需要提供的 bids 的个数

    }

    /**
     * 小时计数器清零
     */
    public void resetHourMonitor() {
        //清理小时计数器
        for (String key : mapMonitorHour.keySet()) {
            AdFlowStatus status = mapMonitorHour.get(key);
            status.setBidNums(0);
            status.setMoney(0);
            status.setWinNums(0);
        }
    }


    /**
     * 每天初始化一次小时 和 天计数器
     */
    public void resetDayMonitor() {
        long time = 0;
        ResultList rl = null;
        try {
            rl = taskService.queryAdByUpTime(time);
            mapMonitorDaily.clear();
            mapMonitorHour.clear();
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
                status3.setMoney(money.floatValue());
                mapThresholdDaily.put(auid, status3);
                //重新加载 小时 参考指标
                AdFlowStatus status4 = new AdFlowStatus();
                status4.setUid(auid);
                status4.setName(name);
                status4.setWinNumsByThousand(winNumsHour);
                status4.setMoney(money.floatValue());
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
     * 每隔 10 分钟
     * 从数据库中加载所有的广告,广告主、广告素材和广告配额
     */
    public void loadAdInterval(boolean isInitial) {

        HashMap<String, ReportBean> reportMapDaily = null;


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
            HashSet<String> lowBalanceAdList = updateIndicator(adList);

            int counter = 0;


            for (ResultMap map : adList) {
                AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));
                //广告组
                String groupId = map.getString("group_uid");
                ad.setGroupId(groupId);
                String adUid = ad.getAdUid();

                if (isInitial) {
                    //初始化所有的监控
                    AdFlowStatus statusHour = new AdFlowStatus();
                    mapMonitorHour.put(adUid,statusHour);
                    AdFlowStatus statusDay = new AdFlowStatus();
                    mapMonitorDaily.put(adUid,statusDay);
                    AdFlowStatus statusAll = new AdFlowStatus();
                    mapMonitorTotal.put(adUid,statusAll);
                    AdFlowStatus statusAdGroup = new AdFlowStatus();
                    mapMonitorAdGroupTotal.put(groupId,statusAdGroup);

                    if (reportMapHour.size() > 0) {
                        ReportBean report = reportMapHour.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, 0, expense.floatValue(), 0,0,0);
                        }
                    }

                    if (reportMapDaily.size() > 0) {
                        ReportBean report = reportMapDaily.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, 0, expense.floatValue(), 1,0,0);
                        }
                    }

                    if (reportMapTotal.size() > 0) {
                        ReportBean report = reportMapTotal.get(adUid);
                        if (report != null) {
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(adUid, 0, expense.floatValue(), 2,0,0);
                        }
                    }
                }


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
                ad.setPrice(map.getBigDecimal("price").floatValue());
                ad.setMode(map.getString("mode"));
                // 设置广告的可拖欠的额度
                ad.setMoneyArrears(map.getInteger("money_arrears"));
                //出价模式
                ad.setMode(map.getString("mode"));

                ad.setPriority(map.getInteger("priority"));
                //限额
                // 如果当前广告设定限额为 0 ，则以该账户的每日限额为准，
                BigDecimal quotaAmount = map.getBigDecimal("quota_amount");
                if(quotaAmount.doubleValue() <= 0 ){
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
                mapTask.put(adUid, new TaskBean(adUid));
                counter++;

                if(lowBalanceAdList!= null && lowBalanceAdList.contains(adUid)){
                    stopAd(adUid,adUid + "\t广告余额不足，请联系广告主充值。。",false);
                    myLog.error(adUid + "\t广告余额不足，请联系广告主充值。。");
                    continue;
                }

            }

            //计算权重因子
            adProperty.handle();
            //定期 10 分钟更新 CPC 阀值
            cpcHandler.updateIndicator();

            myLog.info("主控： 开始分发任务，此次有 " + counter + " 个广告需要分发。。。 ");
//            for (int i = 0; i < 10000 ; i++) {
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
    	nodeList.clear();
    	WorkNodeBean testNode = new WorkNodeBean();
    	testNode.setId(20);
    	testNode.setIp("192.168.1.1");
    	testNode.setName("rtb-008");
    	testNode.setStatus(1);
    	testNode.setMemo(null);
    	nodeList.add(testNode);
        int nodeNums = nodeList.size();
        
        
        
        //遍历所有的广告
        ArrayList<AdBean> adList = new ArrayList<>();
        
        
        AdBean ad1 = new AdBean();
		AudienceBean au1 = new AudienceBean();
		String curl = "http://101.200.56.200:8880/" + "lingjiclick?" + "id=" + "1213" + "&price=" + 6 + "&pmp="
				+ "2222";
		au1.setAdUid("12345678");
		au1.setAdviserId("123456");
		au1.setName("大学生");
		au1.setRemark("remark");
		au1.setType("location");
		au1.setCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		 au1.setGeos(
			 "[{\"北京市通州区\":[\"116.640865\",\"39.852104\",\"1000\"]},{\"北京市大兴区公园北环路辅路-旧宫,清和园\":[\"116.461492\",\"39.794028\",\"5316\"]}]");
		au1.setMobilityType(0);
		//[{"北京市通州区":["116.640865","39.852104","1000"]},{"北京市大兴区公园北环路辅路-旧宫,清和园":["116.461492","39.794028","5316"]}]
		au1.setDemographicTagId("[111120,222220,333320,444420]");
		au1.setDemographicCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		au1.setIncomeLevel(2);
		au1.setAppPreferenceIds("eat food");
		au1.setPlatformId(1);
		au1.setBrandIds("335");
		au1.setPhonePriceLevel(3);
		au1.setNetworkId(2);
		au1.setCarrierId(4);
		au1.setCompanyIds("{\"北京AAA有限公司\":275231,\"北京BBB有限公司\":375331,\"北京CCC有限公司\":475431}");
		au1.setCompanyNames("北京AAA有限公司,北京BBB有限公司,北京CCC有限公司");

		List<AudienceBean> au1List = new ArrayList<AudienceBean>();
		au1List.add(au1);
		ad1.setAudienceList(au1List);

		AdPropertyBean propertyBean = new AdPropertyBean();
		propertyBean.setImpProcess(3);
		propertyBean.setCreativeQuality(2);
		propertyBean.setMoneyLeft(4);
		propertyBean.setAdvertiserScore(5);
		propertyBean.setCtrScore(2);

		ad1.setPropertyBean(propertyBean);

		AdvertiserBean advertiser = new AdvertiserBean();
		advertiser.setUid("7777777");
		advertiser.setName("广告主A");
		advertiser.setGrade(1);

		ad1.setAdvertiser(advertiser);

		ad1.setAdUid("23455555");
		ad1.setName("广告名称A");

		CreativeBean creative = new CreativeBean();
		creative.setUid("567890");
		creative.setName("广告素材A");
		creative.setApproved(1);

		Material material = new Material();
		material.setUid("1");
		material.setType("banner");
		material.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material.setWidth(340);
		material.setHeight(70);
		material.setApproved_adx("1");
		material.setExt("jpg");
		Material material01 = new Material();
		material01.setUid("01");
		material01.setType("banner");
		material01.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material01.setWidth(320);
		material01.setHeight(50);
		material01.setApproved_adx("1");
		material01.setExt("jpg");
		Material material02 = new Material();
		material02.setUid("02");
		material02.setType("banner");
		material02.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material02.setWidth(350);
		material02.setHeight(80);
		material02.setApproved_adx("1");
		material02.setExt("jpg");
		Material material03 = new Material();
		material03.setUid("03");
		material03.setType("banner");
		material03.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material03.setWidth(360);
		material03.setHeight(90);
		material03.setExt("jpg");
		material03.setApproved_adx("1");

		List<Material> materialList = new ArrayList<Material>();
		materialList.add(material);
		materialList.add(material01);
		materialList.add(material02);
		materialList.add(material03);
		creative.setType("banner");
		creative.setMaterialList(materialList);
		creative.setApproved_adx("1");
		List<CreativeBean> creativeList = new ArrayList<CreativeBean>();
		creativeList.add(creative);

		ad1.setCreativeList(creativeList);

		ad1.setPrice(100);
		
		creative.setLink(curl);
		creative.setTracking("https://www.shuzilm.cn/");
		creative.setLanding("https://www.shuzilm.cn/");

		AdBean ad2 = new AdBean();
		AudienceBean au2 = new AudienceBean();
		au2.setAdUid("12345678");
		au2.setAdviserId("123456");
		au2.setName("大学生");
		au2.setRemark("remark");
		au2.setType("location");
		au2.setCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		 au2.setGeos(
		 "[{\"北京市通州区\":[\"116.640865\",\"22.852104\",\"1000\"]},{\"北京市大兴区公园北环路辅路-旧宫,清和园\":[\"116.461492\",\"22.794028\",\"5316\"]}]");
		au2.setMobilityType(0);
		au2.setDemographicTagId("[111120,222220,333320,444420]");
		au2.setDemographicCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		au2.setIncomeLevel(2);
		au2.setAppPreferenceIds("eat food");
		au2.setPlatformId(1);
		au2.setBrandIds("335");
		au2.setPhonePriceLevel(3);
		au2.setNetworkId(2);
		au2.setCarrierId(4);
		au2.setCompanyIds("{\"北京AAA有限公司\":275231,\"北京BBB有限公司\":375331,\"北京CCC有限公司\":475431}");
		au2.setCompanyNames("北京AAA有限公司,北京BBB有限公司,北京CCC有限公司");

		List<AudienceBean> au2List = new ArrayList<AudienceBean>();
		au2List.add(au2);
		ad2.setAudienceList(au2List);

		AdPropertyBean propertyBean2 = new AdPropertyBean();
		propertyBean2.setImpProcess(4);
		propertyBean2.setCreativeQuality(3);
		propertyBean2.setMoneyLeft(4);
		propertyBean2.setAdvertiserScore(5);
		propertyBean2.setCtrScore(5);

		ad2.setPropertyBean(propertyBean2);

		AdvertiserBean advertiser2 = new AdvertiserBean();
		advertiser2.setUid("7777777");
		advertiser2.setName("广告主A");
		advertiser2.setGrade(1);

		ad2.setAdvertiser(advertiser2);

		ad2.setAdUid("23455556");
		ad2.setName("广告名称A");

		CreativeBean creative2 = new CreativeBean();
		creative2.setUid("567890");
		creative2.setName("广告素材A");
		Material material2 = new Material();
		material2.setUid("2");
		material2.setType("banner");
		material2.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material2.setWidth(820);
		material2.setHeight(630);
		material2.setApproved_adx("1");
		material2.setExt("jpg");
		Material material21 = new Material();
		material21.setUid("21");
		material21.setType("banner");
		material21.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material21.setWidth(390);
		material21.setHeight(100);
		material21.setExt("jpg");
		material21.setApproved_adx("1");
		Material material22 = new Material();
		material22.setUid("22");
		material22.setType("banner");
		material22.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material22.setWidth(360);
		material22.setHeight(70);
		material22.setApproved_adx("1");
		material22.setExt("jpg");
		Material material23 = new Material();
		material23.setUid("23");
		material23.setType("banner");
		material23.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material23.setWidth(320);
		material23.setHeight(50);
		material23.setApproved_adx("1");
		material23.setExt("jpg");

		List<Material> materialList2 = new ArrayList<Material>();
		materialList2.add(material2);
		materialList2.add(material21);
		materialList2.add(material22);
		materialList2.add(material23);
		creative2.setMaterialList(materialList2);
		creative2.setType("banner");
		creative2.setApproved_adx("1");
		creative2.setApproved(1);
		List<CreativeBean> creativeList2 = new ArrayList<CreativeBean>();
		creativeList2.add(creative2);

		ad2.setCreativeList(creativeList2);

		ad2.setPrice(100);
		
		creative2.setLink(curl);
		creative2.setTracking("https://www.shuzilm.cn/");
		creative2.setLanding("https://www.shuzilm.cn/");

		adList.add(ad1);
		adList.add(ad2);

		for (int i = 0; i < 10000; i++) {
			AdBean ad = new AdBean();
			BeanUtils.copyProperties(ad2, ad);
			AudienceBean auTemp = ad2.getAudienceList().get(0);
			AudienceBean au = new AudienceBean();
			BeanUtils.copyProperties(auTemp,au);
			List<AudienceBean> audienceList = new ArrayList<AudienceBean>();
			au.setGeos(
					 "[{\"北京市通州区\":[\"116.640"+i+"\",\"22.852"+i+"\",\"1000\"]},{\"北京市大兴区公园北环路辅路-旧宫,清和园\":[\"116.461"+i+"\",\"22.794"+i+"\",\"5316\"]}]");
			audienceList.add(au);
			ad.setAudienceList(audienceList);
			ad.setAdUid("aaa" + i);
			List<CreativeBean> list = ad.getCreativeList();
			List<CreativeBean> list1 = new ArrayList<CreativeBean>();
			CreativeBean c = list.get(0);
			CreativeBean c1 = new CreativeBean();
			BeanUtils.copyProperties(c, c1);
			List<Material> materialList1 = c.getMaterialList();
			List<Material> materialList3 = new ArrayList<Material>();
			int k = 0;
			for (int q = 0; q < materialList1.size(); q++) {
				Material m = new Material();
				m.setUid(Math.random() + "");
				m.setType("banner");
				m.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
				m.setApproved_adx("1");
				m.setWidth(320 + k);
				m.setHeight(50 + k);
				m.setExt("jpg");
				k = k + 10;
				materialList3.add(m);
			}
			c1.setMaterialList(materialList3);
			list1.add(c1);
			ad.setCreativeList(list1);
			// for(Material m:materialList1){
			// Material m1 = new Material();
			// BeanUtils.copyProperties(m1, m);
			// m.setUid(Math.random()+"");
			// m.setType("banner");
			// m.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
			//
			// material22.setWidth(800+k);
			// material22.setHeight(600+k);
			// k =k+10;
			// }
			adList.add(ad);
		}
        
//        for (String adUid : mapAd.keySet()) {
//            //对任务进行拆解
//            TaskBean task = new TaskBean(adUid);
//            AdBean ad = mapAd.get(adUid);
//
//            //从小时监控中取出曝光量、点击次数 、点击金额
//            AdFlowStatus statusHour = mapMonitorHour.get(adUid);
//            task.setClickNums(statusHour.getClickNums());
//            task.setExposureNums(statusHour.getWinNums());
//            task.setMoney(statusHour.getMoney());
//
//            //给每一个节点分配自己的 曝光 额度
//            task.setExposureLimitPerHour(ad.getCpmHourLimit() / nodeNums);
//            task.setExposureLimitPerDay(ad.getCpmDailyLimit() / nodeNums);
//            task.setCommand(TaskBean.COMMAND_START);
//            adList.add(ad);
//            for (WorkNodeBean node : nodeList) {
//                //发送广告状态
//                pushTaskSingleNode(node.getName(), task);
//            }
//        }

        for (WorkNodeBean node : nodeList) {
            //发送广告
            pushAdSingleNode(node.getName(), adList);
        }
    }

    /**
     * @param nodeName
     * @param bean
     */
    private void pushTaskSingleNode(String nodeName, TaskBean bean) {
        MsgControlCenter.sendTask(nodeName, bean, Priority.NORM_PRIORITY);
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
        for (WorkNodeBean node : nodeList) {
            TaskBean task = mapTask.get(adUid);
            task.setCommandMemo(reason);
            task.setCommand(TaskBean.COMMAND_PAUSE);
            task.setScope(isHourOrAll ? TaskBean.SCOPE_HOUR : TaskBean.SCOPE_ALL);
            pushTaskSingleNode(node.getName(), task);
        }

    }

    /**
     * @param adUid
     * @param reason
     * @param isHourOrAll 如果是小时，则只停止该小时的投放，如果是全部，则马上停止后续小时的所有的投放
     */
    public void stopAd(String adUid, String reason, boolean isHourOrAll) {
        for (WorkNodeBean node : nodeList) {
            TaskBean task = mapTask.get(adUid);
            task.setCommandMemo(reason);
            task.setCommand(TaskBean.COMMAND_STOP);
            task.setScope(isHourOrAll ? TaskBean.SCOPE_HOUR : TaskBean.SCOPE_ALL);
            pushTaskSingleNode(node.getName(), task);
        }

    }

}
