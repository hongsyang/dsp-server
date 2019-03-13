package cn.shuzilm.backend.master.node;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

				ConcurrentHashMap<String,Object[]> dynamicMap = bean.getDynamicMap();

				dynamicMap.forEach((key, value) -> {
					try{
						String [] keys = key.split("_");
						if(keys.length == 3 && value.length == 3) {
							// 更新动态出价缓存map
							String packageName = keys[0];
							String tagId = keys[1];
							String[] size = keys[2].split("#");
							if(size.length == 2) {
								int width = Integer.parseInt(size[0]);
								int height = Integer.parseInt(size[1]);
								if(width > 0 && height > 0) {
									AdFlowControl.getInstance().updateDynamicPriceMap("RTB", (long)value[0],packageName,tagId,width,height,(float)value[1]);
									// 更新转换Map
									AdFlowControl.getDynamicTransferMap().put((String)value[2], key);
								}
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}

				});
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
