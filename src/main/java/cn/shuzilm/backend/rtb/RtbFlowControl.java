package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.TaskBean;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/7/17.
 */
public class RtbFlowControl {
    private String nodeName ;
    /**
     * 广告资源管理
     */
    private static ConcurrentHashMap<String,TaskBean> mapAd = null;
    public RtbFlowControl(String nodeName){
        this.nodeName = nodeName;
        mapAd = new ConcurrentHashMap<>();
    }

    public void trigger(){
        // 5 s
        pullAndUpdateTask();
        // 1 hour
        refreshAdStatus();
    }
    /**
     * 每隔 5 秒钟从消息中心获得当前节点的当前任务，并与当前两个 MAP monitor 进行更新
     *
     */
    public void pullAndUpdateTask(){
        TaskBean task = MsgControlCenter.recvTask(nodeName);
        if(task != null){
            String uid = task.getTaskBean().getAdUid();
            mapAd.put(uid,task);
        }

    }

    /**
     * 每个小时重置一次 重置每个小时的投放状态，如果为暂停状态，且作用域为小时，则下一个小时可以继续开始
     */
    public void refreshAdStatus(){
        for(String auid : mapAd.keySet()){
            TaskBean bean = mapAd.get(auid);
            int scope = bean.getScope();
            int commandCode = bean.getCommand();
            if(scope == TaskBean.SCOPE_HOUR && commandCode == TaskBean.COMMAND_PAUSE){
                bean.setCommand(TaskBean.COMMAND_START);
            }
        }
    }

    /**
     * 监测广告是否可用
     * @param auid
     * @return
     */
    public boolean checkAvalable(String auid){
        TaskBean bean = mapAd.get(auid);
        int commandCode = bean.getCommand();
//        int scope = bean.getScope();
//        public static final int TASK_STATE_READY = 0;
//        public static final int TASK_STATE_RUNNING = 1;
//        public static final int TASK_STATE_FINISHED = 2;
//        public static final int TASK_STATE_PAUSED = 3;
//        public static final int TASK_STATE_STOPED = 4;
        switch(commandCode){
            case TaskBean.COMMAND_PAUSE:
                return false;
            case TaskBean.COMMAND_STOP:
                return false;
            default:
                break;
        }
        return true;
    }
}
