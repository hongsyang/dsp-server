package cn.shuzilm.backend.pixel;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.common.Constants;

/**
 * Created by thunders on 2018/7/17.
 */
public class PixelFlowControl {
    private static PixelFlowControl pixcel = null;
    private PixelFlowControl(){
    }

    public static PixelFlowControl getInstance(){
        if(pixcel == null){
            String nodeName = Constants.getInstance().getConf("HOST");
            pixcel = new PixelFlowControl(nodeName);
        }
        return pixcel;
    }
    
    private String nodeName;
    public PixelFlowControl(String nodeName){
        this.nodeName = nodeName;
    }

    public void sendStatus(AdPixelBean pixel){
        MsgControlCenter.sendPixelStatus(this.nodeName,pixel);
    }

    public static void main(String[] args) {
        AdPixelBean bean = new AdPixelBean();
        PixelFlowControl.getInstance().sendStatus(bean);
    }


}
