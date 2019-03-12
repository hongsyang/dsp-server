package cn.shuzilm.backend.master.node;

import java.util.ArrayList;

import org.slf4j.MDC;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBidBean;
import cn.shuzilm.bean.control.NodeStatusBean;

public class GainDataFromRTBQueue implements Runnable {

	private String nodeName;

	public GainDataFromRTBQueue(String nodeName) {
		this.nodeName = nodeName;
	}

	@Override
	public void run() {
		while(true){
			try {
				MDC.put("sift", "control");
				//从各个 RTB 节点，获得最新的 bids 个数，并更新至内存监控
				NodeStatusBean bean = MsgControlCenter.recvBidStatus(nodeName);
				if (bean == null)
					continue;
				ArrayList<AdBidBean> bidList = bean.getBidList();
				for (AdBidBean bid : bidList) {
					String id = bid.getUid();
					if(id.contains("dynamic_")) {
						String [] values = id.split("_");
						if(values.length == 3) {

						}
						// 更新动态出价缓存map
						AdFlowControl.getInstance().updateDynamicPriceMap("RTB", bid.getBidNums(),"","","","",0f);
					}else {
						AdFlowControl.getInstance().updateBids(bid.getUid(), bid.getBidNums());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(5 * 10);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
	}

}
