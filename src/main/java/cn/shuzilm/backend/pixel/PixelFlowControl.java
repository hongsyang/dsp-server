package cn.shuzilm.backend.pixel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.common.Constants;

/**
 * Created by thunders on 2018/7/17.
 */
public class PixelFlowControl {
	
	private static final Logger LOG = LoggerFactory.getLogger(PixelFlowControl.class);
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

    public AdPixelBean sendStatus(AdPixelBean pixel){
    	if(pixel.getClickNums() == 1){
    		MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
            return pixel;
    	}
    	String adUid = pixel.getAdUid();
    	AdBean ad = mapAd.get(adUid);
    	double rebate = 0.0;
    	if(ad == null){
    		return null;
    	}
    	if(ad.getAdvertiser().getAgencyBean() != null)
    		rebate = ad.getAdvertiser().getAgencyBean().getRebate();//获取代理商返点比例
    	double dspProfit = 0.0;
    	double rebateProfit = 0.0;
    	double cost = pixel.getCost();
    	double premiumFactor = pixel.getPremiumFactor();
    	double dspAndRebatePremiumFactor = premiumFactor + rebate;
    	double price = ad.getPrice();
    	double finalPrice = getResult(cost, getResult(1.0, dspAndRebatePremiumFactor, "-"),"/");
    	if(finalPrice > price){//最终消耗金额高于广告出价金额,适当调整DSP平台利润
    		double dspAndRebatePremiumFactorTemp = dspToleranceRatio + rebate;//DSP保守利润率+代理商返点比例
    		double tempPrice = getResult(cost, (getResult(1.0, dspAndRebatePremiumFactorTemp, "-")),"/");
    		if(tempPrice == price){
    			double dspAndRebatePremiumEqualFactor = dspToleranceRatio - dspToleranceEqualRatio + rebate;
    			double tempFinalPrice = getResult(cost, (getResult(1.0, dspAndRebatePremiumEqualFactor, "-")),"/");
    			finalPrice = tempFinalPrice;
    			dspProfit = getResult(finalPrice, dspToleranceRatio - dspToleranceEqualRatio, "*");
    			rebateProfit = getResult(finalPrice, rebate, "*");   			
    			pixel.setLower(true);
    		}else if(tempPrice < price){
    			finalPrice = tempPrice;
    			dspProfit = getResult(finalPrice, dspToleranceRatio, "*"); 			
    			rebateProfit = getResult(finalPrice, rebate, "*");
    			pixel.setLower(true);
    		}else{
    			dspProfit = getResult(finalPrice, premiumFactor, "*");
    			rebateProfit = getResult(finalPrice, rebate, "*");   			
    			pixel.setLower(false);
    		}
    	}else if(finalPrice == price){
    		double dspAndRebatePremiumEqualFactor = dspAndRebatePremiumFactor - dspToleranceEqualRatio;
    		finalPrice = getResult(cost, (getResult(1.0, dspAndRebatePremiumEqualFactor, "-")),"/");  		
    		dspProfit = getResult(finalPrice, premiumFactor - dspToleranceEqualRatio, "*");   		
			rebateProfit = getResult(finalPrice, rebate, "*"); 			
    		pixel.setLower(true);
    	}else{
    		dspProfit = getResult(finalPrice, premiumFactor, "*");   		 	
			rebateProfit = getResult(finalPrice, rebate, "*");			   
    		pixel.setLower(true);
    	}    	
    	
    	pixel.setFinalCost(finalPrice);
    	pixel.setDspProfit(getResult(dspProfit, 1.0, "/"));
    	pixel.setRebateProfit(getResult(rebateProfit, 1.0, "/"));
    	
    	LOG.info("dsp利润="+pixel.getDspProfit());
    	LOG.info("代理商利润="+pixel.getRebateProfit());
    	LOG.info("成本价="+pixel.getCost());
    	LOG.info("总消耗金额="+(pixel.getFinalCost()));
        MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
        return pixel;
    }

    public static void main(String[] args) {
        AdPixelBean bean = new AdPixelBean();
        bean.setCost(40.0);
        bean.setPremiumFactor(0.5);
        bean.setAdUid("123");
        AdPixelBean pixel = PixelFlowControl.getInstance().sendStatus(bean);
        
        System.out.println("dsp利润="+pixel.getDspProfit());
        System.out.println("代理商利润="+pixel.getRebateProfit());
        System.out.println("成本价="+pixel.getCost());
        System.out.println("总消耗金额="+(pixel.getFinalCost()));
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
    
    /**
	 * 
	 * @param numA 数字A
	 * @param numB 数字B
	 * @param operate 运算符
	 * @return
	 */
	public double getResult(double numA, double numB, String operate){
		double res = 0;
		BigDecimal bigA = new BigDecimal(Double.toString(numA));
		BigDecimal bigB = new BigDecimal(Double.toString(numB));
		switch (operate) {
			
			case "+":
				res = bigA.add(bigB).doubleValue();
				break;
			case "-":
				res = bigA.subtract(bigB).doubleValue();
				break;
			case "*":
				res = bigA.multiply(bigB).doubleValue();
				break;
			case "/":
				res = bigA.divide(bigB,5,RoundingMode.HALF_DOWN).doubleValue();
				break;
			default :
				break;
		}
		return res;
	}


}
