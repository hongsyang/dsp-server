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
    
    private double dspToleranceEqualRatio = 0.02;
    
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
    	if(ad == null){
    		return -1;
    	}
    	if(ad.getAdvertiser().getAgencyBean() != null)
    		rebate = ad.getAdvertiser().getAgencyBean().getRebate();//获取代理商返点比例
    	double dspProfit = 0.0;
    	double rebateProfit = 0.0;
    	double cost = pixel.getCost();
    	double premiumFactor = pixel.getPremiumFactor();
    	double dspAndRebatePremiumFactor = premiumFactor + rebate;
    	double price = ad.getPrice();//广告出价
    	double finalPrice = cost / (1 - dspAndRebatePremiumFactor);
    	if(finalPrice > price){//最终消耗金额高于广告出价金额,适当调整DSP平台利润
    		double dspAndRebatePremiumFactorTemp = dspToleranceRatio + rebate;//DSP保守利润率+代理商返点比例
    		double tempPrice = cost / (1 - dspAndRebatePremiumFactorTemp);
    		if(tempPrice == price){
    			double dspAndRebatePremiumEqualFactor = dspToleranceRatio - dspToleranceEqualRatio + rebate;
    			double tempFinalPrice = cost / (1 - dspAndRebatePremiumEqualFactor);
    			finalPrice = tempFinalPrice;
    			dspProfit = finalPrice * (dspToleranceRatio - dspToleranceEqualRatio);
    			rebateProfit = finalPrice * rebate;
    			pixel.setLower(true);
    		}else if(tempPrice < price){
    			finalPrice = tempPrice;
    			dspProfit = finalPrice * dspToleranceRatio;
    			rebateProfit = finalPrice * rebate;
    			pixel.setLower(true);
    		}else{
    			dspProfit = finalPrice * premiumFactor;
    			rebateProfit = finalPrice * rebate;
    			pixel.setLower(false);
    		}
    	}else if(finalPrice == price){
    		double dspAndRebatePremiumEqualFactor = dspAndRebatePremiumFactor - dspToleranceEqualRatio;
    		finalPrice = cost / (1 - dspAndRebatePremiumEqualFactor);
    		dspProfit = finalPrice * (premiumFactor - dspToleranceEqualRatio);
			rebateProfit = finalPrice * rebate;
    		pixel.setLower(true);
    	}else{
    		dspProfit = finalPrice * premiumFactor;
			rebateProfit = finalPrice * rebate;
    		pixel.setLower(true);
    	}
    	pixel.setFinalCost(finalPrice);
    	pixel.setDspProfit(dspProfit);
    	pixel.setRebateProfit(rebateProfit);
        MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
        return finalPrice;
    }

    public static void main(String[] args) {
        AdPixelBean bean = new AdPixelBean();
        bean.setCost(40.0);
        bean.setPremiumFactor(0.5);
        bean.setAdUid("123");
        System.out.println(PixelFlowControl.getInstance().sendStatus(bean));
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
