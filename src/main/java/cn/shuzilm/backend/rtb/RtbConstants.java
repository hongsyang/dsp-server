package cn.shuzilm.backend.rtb;

import java.util.concurrent.ConcurrentHashMap;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.common.Constants;

public class RtbConstants {
	
	public static String HOST = "HOST";
	public static String IMP_PROCESS = "IMP_PROCESS";
	public static String CREATIVE_QUALITY = "CREATIVE_QUALITY";
	public static String MONEY_LEFT = "MONEY_LEFT";
	public static String ADVERTISER_SCORE = "ADVERTISER_SCORE";
	public static String CTR_SCORE = "CTR_SCORE";
	public static String LOCATION = "LOCATION";
	public static String DEMOGRAPHIC = "DEMOGRAPHIC";
	public static String COMPANY = "COMPANY";
	
	
	private static RtbConstants con;
	private static ConcurrentHashMap<String, Float> rtbMap = null;
	private Constants constant;

    public static RtbConstants getInstance(){
        if(con == null){
            con =  new RtbConstants();
            return con;
        }else{
            return con;
        }
    }
    
    private RtbConstants(){
    	constant = Constants.getInstance();
    	rtbMap = new ConcurrentHashMap<String, Float>();
    	rtbMap.put(IMP_PROCESS, Float.parseFloat(constant.getConf(IMP_PROCESS)));
    	rtbMap.put(CREATIVE_QUALITY, Float.parseFloat(constant.getConf(CREATIVE_QUALITY)));
    	rtbMap.put(MONEY_LEFT, Float.parseFloat(constant.getConf(MONEY_LEFT)));
    	rtbMap.put(ADVERTISER_SCORE, Float.parseFloat(constant.getConf(ADVERTISER_SCORE)));
    	rtbMap.put(CTR_SCORE, Float.parseFloat(constant.getConf(CTR_SCORE)));
    	rtbMap.put(LOCATION, Float.parseFloat(constant.getConf(LOCATION)));
    	rtbMap.put(DEMOGRAPHIC, Float.parseFloat(constant.getConf(DEMOGRAPHIC)));
    	rtbMap.put(COMPANY, Float.parseFloat(constant.getConf(COMPANY)));
    }
    
    public float getRtbVar(String key){
    	return rtbMap.get(key);
    }
}
