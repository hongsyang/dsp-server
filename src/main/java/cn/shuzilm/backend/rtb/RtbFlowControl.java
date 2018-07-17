package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.TaskBean;

import java.util.HashMap;

/**
 * Created by thunders on 2018/7/17.
 */
public class RtbFlowControl {
    private String nodeName ;
    /**
     * 广告资源管理
     */
    private static HashMap<String,TaskBean> mapAd = null;
    public RtbFlowControl(String nodeName){
        this.nodeName = nodeName;
        mapAd = new HashMap<>();
    }
    /**
     * 每隔 5 秒钟从消息中心获得当前节点的当前任务，并与当前两个 MAP monitor 进行更新
     *
     */
    public void pullAndUpdateTask(){
        TaskBean task = MsgControlCenter.recvTask(nodeName);
        String uid = task.getTaskBean().getAdUid();
        mapAd.put(uid,task);
    }

    /**
     * 重置每个小时的投放状态，如果为暂停状态，且作用域为小时，则下一个小时可以继续开始
     */
    public void refreshAdStatus(){

    }
}
