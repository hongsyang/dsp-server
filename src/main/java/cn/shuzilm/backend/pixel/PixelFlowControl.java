package cn.shuzilm.backend.pixel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;

import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.backend.master.TaskServicve;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.NodeStatusBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.common.PixelConstants;

/**
 * Created by thunders on 2018/7/17.
 */
public class PixelFlowControl {

	private static final Logger LOG = LoggerFactory.getLogger(PixelFlowControl.class);
	private static PixelFlowControl pixcel = null;

	private double dspToleranceRatio = 0.3;

	private double dspToleranceEqualRatio = 0.02;
		
	private static TaskServicve taskService = new TaskServicve();

	private static ConcurrentHashMap<String, AdBean> mapAd = null;

	public ConcurrentHashMap<String, AdBean> getAdMap() {
		return mapAd;
	}

	public static PixelFlowControl getInstance() {
		if (pixcel == null) {
			String nodeName = PixelConstants.getInstance().getConf("HOST");
			pixcel = new PixelFlowControl(nodeName);
		}
		return pixcel;
	}

	private String nodeName;

	public PixelFlowControl(String nodeName) {
		this.nodeName = nodeName;
		MDC.put("sift", "pixel");
		mapAd = new ConcurrentHashMap<String, AdBean>();		
	}

	public AdPixelBean sendStatus(AdPixelBean pixel) throws Exception{
		MDC.put("sift", "pixel");
		if (pixel.getClickNums() == 1) {
			pixel.setFinalCost(0.0);
			pixel.setLower(true);
			MsgControlCenter.sendPixelStatus(this.nodeName, pixel);
			return pixel;
		}
		String adUid = pixel.getAdUid();
		AdBean ad = mapAd.get(adUid);
		double rebate = 0.0;
		if (ad == null) {
			LOG.warn("根据ADUID[" + adUid + "]未找到广告!");
			return null;
		}
		if (ad.getAdvertiser() != null && ad.getAdvertiser().getAgencyBean() != null)
			rebate = ad.getAdvertiser().getAgencyBean().getRebate();// 获取代理商返点比例
		double dspProfit = 0.0;
		double rebateProfit = 0.0;
		double cost = pixel.getCost();
		double premiumFactor = pixel.getPremiumFactor();
		double price = 0.0;
		if(pixel.getBidPrice() != null && pixel.getBidPrice() > 0){
			price = (double) (Math.round(pixel.getBidPrice() * 100000)/100000.0);
		}else{
			price = (double) (Math.round(ad.getPrice() * 100000)/100000.0);
		}
		
		
		
		if(premiumFactor >= 1){
			dspToleranceRatio = 0.5;
		}else if(premiumFactor >= 0.5 && premiumFactor < 1){
			dspToleranceRatio = 0.3;
		}
		
		double finalPrice = 0.0;
		
		double rebateProfitTemp = (double) (Math.round(cost * rebate * 100000)/100000.0);
		
		double profitTemp = (double) (Math.round((price - cost - rebateProfitTemp) / cost * 100000)/100000.0);
		
		if(profitTemp < dspToleranceRatio){
			finalPrice = cost + rebateProfitTemp + cost * premiumFactor;
			rebateProfit = finalPrice * rebate;
			dspProfit = finalPrice - cost - rebateProfit;
			pixel.setLower(false);			
		}else if(profitTemp >= dspToleranceRatio && profitTemp <= premiumFactor){
			finalPrice = cost + rebateProfitTemp + cost * profitTemp;
			rebateProfit = finalPrice * rebate;
			dspProfit = finalPrice - cost - rebateProfit;
			pixel.setLower(true);
		}else if(profitTemp > premiumFactor){
			finalPrice = cost + rebateProfitTemp + cost * premiumFactor;
			rebateProfit = finalPrice * rebate;
			dspProfit = finalPrice - cost - rebateProfit;
			pixel.setLower(true);
		}
		

		pixel.setFinalCost((double) (Math.round(finalPrice * 100000)/100000.0));		
		pixel.setDspProfit((double) (Math.round(dspProfit * 100000)/100000.0));		
		pixel.setRebateProfit((double) (Math.round(rebateProfit * 100000)/100000.0));
		
		LOG.info("dsp利润=" + pixel.getDspProfit());
		LOG.info("代理商利润=" + pixel.getRebateProfit());
		LOG.info("成本价=" + pixel.getCost());
		LOG.info("出价=" + price);
		LOG.info("总消耗金额=" + (pixel.getFinalCost()));
		LOG.info("广告状态=" + pixel.isLower());
		if(pixel.getWinNoticeNums() > 0){
			LOG.info("wintoice=1,上报主控!");
			MsgControlCenter.sendPixelStatus(this.nodeName, pixel);
		}
		return pixel;
	}

