package cn.shuzilm.backend.pixel;

import java.util.ArrayList;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.common.Constants;

/**
 * Created by thunders on 2018/7/17.
 */
public class PixelFlowControl {
    private static PixelFlowControl pixcel = null;

    public static PixelFlowControl getInstance(){
        if(pixcel == null)
            pixcel = new PixelFlowControl();
        return pixcel;
    }
    
    private String nodeName;
    public PixelFlowControl(){
        this.nodeName = Constants.getInstance().getConf("HOST");;
    }

    public void sendStatus(AdPixelBean pixel){   	
        MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
    }

    public static void main(String[] args) {
        AdPixelBean bean = new AdPixelBean();
        PixelFlowControl.getInstance().sendStatus(bean);
    }

    /**
     * 每隔 10 分钟更新一次广告素材或者人群包
     */
    public void pullTenMinutes() {
    	// 从 10 分钟的队列中获得广告素材和人群包
    	ArrayList<AdBean> adBeanList = MsgControlCenter.recvAdBean(nodeName);
    	
    }

}
