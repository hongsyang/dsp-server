package cn.shuzilm.backend.master.node;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.control.AdPixelBean;

public class GainDataFromPIXCELQueue implements Runnable {

	private String nodeName;
	
	private static final Logger LOG = LoggerFactory.getLogger(PixelFlowControl.class);
	
	public GainDataFromPIXCELQueue(String nodeName) {
		this.nodeName = nodeName;
	}

	@Override
	public void run() {

		while (true) {
			try {
				MDC.put("sift", "control");
				//从各个 PIXCEL 节点获得最新 wins 和 金额消费情况， 并更新至内存监控
				AdPixelBean pix = MsgControlCenter.recvPixelStatus(nodeName);
				if (pix == null)
					continue;
				AdFlowControl.getInstance().updatePixel(pix.getAdUid(), pix.getWinNoticeNums(),
						Float.valueOf(pix.getFinalCost().toString()), -1, pix.getClickNums(), pix.getType(),
						pix.isLower());

				// 更新动态出价缓存map
				String reqestId = pix.getRequestId();
				//LOG.debug("requestId: {}",reqestId);
				/*AdFlowControl.getDynamicTransferMap().forEach((key,value) -> {
					LOG.debug("转换keyMap： key: {} valuke: {}", key,value);
				});*/
				if(StringUtils.isNotEmpty(reqestId)) {
					String mapKey = AdFlowControl.getDynamicTransferMap().get(reqestId);
					if(StringUtils.isNotEmpty(mapKey)) {
						AdFlowControl.getInstance().updateDynamicPriceMap("PIXEL", 1,mapKey,pix.getCost());
						/*String [] keys = mapKey.split("_");
						if(keys.length == 3) {
							String packageName = keys[0];
							String tagId = keys[1];
							String size = keys[2];
							double price = pix.getBidPrice();
							AdFlowControl.getInstance().updateDynamicPriceMap("PIXEL", 1,packageName,tagId,size,price);
						}*/
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

	public static void main(String[] args) {
		AdFlowControl.getInstance();
		AdFlowControl.getDynamicTransferMap().put("requestid1","com.dengjian.ios_null_200#300");
		new Thread(new GainDataFromPIXCELQueue("rtb-101")).start();
	}
}