	public static void main(String[] args) {
		AdPixelBean bean = new AdPixelBean();
		bean.setCost(5.01);
		bean.setPremiumFactor(1.0);
		bean.setAdUid("123");
		bean.setBidPrice(10d);
		bean.setRequestId("requestid1");
		bean.setClickNums(1);
		AdBean ad = new AdBean();
        ad.setAdUid("123");
        String adverUid = "123";
        ad.setPrice(8.517F);
        PixelFlowControl pixcel = PixelFlowControl.getInstance();
        mapAd.put(ad.getAdUid(), ad);
		AdPixelBean pixel;
		try {
			long startTime = System.currentTimeMillis();
			for(int i=0;i<1;i++){
			pixel = pixcel.sendStatus(bean);
			}
			System.out.println("花费时间:"+(System.currentTimeMillis()-startTime));
//			System.out.println("dsp利润=" + pixel.getDspProfit());
//			System.out.println("代理商利润=" + pixel.getRebateProfit());
//			System.out.println("成本价=" + pixel.getCost());
//			System.out.println("总消耗金额=" + (pixel.getFinalCost()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/**
	 * 每隔 1秒钟更新一次广告素材或者人群包
	 */
	public void pullTenMinutes() {
		MDC.put("sift", "pixel");
		// 从 10 分钟的队列中获得广告素材和人群包
		ArrayList<AdBean> adBeanList = MsgControlCenter.recvAdBean(nodeName);
		if (adBeanList != null) {
			LOG.info("广告共计加载条目数:" + adBeanList.size());
			for (AdBean ad : adBeanList) {
				mapAd.put(ad.getAdUid(), ad);
			}
		}
	}
	
	public void pullAdFromDB(){
		MDC.put("sift", "pixel");
		try {
			ResultList adList = taskService.queryAllAd();
			for(ResultMap map:adList){
				try{
				AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));
                String adverUid = map.getString("advertiser_uid");
                AdvertiserBean adver = taskService.queryAdverByUid(adverUid);
                ad.setAdvertiser(adver);
                String mode = map.getString("mode");
                if("cpc".equalsIgnoreCase(mode)){
                	ad.setPrice(map.getBigDecimal("price").floatValue() * 0.006f * 1000);               	
                }else{
                	ad.setPrice(map.getBigDecimal("price").floatValue());
                }
                mapAd.put(ad.getAdUid(), ad);
				}catch(Exception ex){
					continue;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 每隔 10分钟上报pixel引擎心跳
	 */
	public void pushPixelHeart() {
		MDC.put("sift", "pixel");
		NodeStatusBean bean = new NodeStatusBean();
		bean.setLastUpdateTime(System.currentTimeMillis());
		MsgControlCenter.sendNodeStatus(nodeName, bean);
	}

	/**
	 * 
	 * @param numA
	 *            数字A
	 * @param numB
	 *            数字B
	 * @param operate
	 *            运算符
	 * @return
	 */
	public double getResult(double numA, double numB, String operate) {
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
			res = bigA.divide(bigB, 5, RoundingMode.HALF_DOWN).doubleValue();
			break;
		default:
			break;
		}
		return res;
	}

}
