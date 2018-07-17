package cn.shuzilm.backend.pixel;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdPixelBean;

/**
 * Created by thunders on 2018/7/17.
 */
public class PixelFlowControl {
    private String nodeName;
    public PixelFlowControl(String nodeName){
        this.nodeName = nodeName;
    }
    public void sendStatus(AdPixelBean pixel){
        MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
    }


}
