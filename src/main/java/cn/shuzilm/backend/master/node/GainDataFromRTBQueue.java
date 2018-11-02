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
					AdFlowControl.getInstance().updateBids(bid.getUid(), bid.getBidNums());
				}
			} catch (Exception e) {
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				continue;
			}
		}
	}

}
