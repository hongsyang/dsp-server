package cn.shuzilm.backend.pixel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.common.Constants;

/**
 * Created by thunders on 2018/7/17.
 */
public class PixelFlowControl {
    private static PixelFlowControl pixcel = null;
    
    private double dspToleranceRatio = 0.3;
    
    private static ConcurrentHashMap<String,AdBean> mapAd = null;
    
    public ConcurrentHashMap<String, AdBean> getAdMap() {
        return mapAd;
    }

    public static PixelFlowControl getInstance(){
        if(pixcel == null){
            String nodeName = Constants.getInstance().getConf("HOST");
            pixcel = new PixelFlowControl(nodeName);
        }
        return pixcel;
    }
    
    private String nodeName;
    public PixelFlowControl(String nodeName){
        this.nodeName = nodeName;
        mapAd = new ConcurrentHashMap<String,AdBean>();
    }

    public double sendStatus(AdPixelBean pixel){
    	String adUid = pixel.getAdUid();
    	AdBean ad = mapAd.get(adUid);
    	double rebate = 0.0;
    	if(ad.getAdvertiser().getAgencyBean() != null)
    		rebate = ad.getAdvertiser().getAgencyBean().getRebate();//获取代理商返点比例
    	double cost = pixel.getCost();
    	double premiumFactor = pixel.getPremiumFactor();
    	double actualPricePremium = cost * premiumFactor;//DSP平台利润
    	double agencyProfit = cost * rebate;//代理商利润
    	double price = ad.getPrice();//广告出价
    	double finalPrice = cost + agencyProfit + actualPricePremium;
    	if(finalPrice > price){//最终消耗金额高于广告出价金额,适当调整DSP平台利润
    		double tempPrice = price - agencyProfit - cost;
    		if(tempPrice > 0){
    			double tempRatio = tempPrice / actualPricePremium;
    			if(tempRatio > dspToleranceRatio){
    				finalPrice = cost + agencyProfit + actualPricePremium * tempRatio;
    			}
    		}
    	}   
    	pixel.setFinalCost(finalPrice);
        MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
        return finalPrice;
    }

    public static void main(String[] args) {
        AdPixelBean bean = new AdPixelBean();
        PixelFlowControl.getInstance().sendStatus(bean);
    }

    /**
     * 每隔 10 分钟更新一次广告素材或者人群包
     */
    public void pullTenMinutes() {
        // 从 10 分钟的队列中获得广告素材和人群包
        ArrayList<AdBean> adBeanList = MsgControlCenter.recvAdBean(nodeName);
        if(adBeanList != null){
        for(AdBean ad:adBeanList){
        	mapAd.put(ad.getAdUid(), ad);
        }
        }
    }

}
