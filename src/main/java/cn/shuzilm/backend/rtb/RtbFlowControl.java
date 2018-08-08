package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AreaBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.GpsGridBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.util.geo.GeoHash;
import cn.shuzilm.util.geo.GridMark;
import cn.shuzilm.util.geo.GridMark2;
import com.jcraft.jsch.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/7/17.
 */
public class RtbFlowControl {
    private static final org.slf4j.Logger myLog = LoggerFactory.getLogger(AdFlowControl.class);
    private static RtbFlowControl rtb = null;

    public static RtbFlowControl getInstance(){
        if(rtb == null){
            rtb = new RtbFlowControl();
        }
        return rtb;
    }

    public static void main(String[] args) {
        //从主控节点读取一些数据
        AdFlowControl.getInstance().loadAdInterval(true);
        //测试 RTB 引擎的
        RtbFlowControl.getInstance().trigger();
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

    /**
     * 广告任务管理
     */
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
    // 判断标签坐标是否在 广告主的选取范围内
    private GridMark2 grid = null;

    private RtbFlowControl(){
        MDC.put("sift","rtb");
        nodeName = Constants.getInstance().getConf("HOST");
        this.nodeName = nodeName;
        mapAd = new ConcurrentHashMap<>();
        mapTask = new ConcurrentHashMap<>();
        areaMap = new ConcurrentHashMap<>();
        mapAdCreative = new ConcurrentHashMap<>();
        // 判断标签坐标是否在 广告主的选取范围内
        grid = new GridMark2();

    }

    public void trigger(){
        // 5 s
        pullAndUpdateTask();

        // 10分钟拉取一次最新的广告内容
        pullTenMinutes();

        // 1 hour
        refreshAdStatus();
    }

    /**
     * 检查设备的标签所带的居住地、工作地、活动地坐标
     * @param lng
     * @param lat
     * @return
     */
    public ArrayList<String> checkInBound( double lng,double lat){
        ArrayList<String> uidList = grid.findGrid(lng,lat);
        return uidList;
    }

    /**
     * 每隔 10 分钟更新一次广告素材或者人群包
     */
    public void pullTenMinutes(){
        //从 10 分钟的队列中获得广告素材和人群包
        ArrayList<AdBean> adBeanList = MsgControlCenter.recvAdBean(nodeName);
        ArrayList<GpsBean> gpsAll = new ArrayList<>();
        if(adBeanList != null){
            for(AdBean adBean : adBeanList){
                //广告ID
                String uid = adBean.getAdUid();

                mapAd.put(uid,adBean);
                AudienceBean audience =  adBean.getAudience();
                if(audience != null){
                    //将 经纬度坐标装载到 MAP 中，便于快速查找
                    ArrayList<GpsBean> gpsList = audience.getGeoList();
                    if(gpsList != null){
                        for(GpsBean gps : gpsList){
                            gps.setPayload(uid);
                        }
                        gpsAll.addAll(gpsList);
                    }
                    //将 省、地级、县级装载到 MAP 中，便于快速查找
                    List<AreaBean> areaList = audience.getCityList();
                    for(AreaBean area: areaList){
                        if(area.getCountyId() == 0){
                            //当县级选项为 0 的时候，则认为是匹配全地级市
                            areaMap.put(area.getProvinceId() + "_" +area.getCityId() + "_",adBean.getAdUid());
                        }else
                            areaMap.put(area.getProvinceId() + "_" +area.getCityId() + "_" + area.getCountyId(),adBean.getAdUid());

                    }
                }else{
                    myLog.error(adBean.getAdUid() + "\t" + adBean.getName() + " 没有设置人群包..");
                }

                //广告内容的更新 ，按照素材的类型和尺寸
                CreativeBean creative =  adBean.getCreativeList().get(0);
                String creativeKey = creative.getType() +"_"+ creative.getWidth()+"_"+ + creative.getHeight();

                if(!mapAdCreative.contains(creativeKey)){
                    List<String> uidList = new ArrayList<String>();
                    uidList.add(uid);
                    mapAdCreative.put(creativeKey,uidList);
                }else{
                    List<String> uidList = mapAdCreative.get(creativeKey);
                    uidList.add(uid);
                }
            }

            //将 GPS 坐标加载到 栅格快速比对处理类中
            ArrayList<GpsGridBean> list = grid.reConvert(gpsAll);
            grid.init(list);
            myLog.info("广告共计加载条目数 : " + adBeanList.size());
            myLog.info("广告中的经纬度坐标共计条目数：" + list.size());

        }

    }

    /**
     * 每隔 5 秒钟从消息中心获得当前节点的当前任务，并与当前两个 MAP monitor 进行更新
     * 不包括素材
     *
     */
    public void pullAndUpdateTask(){
        TaskBean task = MsgControlCenter.recvTask(nodeName);
        if(task == null){
            return;
        }
        if(task != null){
            //把最新的任务更新到 MAP task 中
            mapTask.put(task.getAdUid(),task);
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
