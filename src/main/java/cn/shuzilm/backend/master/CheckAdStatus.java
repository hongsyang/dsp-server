package cn.shuzilm.backend.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdFlowStatus;

public class CheckAdStatus {
	
	private static CheckAdStatus checkAdStatus;
	
	private static  AdFlowControl adFlowControl;
	
	private static CPCHandler cpcHandler;
	
	private static final Logger myLog = LoggerFactory.getLogger(CheckAdStatus.class);
	
	public static CheckAdStatus getInstance(){
		if(checkAdStatus == null){
			adFlowControl = AdFlowControl.getInstance();
			checkAdStatus = new CheckAdStatus();
			cpcHandler = CPCHandler.getInstance();
		}
		return checkAdStatus;
	}
	
	public boolean getAdStatus(String adUid,boolean isUpdate){
		
		MDC.put("sift", "control");
		AdFlowStatus threshold = adFlowControl.getMapThresholdHour().get(adUid);
        AdFlowStatus monitor = adFlowControl.getMapMonitorHour().get(adUid);
        //每小时曝光超过了设置的最大阀值，则终止该小时的广告投放
        if (threshold != null && monitor != null && threshold.getWinNums() != 0 && monitor.getWinNums() >= threshold.getWinNums() * 0.95) {
            String reason = "#### 小时 CPM 超限，参考指标：" + threshold.getWinNums() + "(CPM)\t" + monitor.getWinNums() + "(CPM) ### " ;
            myLog.error(adUid+" 广告未开启原因:"+reason);
            return false;
        }
        String groupId = adFlowControl.getMapAd().get(adUid).getGroupId();
        AdFlowStatus monitorAdGroup = adFlowControl.getMapMonitorAdGroupTotal().get(groupId);
        AdFlowStatus monitorTotalAdGroup = adFlowControl.getMapMonitorAdGroupRealTotal().get(groupId);
        double thresholdGroupMoney = adFlowControl.getMapAdGroup().get(groupId).getQuotaMoney().doubleValue()*1000;
        double thresholdTotalGroupMoney = adFlowControl.getMapAdGroup().get(groupId).getQuotaTotalMoney().doubleValue()*1000;
        int quota = adFlowControl.getMapAdGroup().get(groupId).getQuota();
        int quotaTotal = adFlowControl.getMapAdGroup().get(groupId).getQuota_total();
        
        if(monitorAdGroup != null && thresholdGroupMoney != 0 && quota == 1 && monitorAdGroup.getMoney() >= thresholdGroupMoney * 0.95){
            //广告组每日限额超限，则发送停止命令，终止该广告投放
            String reason = "#### 广告组 每日限额 超限，参考指标：" + thresholdGroupMoney + "元(CPM)\t" + monitorAdGroup.getMoney() + "元 (CPM)###";
            myLog.error(adUid + " 广告未开启原因:" + reason);
            return false;
        }
        
        if(monitorTotalAdGroup != null && thresholdTotalGroupMoney != 0 && quotaTotal == 1 && monitorTotalAdGroup.getMoney() >= thresholdTotalGroupMoney * 0.95){
            //广告组总限额超限，则发送停止命令，终止该广告投放
            String reason = "#### 广告组 总限额 超限，参考指标：" + thresholdTotalGroupMoney + "元(CPM)\t" + monitorTotalAdGroup.getMoney() + "元(CPM) ###";
            myLog.error(adUid + " 广告未开启原因:" + reason);
            return false;
        }
        AdFlowStatus thresholdDaily = adFlowControl.getMapThresholdDaily().get(adUid);
        AdFlowStatus monitorDaily = adFlowControl.getMapMonitorDaily().get(adUid);
        String advertiserId = adFlowControl.getMapAd().get(adUid).getAdvertiser().getUid();
        AdFlowStatus advertiserDaily = adFlowControl.getMapMonitorAdvertiserDaily().get(advertiserId);
        AdFlowStatus thresholdAdvertiser = adFlowControl.getMapMonitorAdvertiserThresholdDaily().get(advertiserId);
        
        if(advertiserDaily != null && thresholdAdvertiser != null && thresholdAdvertiser.getMoney() != 0 && advertiserDaily.getMoney() >= thresholdAdvertiser.getMoney() * 0.95){
        	String reason = "#### 广告主每日金额 超限，参考指标：" + thresholdAdvertiser.getMoney() + "元(CPM)\t" + advertiserDaily.getMoney() + "元(CPM) ###";
            myLog.error(adUid+" 广告未开启原因:"+advertiserDaily.toString() + "\t" + reason);
            return false;
        }
        if (monitorDaily != null && thresholdDaily != null && thresholdDaily.getMoney() != 0 && monitorDaily.getMoney() >= thresholdDaily.getMoney() * 0.95) {
            //金额超限，则发送小时控制消息给各个节点，终止该小时广告投放
            String reason = "#### 每日金额 超限，参考指标：" + thresholdDaily.getMoney() + "元(CPM)\t" + monitorDaily.getMoney() + "元(CPM) ###";
            myLog.error(adUid+" 广告未开启原因:"+monitorDaily.toString() + "\t" + reason);
            return false;
        }
        
        if (monitorDaily != null && thresholdDaily != null && thresholdDaily.getWinNums() != 0 && monitorDaily.getWinNums() >= thresholdDaily.getWinNums() * 0.95) {
            String reason = "#### 每日 CPM 超限，参考指标：" + thresholdDaily.getWinNums() + "(CPM)\t" + monitorDaily.getWinNums() + "(CPM) ### " ;
            myLog.error(adUid+" 广告未开启原因:"+monitorDaily.toString() + "\t" + reason);
            return false;
        }
        
        AdBean ad = adFlowControl.getMapAd().get(adUid);
    	String mode = ad.getMode();
    	if("cpc".equalsIgnoreCase(mode)){
    	//监测 CPC 类型的广告是否可以投放
    	if(isUpdate){
    		AdFlowStatus flowStatus = cpcHandler.getMapMonitorTotal().get(adUid);
    		if(flowStatus != null){
    			flowStatus.setClickNums(0);
    			flowStatus.setMoney(0);
    			flowStatus.setWinNums(0);
    			myLog.info(adUid+" 重新累计CPC曝光数与限额!");
    		}
    	}
        String isOk = cpcHandler.checkAvailable(adUid);
        if(isOk != null){
        	 myLog.error(adUid+" 广告未开启原因:"+isOk);
        	 return false;
        }        	
        }
    	
    	
		return true;
	}
}
