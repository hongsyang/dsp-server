package cn.shuzilm.backend.master;

import cn.shuzilm.backend.rtb.RtbFlowControl;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdFlowStatus;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.ReportBean;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/8/15.
 * 广告加权因子的计算
 */
public class AdPropertyHandler {
    private HashMap<String, AdBean> adMap = null;
    private HashMap<String, AdFlowStatus> mapMonitorHour = null;
    public HashMap<String, ReportBean> reportMapHour = null;

    private enum Property{
        IMP_PROCESS,
        MONEY_LEFT,
        CTR_SCORE

    }
    private static HashMap<String,AdPropertyBean> map = null;
    private AdFlowControl controlIns;

    public AdPropertyHandler(AdFlowControl controlIns){
        map = new HashMap<>();
        reportMapHour = controlIns.getReportMapHour();
        adMap = controlIns.getMapAd();
        mapMonitorHour = controlIns.getMapMonitorHour();
        handle();
    }


    public void handle(){

        for(String adUid : adMap.keySet()){
            AdBean ad = adMap.get(adUid);
            AdPropertyBean property = new AdPropertyBean();
            AdFlowStatus statusHour = mapMonitorHour.get(adUid);
            // 计算点击率 通过小时反馈的曝光和点击情况计算而来
            // 点击率 = 点击次数  / 曝光次数
            double clickRate = statusHour.getClickNums() * 1.0 / statusHour.getWinNums();
            property.setCtrScore(clickRate);

            //计算该广告消耗金额 根据 当前小时的耗费金额得来
            //曝光进度 = 当前实际耗费的金额 / 当前广告设定的限额 （如果当前广告设定限额为 0 ，则以该账户的每日限额为准， 如果每日限额为 0 ， 则以余额为准）
            ReportBean report = reportMapHour.get(adUid);
            property.setImpProcess(report.getCost().doubleValue() / ad.getQuotaAmount().doubleValue());

            //计算广告剩余金额因子 = 每小时的限额 - 当前小时耗费的金额
            property.setMoneyLeft(ad.getQuotaAmount().doubleValue() - report.getCost().doubleValue());

            map.put(adUid,property);
            //将计算好的因子打分写入到当前广告对象中
            ad.setPropertyBean(property);

        }


    }

    /**
     * 计算曝光进度因子
     * 已经花了多少钱，占总充值额度的占比
     * @return
     */
    public int getImpProcess(String adUid){
        return map.get(adUid).getImpProcess();
    }

    /**
     * 计算资金余额因子
     * @return
     */
    public int getMoneyLeft(String adUid){
        return map.get(adUid).getMoneyLeft();
    }

    /**
     * 计算点击率因子
     * @return
     */
    public int getCtrScore(String adUid){
        return map.get(adUid).getCtrScore();
    }


}
