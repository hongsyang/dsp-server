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
    public HashMap<String, ReportBean> reportMapTotal = null;

    private enum Property{
        IMP_PROCESS,
        MONEY_LEFT,
        CTR_SCORE

    }
    private static HashMap<String,AdPropertyBean> map = null;
    private AdFlowControl controlIns;

    public AdPropertyHandler(){
        map = new HashMap<>();
        controlIns = AdFlowControl.getInstance();
        reportMapTotal = controlIns.getReportMapTotal();
        adMap = controlIns.getMapAd();
        mapMonitorHour = controlIns.getMapMonitorHour();
        handle();
    }


    public void handle(){

        for(String adUid : adMap.keySet()){
            AdBean ad = adMap.get(adUid);
            AdPropertyBean property = new AdPropertyBean();
            AdFlowStatus statusHour = mapMonitorHour.get(adUid);
            double clickRate = statusHour.getClickNums() * 1.0 / statusHour.getWinNums();
            //计算点击率
            property.setCtrScore(clickRate);

            //计算该广告消耗金额
            ReportBean report = reportMapTotal.get(adUid);
            property.setImpProcess(report.getCost().doubleValue() / ad.getQuotaAmount().doubleValue());
            //计算广告剩余金额因子
            property.setMoneyLeft();

            map.put(adUid,property);
        }


    }

    /**
     * 计算曝光进度因子
     * 已经花了多少钱，占总充值额度的占比
     * @return
     */
    private int getImpProcess(String adUid){
        return map.get(adUid).getImpProcess();
    }

    /**
     * 计算资金余额因子
     * @return
     */
    private int getMoneyLeft(String adUid){
        return map.get(adUid).getMoneyLeft();
    }

    /**
     * 计算点击率因子
     * @return
     */
    private int getCtrScore(String adUid){
        return map.get(adUid).getCtrScore();
    }


}
