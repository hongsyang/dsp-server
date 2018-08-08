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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 广告流量控制
 * Created by thunders on 2018/7/11.
 *
 */
public class AdFlowControl {
    private static AdFlowControl control;
    public static AdFlowControl getInstance(){
        if(control == null){
            control = new AdFlowControl();
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

    /**
     * 广告资源管理
     */
    private static HashMap<String,AdBean> mapAd = null;

    /**
     * 广告任务管理
     */
    private static HashMap<String,TaskBean> mapTask = null;

    /**
     * 广告组与广告的对应关系
     */
    private static HashMap<String,GroupAdBean> mapAdGroup = null;

    /**
     * 广告永久的指标监控
     */
    private static HashMap<String,AdFlowStatus> mapMonitorTotal = null;
    /**
     * 广告每天的指标监控
     */
    private static HashMap<String,AdFlowStatus> mapMonitorDaily = null;
    /**
     * 广告每小时的指标监控
     */
    private static HashMap<String,AdFlowStatus> mapMonitorHour = null;
    /**
     * 数据库中设定的设计流控指标（天 最高限）
     */
    private static HashMap<String,AdFlowStatus> mapThresholdDaily = null;

    /**
     * 数据库中设定的设计流控指标（小时 最高限）
     */
    private static HashMap<String,AdFlowStatus> mapThresholdHour = null;

    /**
     * 广告主设定的总流量和金额 (最高限)
     */
    private static HashMap<String,AdFlowStatus> adverConsumeMapCurr = null;

    public AdFlowControl(){
        MDC.put("sift","control");
        mapAd = new HashMap<>();
        mapTask = new HashMap<>();

        mapMonitorDaily = new HashMap<>();
        mapMonitorHour = new HashMap<>();
        adverConsumeMapCurr = new HashMap<>();
        mapThresholdDaily = new HashMap<>();
        mapThresholdHour = new HashMap<>();
        mapAdGroup = new HashMap<>();
        mapMonitorTotal = new HashMap<>();
//        adviserMap = new HashMap<>();
    }

    public void trigger(int type){
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
     * @param adUid
     * @param addBidNums
     */
    private void updateBids(String adUid,long addBidNums){
        AdFlowStatus statusHour =  mapMonitorHour.get(adUid);
        statusHour.setBidNums(statusHour.getBidNums() + addBidNums);
        AdFlowStatus statusDaily = mapMonitorDaily.get(adUid);
        statusDaily.setBidNums(statusDaily.getBidNums() + addBidNums);
    }

    /**
     * 将 曝光和产生的费用更新到 天和小时监视器中
     * @param adUid
     * @param addWinNoticeNums
     * @param addMoney
     * @param type 0 hour  1 daily  2 total -1 全部都更新
     */
    private void updatePixel(String adUid,long addWinNoticeNums,float addMoney,int type){
        switch(type){
            case 0 :
                AdFlowStatus statusHour = mapMonitorHour.get(adUid);
                if(statusHour == null)
                    break;
                statusHour.setWinNums(statusHour.getWinNums() + addWinNoticeNums);
                statusHour.setMoney(statusHour.getMoney() + addMoney);
                break;
            case 1:
                AdFlowStatus statusDaily = mapMonitorDaily.get(adUid);
                if(statusDaily == null)
                    break;
                statusDaily.setWinNums(statusDaily.getWinNums() + addWinNoticeNums);
                statusDaily.setMoney(statusDaily.getMoney() + addMoney);
                break;
            case 2:
                AdFlowStatus statusAll = mapMonitorTotal.get(adUid);
                if(statusAll == null)
                    break;
                statusAll.setWinNums(statusAll.getWinNums() + addWinNoticeNums);
                statusAll.setMoney(statusAll.getMoney() + addMoney);
                break;
            case -1:
                statusHour = mapMonitorHour.get(adUid);
                if(statusHour != null){
                    statusHour.setWinNums(statusHour.getWinNums() + addWinNoticeNums);
                    statusHour.setMoney(statusHour.getMoney() + addMoney);
                }

                statusDaily = mapMonitorDaily.get(adUid);
                if(statusDaily != null){
                    statusDaily.setWinNums(statusDaily.getWinNums() + addWinNoticeNums);
                    statusDaily.setMoney(statusDaily.getMoney() + addMoney);
                }

                statusAll = mapMonitorTotal.get(adUid);
                if(statusAll != null){
                    statusAll.setWinNums(statusAll.getWinNums() + addWinNoticeNums);
                    statusAll.setMoney(statusAll.getMoney() + addMoney);
                }

                break;
        }
    }



    /**
     * 每隔 5 秒钟从消息中心获得所有节点的当前任务，并与当前两个 MAP monitor 进行更新
     *
     */
    public void pullAndUpdateTask(){
        //分发任务
        //1、根据当前各个节点消耗的情况，进行扣减，如：之前已经有该广告在投放了，后来调整了配额或金额，则从当前的额度中减掉已经消耗的部分（每小时和每天的），然后剩余的作为任务重新分发下去
        //更新当前广告主报价，资金池，流量池,广告打分

        //从各个 RTB 节点，获得最新的 bids 个数，并更新至内存监控
        for(WorkNodeBean node : nodeList){
            NodeStatusBean bean = MsgControlCenter.recvBidStatus(node.getName());
            if(bean == null)
                continue;
            ArrayList<AdBidBean> bidList = bean.getBidList();
            for(AdBidBean bid : bidList){
                updateBids(bid.getUid(),bid.getBidNums());
            }
        }

        //从各个 PIXCEL 节点获得最新 wins 和 金额消费情况， 并更新至内存监控
        for(WorkNodeBean node : nodeList){
            NodeStatusBean bean = MsgControlCenter.recvPixelStatus(node.getName());
            if(bean == null)
                continue;
            ArrayList<AdPixelBean> pixelList = bean.getPixelList();
            for(AdPixelBean pix : pixelList){
                //更新全部监视器
                updatePixel(pix.getAdUid(),pix.getWinNoticeNums(),pix.getMoney(),-1);
            }

        }

        //拿当前的指标跟当前的阀值比较，如果超出阀值，则立刻停止任务，并下发任务停止命令
        for(String auid : mapThresholdHour.keySet()){
            AdFlowStatus threshold = mapThresholdHour.get(auid);
            AdFlowStatus monitor = mapMonitorHour.get(auid);
            //每小时曝光超过了设置的最大阀值，则终止该小时的广告投放
            if(threshold.getWinNums() != 0 && monitor.getWinNums() >= threshold.getWinNums()){
                String reason = "#### CPM 超限，参考指标：" +threshold.getWinNums()+ " ###";
                pauseAd(auid,reason,true);
                myLog.info(monitor.toString() + "\t" + reason );
            }
            if(threshold.getMoney() != 0 && monitor.getMoney() >= threshold.getMoney()){
                //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                String reason = "#### 金额 超限，参考指标：" +threshold.getMoney()+ " ###" ;
                pauseAd(auid,reason,true);
                myLog.info(monitor.toString() + "\t" +  reason);

            }
        }

        //账户的余额和每日的限额都在这里做适配，以最低的为准，
        //其中来自于三个地方： 2 个是 balance 表的 balance 字段 和 quota_amount 字段 ，
        // 还有一个地方来自于 广告组限额
        //跟每日监控作比对
        for(String auid : mapThresholdDaily.keySet()){
            AdFlowStatus threshold = mapThresholdDaily.get(auid);
            AdFlowStatus monitor = mapMonitorDaily.get(auid);
            if(threshold.getMoney() != 0 && monitor.getMoney() >= threshold.getMoney()){
                //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
                String reason = "#### 每日金额 超限，参考指标：" +threshold.getMoney()+ " ###";
                stopAd(auid,reason,false);
                myLog.info(monitor.toString() + "\t" + reason );
            }
        }

        //根据返回的 winnotice 个数 和金额，重新调节和下发需要提供的 bids 的个数

    }

    /**
     * 小时计数器清零
     */
    public void resetHourMonitor(){
        //清理小时计数器
        for(String key : mapMonitorHour.keySet()){
            AdFlowStatus status = mapMonitorHour.get(key);
            status.setBidNums(0);
            status.setMoney(0);
            status.setWinNums(0);
        }
    }


    /**
     * 每天初始化一次小时 和 天计数器
     */
    public void resetDayMonitor(){
        long time = 0;
        ResultList rl = null;
        try {
            rl = taskService.queryAdByUpTime(time);
            mapMonitorDaily.clear();
            mapMonitorHour.clear();
            for(ResultMap map : rl){
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
                mapMonitorDaily.put(auid,status);
                //初始化小时监视器
                AdFlowStatus status2 = new AdFlowStatus();
                status2.reset();
                status2.setUid(auid);
                status2.setName(name);
                mapMonitorHour.put(auid,status2);


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 每隔 10 分钟更新一次天和小时的阀值
     */
    private void updateIndicator(ResultList rl){
        try {
            for(ResultMap map : rl){
                String auid = map.getString("uid");
                String name = map.getString("name");
                String adviserId = map.getString("advertiser_uid");
                ResultMap balanceMap = taskService.queryAdviserAccountById(adviserId);
                BigDecimal balance = balanceMap.getBigDecimal("balance");
                BigDecimal quotaMoneyPerDay = balanceMap.getBigDecimal("quota_amount");

                int winNumsHour = map.getInteger("cpm_hourly");
                int winNumsDaily = map.getInteger("cpm_daily");
                BigDecimal money = map.getBigDecimal("quota_amount");

                if(quotaMoneyPerDay.floatValue()!= 0 && quotaMoneyPerDay.floatValue()<= money.floatValue()){
                    money = quotaMoneyPerDay;
                }
                //如果这个账户的余额比每天或小时的限额还小，则赋予小的值
                if(balance.floatValue()!=0 && balance.floatValue() <= money.floatValue() ){
                    money = balance;
                }



                //重新加载 天 参考指标
                AdFlowStatus status3 = new AdFlowStatus();
                status3.reset();
                status3.setUid(auid);
                status3.setName(name);
                status3.setWinNumsByThousand(winNumsDaily);
                status3.setMoney(money.floatValue());
                mapThresholdDaily.put(auid,status3);
                //重新加载 小时 参考指标
                AdFlowStatus status4 = new AdFlowStatus();
                status4.setUid(auid);
                status4.setName(name);
                status4.setWinNumsByThousand(winNumsHour);
                status4.setMoney(money.floatValue());
                mapThresholdHour.put(auid,status4);

            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public void loadAdInterval(boolean isInitial){
        long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        //取出所有的广告，并取出变动的部分，如果是配额和金额发生变化，则需要重新分配任务
        try {
            //加载 主机节点信息
            nodeList = taskService.getWorkNodeAll();

            //加载最新广告信息
            if(isInitial){
                timeBefore = 0;
            }
            //加载广告组信息
            ArrayList<GroupAdBean> groupList = taskService.queryAdGroupAll(timeBefore);
            if(groupList != null){
                for(GroupAdBean group : groupList){
                    mapAdGroup.put(group.getGroupId(),group);
                }
            }
            //加载广告信息
            ResultList rl = taskService.queryAdByUpTime(timeBefore);
            //更新监视器阀值信息
            updateIndicator(rl);
            int counter = 0;
            for(ResultMap map : rl){
                AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));

                if(isInitial){
                    //加载 小时 历史消费金额
                    HashMap<String,ReportBean> reportMapHour = taskService.statAdCostHour();
                    if(reportMapHour.size() >0){
                        ReportBean report = reportMapHour.get(ad.getAdUid());
                        if(report != null){
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(ad.getAdUid(),0,expense.floatValue(),0);
                        }

                    }

                    //加载 天 历史消费金额
                    HashMap<String,ReportBean> reportMapDaily = taskService.statAdCostDaily();
                    if(reportMapDaily.size() > 0){
                        ReportBean report = reportMapDaily.get(ad.getAdUid());
                        if(report != null){
                            BigDecimal expense = report.getExpense();
                            this.updatePixel(ad.getAdUid(),0,expense.floatValue(),1);
                        }
                    }

                    //加载 总 历史消费金额
                    HashMap<String,ReportBean> reportMapTotal = taskService.statAdCostTotal();
                    if(reportMapTotal.size() >0){
                        ReportBean report =  reportMapTotal.get(ad.getAdUid());
                        if(report != null){
                            BigDecimal expense =report.getExpense();
                            this.updatePixel(ad.getAdUid(),0,expense.floatValue(),2);
                        }

                    }


                }

                //广告组
                String groupId = map.getString("group_uid");
                ad.setGroupId(groupId);
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
                AudienceBean audience = taskService.queryAudienceByUpTime(ad.getAdUid());
                ad.setAudience(audience);

                String creativeUid = map.getString("creative_uid");
                //根据 广告创意ID 获得广告创意
                CreativeBean creativeBean = taskService.queryCreativeUidByAid(creativeUid);
                ArrayList<CreativeBean> creaList = new ArrayList<>();
                creaList.add(creativeBean);
                ad.setCreativeList(creaList);
                ad.setEndTime(new Date(map.getInteger("e")));
                ad.setFrqDaily(map.getInteger("frq_daily"));
                ad.setFrqHour(map.getInteger("frq_hourly"));
                ad.setPrice(map.getBigDecimal("price").floatValue());
                ad.setPriority(map.getInteger("priority"));
                //限额
                ad.setQuotaAmount(map.getBigDecimal("quota_amount"));
                ad.setSpeedMode(map.getInteger("speed"));
                ad.setStartTime(new Date(map.getInteger("s")));
                String timeScheTxt = map.getString("time");
                int[][] timeScheduling = TimeSchedulingUtil.timeTxtToMatrix(timeScheTxt);
                ad.setTimeSchedulingArr(timeScheduling);
                ad.setTimestamp(map.getInteger("created_at"));


                //如果是价格和配额发生了变化，直接通知
                //如果素材发生了变化，直接通知
                mapAd.put(ad.getAdUid(),ad);
                mapTask.put(ad.getAdUid(),new TaskBean(ad.getAdUid()));
                counter ++;

            }

            myLog.info("主控： 开始分发任务，此次有 " + counter + " 个广告需要分发。。。 ");
            dispatchTask();
            myLog.info("主控： 开始分发任务，此次有 " + counter + " 分发完毕。。。");
            myLog.info("主控： 共有 " + mapAd.keySet().size() +" 个广告在运行");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 每隔 10 分钟，从数据库中获得所有的广告信息，并进行任务拆解， 同时 通过消息中心将任务下发到每一个节点中
     * 其中包括各种对广告的控制，包括开启广告，暂停广告，终止广告等
     */
    private void dispatchTask(){
        int nodeNums = nodeList.size();
        //遍历所有的广告
        ArrayList<AdBean> adList = new ArrayList<>();
        for(String adUid : mapAd.keySet()){
            //对任务进行拆解
            TaskBean task = new TaskBean(adUid);
            AdBean ad = mapAd.get(adUid);
            //给每一个节点分配自己的 曝光 额度
            task.setExposureLimitPerHour(ad.getCpmHourLimit() / nodeNums);
            task.setExposureLimitPerDay(ad.getCpmDailyLimit() / nodeNums);
            task.setCommand(TaskBean.COMMAND_START);
            adList.add(ad);
            for(WorkNodeBean node : nodeList){
                //发送广告状态
                pushTaskSingleNode(node.getName(),task);
            }
        }

        for(WorkNodeBean node : nodeList){
            //发送广告
            pushAdSingleNode(node.getName(),adList);
        }
    }

    /**
     *
     * @param nodeName
     * @param bean
     */
    private void pushTaskSingleNode(String nodeName,TaskBean bean){
        MsgControlCenter.sendTask(nodeName,bean, Priority.NORM_PRIORITY);
    }

    /**
     *
     * @param nodeName
     * @param beanList
     */
    private void pushAdSingleNode(String nodeName,ArrayList<AdBean> beanList){
        MsgControlCenter.sendAdBean(nodeName,beanList, Priority.NORM_PRIORITY);
    }


    /**
     * @param adUid
     * @param reason
     * @param isHourOrAll 如果是小时，则只停止该小时的投放，如果是全部，则马上停止后续小时的所有的投放
     */
    public void pauseAd(String adUid,String reason,boolean isHourOrAll){
        for(WorkNodeBean node :nodeList){
            TaskBean task = mapTask.get(adUid);
            task.setCommandMemo(reason);
            task.setCommand(TaskBean.COMMAND_PAUSE);
            task.setScope(isHourOrAll?TaskBean.SCOPE_HOUR:TaskBean.SCOPE_ALL);
            pushTaskSingleNode(node.getName(),task);
        }

    }

    /**
     *
     * @param adUid
     * @param reason
     * @param isHourOrAll 如果是小时，则只停止该小时的投放，如果是全部，则马上停止后续小时的所有的投放
     */
    public void stopAd(String adUid,String reason,boolean isHourOrAll){
        for(WorkNodeBean node :nodeList){
            TaskBean task = mapTask.get(adUid);
            task.setCommandMemo(reason);
            task.setCommand(TaskBean.COMMAND_STOP);
            task.setScope(isHourOrAll?TaskBean.SCOPE_HOUR:TaskBean.SCOPE_ALL);
            pushTaskSingleNode(node.getName(),task);
        }

    }

}
