package cn.shuzilm.backend.master;

import cn.shuzilm.bean.AdBean;

import java.util.HashMap;

/**
 * 广告流量控制
 * Created by thunders on 2018/7/11.
 *
 */
public class AdFlowControl {

    private static HashMap<String,AdBean> mapDailyMonitor = null;
    private static HashMap<String,AdBean> mapHourMonitor = null;

    public AdFlowControl(){
        mapDailyMonitor = new HashMap<>();
        mapHourMonitor  = new HashMap<>();
    }

    /**
     * 从数据库中加载所有的广告,广告主、广告素材和广告配额
     */
    public void loadFromDb(){
        //取出所有的广告，并取出变动的部分，如果是配额和金额发生变化，则需要重新分配任务

        //如果是价格发生了变化，则不需要重新分配任务，直接通知价格就可以

        //如果素材发生了变化，则需要重新分配任务


    }

    /**
     * 每隔 5 秒钟从消息中心获得所有节点的当前任务，并与当前两个 MAP 进行更新
     */
    public void pullAndUpdateTask(){
        //从各个 RTB 节点，获得最新的 BIDS 个数，并更新至内存监控

        //从各个 PIXCEL 节点获得最新 wins 和 金额消费情况， 并更新至内存监控

        //根据返回的 winnotice 个数 和金额，重新调节需要提供的 BIDS 的个数

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

    public void pauseAd(String adUid){

    }

    public void stopAd(String adUid){

    }

}
