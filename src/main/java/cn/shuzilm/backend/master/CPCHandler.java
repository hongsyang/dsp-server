package cn.shuzilm.backend.master;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdFlowStatus;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by DENGJIAN on 2018/8/22.
 */
public class CPCHandler {

    private static final Logger myLog = LoggerFactory.getLogger(AdFlowControl.class);
    /**
     * 广告永久的指标监控
     */
    private static HashMap<String, AdFlowStatus> mapMonitorTotal = new HashMap<>();
    /**
     * 数据库中设定的设计流控指标
     */
    private static HashMap<String, AdFlowStatus> mapThresholdTotal = new HashMap<>();

    /**
     * cpc试投放数量
     */
    private static int winTotalNums = 5 * 1000;

    // 广告信息
    private HashMap<String, AdBean> adMap = null;

    private static CPCHandler cpcHandler;

    public static CPCHandler getInstance(){
        if( cpcHandler == null){
            AdFlowControl adFlowControl = AdFlowControl.getInstance();
            cpcHandler = new CPCHandler(adFlowControl);
        }
        return cpcHandler;
    }

    public CPCHandler(AdFlowControl controlIns){
        adMap = controlIns.getMapAd();
    }

    /**
     * 更新按CPC结算的广告的监控器
     * @param adUid 广告id
     * @param addWinNoticeNums  增加的winNotice数量
     * @param addMoney 增加的曝光花费金额
     * @param clickNums 点击数量
     * @param pixelType  pixcel 类型，曝光 0 和 点击 1
     * @return
     */
    public boolean updatePixel(String adUid, long addWinNoticeNums, float addMoney, long clickNums , int pixelType) {
        AdFlowStatus status = mapMonitorTotal.get(adUid);
        // 如果为空新增广告监视器
        if (status == null){
            status = new AdFlowStatus();
            status.setUid(adUid);
            mapMonitorTotal.put(adUid, status);
        }
        if(pixelType == 0){
            status.setWinNums(status.getWinNums() + addWinNoticeNums);
            status.setMoney(status.getMoney() + addMoney);
        }else if(pixelType == 1){
            status.setClickNums(status.getClickNums() +  clickNums);
        }
        return true;
    }

    /**
     * 更新阈值
     * @return
     */
    public boolean updateIndicator(boolean isInitial){
        AdBean adBean = null;
        for (String adUid : adMap.keySet()) {
            try{
                adBean = adMap.get(adUid);
                if(adBean == null){
                    myLog.error("未找到广告信息："+adUid);
                    return false;
                }
                AdFlowStatus status = new AdFlowStatus();
                status.reset();
                status.setUid(adUid);
                status.setName(adBean.getName());
                status.setWinNums(winTotalNums);
                status.setMoney(adBean.getMoneyArrears());
                mapThresholdTotal.put(adUid, status);

                if(isInitial) {
                    AdFlowStatus moniterStatus = new AdFlowStatus();
                    mapMonitorTotal.put(adUid,moniterStatus);
                }
            }catch (Exception e){
                myLog.error("更新阈值失败，广告id: " + adUid, e);
                return false;
            }
        }
        return true;
    }

    /**
     * 判断广告是否可以发放
     * @param auid  广告id
     * @return  false : 暂停广告发放  true: 继续广告发放
     */
    public String checkAvailable(String auid) {
    	String reason = null;
        try{        	
            AdFlowStatus statusMonitor = mapMonitorTotal.get(auid);
            AdFlowStatus statusThreshold = mapThresholdTotal.get(auid);
            if(statusMonitor == null || statusThreshold == null) {
               // myLog.error("监视器或者阈值为空，广告id： " + auid);
            	reason = "监视器或者阈值为空，广告id： " + auid;
                return reason;
            }

            AdBean adBean = adMap.get(auid);
            if(adBean == null){
                //myLog.error("未找到广告信息："+auid);
                reason = "未找到广告信息："+auid;
                return reason;
            }
            // 点击量
            long clickNum = statusMonitor.getClickNums();
            // 曝光成功数量
            long winNum = statusMonitor.getWinNums();
            // 曝光花费
            float money = statusMonitor.getMoney();
            // 单次cpc报价
            float price = adBean.getPrice();
            // 广告的可拖欠额度
            int moneyArrears = adBean.getMoneyArrears();
//            myLog.info("CPC结算广告流量控制：点击量：{}，曝光数量：{}，曝光花费：{}，报价：{}，额度： {}",
//                    clickNum, winNum, money, price, moneyArrears);

            if(clickNum > 0) {
                // 有点击
                // 每个点击平均产生的费用
                float clickPrice = money / clickNum;
                // 大于广告主单个CPC报价，则暂停广告发放
                if(clickPrice >= price) {
                   // myLog.debug("出现点击 每个点击平均产生的费用  大于  广告主单个CPC报价，则暂停广告发放");
                	reason = "出现点击 每个点击平均产生的费用  大于  广告主单个CPC报价，则暂停广告发放   "+auid;
                    return reason;
                }else {
                    //myLog.debug("出现点击 每个点击平均产生的费用  小于  广告主单个CPC报价，则继续广告发放");
                	reason = "出现点击 每个点击平均产生的费用  小于  广告主单个CPC报价，则继续广告发放   "+auid;
                    return null;
                }
            }else {
                // 没有点击
                // 如果投放完毕
                if(winNum >= winTotalNums) {
                    //myLog.debug("没有点击，投放完毕，暂停发放");
                	reason = "没有点击，投放完毕，暂停发放   "+auid;
                    return reason;
                }else {
                    // 超额，暂停广告发放
                    if(money >= moneyArrears) {
                        //myLog.debug("没有点击，未投放完毕，超额，暂停广告投放");
                    	reason = "没有点击，未投放完毕，超额，暂停广告投放   "+auid;
                        return reason;
                    }else {
                        //myLog.debug("没有点击，未投放完毕，未超额，继续广告投放");
                        return null;
                    }
                }
            }
        }catch (Exception e){
            myLog.error("判断广告是否可以发放出错，广告id：" + auid, e);
            reason = "判断广告是否可以发放出错，广告id：" + auid+e;
            return reason;
        }
    }
}

