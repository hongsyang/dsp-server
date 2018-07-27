package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AudienceBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/7/17.
 */
public class RtbFlowControl {
    private static RtbFlowControl rtb = null;
    private RtbFlowControl(){

    }

    public static RtbFlowControl getInstance(){
        if(rtb == null){
            rtb = new RtbFlowControl();
        }
        return rtb;
    }

    public ConcurrentHashMap<String,AdBean> getAdMap(){
        return mapAd;
    }

    public ConcurrentHashMap<String,List<String>> getCreativeMap(){
        return mapAdCreative;
    }


    private String nodeName ;
    /**
     * 广告资源管理
     * key: aduid : taskBean
     */
    private static ConcurrentHashMap<String,AdBean> mapAd = null;

    private static ConcurrentHashMap<String,TaskBean> mapTask = null;
    /**
     * 广告资源的倒置
     * key: 广告类型  + 广告宽 + 广告高
     * value: list<aduid>
     */
    private static ConcurrentHashMap<String,List<String>> mapAdCreative = null;

    /**
     * 省级、地级、县级 map
     * key: 北京市北京市海淀区  河北省廊坊地区广平县
     * value：aduid
     */
    private static ConcurrentHashMap<String,String> areaMap = null;


    public RtbFlowControl(String nodeName){
        this.nodeName = nodeName;
        mapAd = new ConcurrentHashMap<>();
        mapTask = new ConcurrentHashMap<>();
        areaMap = new ConcurrentHashMap<>();
    }

    public void trigger(){
        // 5 s
        pullAndUpdateTask();
        // 1 hour
        refreshAdStatus();
    }

    /**
     * 每隔 10 分钟更新一次广告素材或者人群包
     */
    public void pullTenMinutes(){
        //从 10 分钟的队列中获得广告素材和人群包
        AdBean adBean = MsgControlCenter.recvAdBean(nodeName);
        if(adBean != null){
            //广告ID
            String uid = adBean.getAdUid();
            mapAd.put(uid,adBean);
            AudienceBean audience =  adBean.getAudience();

            //广告内容的更新 ，按照素材的类型和尺寸
            CreativeBean creative =  adBean.getCreativeList().get(0);
            String creativeKey = creative.getType() + creative.getWidth() + creative.getHeight();

            if(!mapAdCreative.contains(creativeKey)){
                List<String> taskList = new ArrayList<String>();
                taskList.add(uid);
                mapAdCreative.put(creativeKey,taskList);
            }else{
                List<String> taskList = mapAdCreative.get(creativeKey);
                taskList.add(uid);
            }
        }

    }

    /**
     * 每隔 5 秒钟从消息中心获得当前节点的当前任务，并与当前两个 MAP monitor 进行更新
     * 不包括素材
     *
     */
    public void pullAndUpdateTask(){
        TaskBean task = MsgControlCenter.recvTask(nodeName);
        if(task != null){
            mapTask.put(task.getAdUid(),task);
            //广告ID
            String uid = task.getAdUid();



        }

    }

    /**
     * 每个小时重置一次 重置每个小时的投放状态，如果为暂停状态，且作用域为小时，则下一个小时可以继续开始
     */
    public void refreshAdStatus(){
        for(String auid : mapTask.keySet()){
            TaskBean bean = mapTask.get(auid);
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
        TaskBean bean = mapTask.get(auid);
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
