package cn.shuzilm.flowcontrol;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPixelBean;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/8/21.
 */
public class FLowControlTest {
    public static void main(String[] args) {
        FLowControlTest.testAverageMode();
    }

    public static void testAverageMode(){
        //uid : 7635434a-7ff2-45f6-9806-09b6d4908e2d
        String adUid = "7635434a-7ff2-45f6-9806-09b6d4908e2d";
        AdFlowControl adIns = AdFlowControl.getInstance();
        adIns.loadAdInterval(true);
        ConcurrentHashMap<String, AdBean> adMap = adIns.getMapAd();
        //模拟曝光请求超过了允许的范围
        AdPixelBean pixel = new AdPixelBean();
        pixel.setWinNoticeNums(1);
        pixel.setFinalCost(2000d);
        pixel.setAdUid(adUid);
        pixel.setType(0);
        pixel.setHost("rtb-001");

        try {
			PixelFlowControl.getInstance().sendStatus(pixel);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        while(true){
        AdFlowControl.getInstance().pullAndUpdateTask(false);
//        }

    }

    public void testFastMode(){

    }

    public void testAdQuota(){

    }

    public void testAdGroupQuota(){

    }

    public void testCpmHourLimit(){

    }

    public void testCpmDayList(){

    }

}
