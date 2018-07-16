package cn.shuzilm.backend.master;

import cn.shuzilm.bean.*;
import cn.shuzilm.bean.control.*;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;

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

    public static ArrayList<WorkNodeBean> nodeList = null;
    /**
     * 主控定期从数据库中读取的间隔 单位：分钟
     */
    private static final int INTERVAL = 10 * 60 * 1000;
    private static TaskServicve taskService = new TaskServicve();

    /**
     * 广告每天的指标监控
     */
    private static HashMap<String,AdFlowStatus> mapDailyMonitor = null;
    /**
     * 广告每小时的指标监控
     */
    private static HashMap<String,AdFlowStatus> mapHourMonitor = null;
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
        mapDailyMonitor = new HashMap<>();
        mapHourMonitor  = new HashMap<>();
        adverConsumeMapCurr = new HashMap<>();
        mapThresholdDaily = new HashMap<>();
        mapThresholdHour = new HashMap<>();
    }

    /**
     * 每隔 5 秒钟从消息中心获得所有节点的当前任务，并与当前两个 MAP monitor 进行更新
     */
    public void pullAndUpdateTask(){
        //从各个 RTB 节点，获得最新的 BIDS 个数，并更新至内存监控
        for(WorkNodeBean node : nodeList){
            NodeStatusBean bean = MsgControlCenter.recvBidStatus(node.getName());
            ArrayList<AdBidBean> bidList = bean.getBidList();
            for(AdBidBean bid : bidList){

            }
        }

        //从各个 PIXCEL 节点获得最新 wins 和 金额消费情况， 并更新至内存监控
        for(WorkNodeBean node : nodeList){
            NodeStatusBean bean = MsgControlCenter.recvBidStatus(node.getName());
            ArrayList<AdPixelBean> pixelList = bean.getPixelList();


        }
        //根据返回的 winnotice 个数 和金额，重新调节需要提供的 BIDS 的个数

    }

    /**
     * 小时计数器清零
     */
    public void resetHourMonitor(){
        //清理小时计数器
        for(String key :mapHourMonitor.keySet()){
            AdFlowStatus status = mapHourMonitor.get(key);
            status.setBidNums(0);
            status.setAmount(0);
            status.setMoney(0);
            status.setWinNums(0);
        }
    }


    /**
     * 每天初始化一次小时 和 天计数器
     */
    public void resetMonitor(){
        long time = 0;
        ResultList rl = null;
        try {
            rl = taskService.queryAdByUpTime(time);
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
                mapDailyMonitor.put(auid,status);
                //初始化小时监视器
                AdFlowStatus status2 = new AdFlowStatus();
                status2.reset();
                status2.setUid(auid);
                status2.setName(name);
                mapHourMonitor.put(auid,status2);


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 每隔 10 分钟初始化一次天和小时的监视器
     */
    public void updateIndicator(){
        long time = 0;
        ResultList rl = null;
        try {
            rl = taskService.queryAdByUpTime(time);

            for(ResultMap map : rl){
                String auid = map.getString("uid");
                String name = map.getString("name");
                String adviserId = map.getString("advertiser_uid");
                ResultMap balanceMap = taskService.queryAdviserAccountById(adviserId);
                BigDecimal balance = balanceMap.getBigDecimal("balance");

                int winNumsHour = map.getInteger("cpm_hourly");
                int winNumsDaily = map.getInteger("cpm_daily");
                BigDecimal money = map.getBigDecimal("quota_amount");
                //如果这个账户的余额比每天或小时的限额还小，则赋予小的值
                if(balance.floatValue() <= money.floatValue() ){
                    money = balance;
                }

                //重新加载 天 参考指标
                AdFlowStatus status3 = new AdFlowStatus();
                status3.reset();
                status3.setUid(auid);
                status3.setName(name);
                status3.setAmount(winNumsDaily);
                status3.setMoney(money.floatValue());
                mapThresholdDaily.put(auid,status3);
                //重新加载 小时 参考指标
                AdFlowStatus status4 = new AdFlowStatus();
                status4.setUid(auid);
                status4.setName(name);
                status4.setAmount(winNumsHour);
                status4.setMoney(money.floatValue());
                mapThresholdHour.put(auid,status4);


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 统计每一个广告主的消费情况
     */
    public void statConsumeByAdver(){

    }


    /**
     * 每隔 10 分钟
     * 从数据库中加载所有的广告,广告主、广告素材和广告配额
     */
    public void loadFromDb(boolean isInitial){
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
            ResultList rl = taskService.queryAdByUpTime(timeBefore);
            for(ResultMap map : rl){
                AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));
                String adverUid = map.getString("advertiser_uid");

                //根据 广告主ID 获得 广告主
                AdvertiserBean adver = taskService.queryAdverByUid(adverUid);
                ad.setAdvertiser(adver);
                ad.setName(map.getString("name"));
                //每天限制
                ad.setCpmDailyLimit(map.getInteger("cpm_daily"));
                //每小时限制
                ad.setCpmHourLimit(map.getInteger("cpm_hourly"));

                String creativeUid = map.getString("creative_uid");
                //根据 广告创意ID 获得广告创意
                CreativeBean creativeBean = taskService.queryCreativeUidByAid(creativeUid);
                ArrayList<CreativeBean> creaList = new ArrayList<>();
                creaList.add(creativeBean);
                ad.setCreativeList(creaList);
                ad.setEndTime(new Date(map.getInteger("e")));
                ad.setFrqDaily(map.getInteger("frq_daily"));
                ad.setFrqHour(map.getInteger("frq_hourly"));
                ad.setPrice(map.getFloat("price"));
                ad.setPriority(map.getInteger("priority"));
                //限额
                ad.setQuotaAmount(map.getBigDecimal("quota_amount"));
                ad.setSpeedMode(map.getInteger("speed"));
                ad.setStartTime(new Date(map.getInteger("s")));
                String timeScheTxt = map.getString("time");
                int[][] timeScheduling = timeTxtToMatrix(timeScheTxt);
                ad.setTimeSchedulingArr(timeScheduling);
                ad.setTimestamp(map.getLong("created_at"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //如果是价格发生了变化，则不需要重新分配任务，直接通知价格就可以

        //如果素材发生了变化，则需要重新分配任务


    }

    private int[][] timeTxtToMatrix(String text){
        return null;
    }



    /**
     * 每隔  10 分钟，从数据库中获得最新变更的广告信息，并进行任务拆解， 同时 通过消息中心将任务下发到
     * 每一个节点中
     * 其中包括各种对广告的控制，包括开启广告，暂停广告，终止广告等
     */
    public void pushTask(){
        //分发任务
        //1、根据当前各个节点消耗的情况，进行扣减，如：之前已经有该广告在投放了，后来调整了配额或金额，则从当前的额度中减掉已经消耗的部分（每小时和每天的），然后剩余的作为任务重新分发下去

        //更新当前广告主报价，资金池，流量池,广告打分

    }

    /**
     * 达到限度，暂停广告投放
     * @param adUid
     */
    public void pauseAd(String adUid){

    }

    /**
     * 达到限度，停止广告投放，并回收资源
     * @param adUid
     */
    public void stopAd(String adUid){

    }

}
