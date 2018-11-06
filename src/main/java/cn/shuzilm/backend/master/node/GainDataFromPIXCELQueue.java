package cn.shuzilm.backend.master.node;

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
