package cn.shuzilm.backend.rtb;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AreaBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.TagBean;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.JsonTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by thunders on 2018/7/17.
 */
public class RuleMatching {

    private AsyncRedisClient redis;

    private  RtbFlowControl rtbIns;
    /**
     * 随机数控制开启标签顺序，雨露均沾
     */
    private Random tagRandom;
    /**
     * 随机数控制广告进度等顺序，雨露均沾
     */
    private Random adRandom;

    public RuleMatching(String[] nodes){
        redis = new AsyncRedisClient(nodes);
        rtbIns = RtbFlowControl.getInstance();
        tagRandom = new Random();
        adRandom = new Random();

        //获得广告价格、素材、广告主资金余额、
        //TODO
        //加载标签溢价比例
        //TODO
    }

    /**
     * 创意类型、尺寸匹配
     */
    public void filter(String type,int width,int height){

    }

    /**
     * 将设备ID 的标签从加速层取出，并做规则判断
     * @param deviceId
     */
    public void match(String deviceId,String adType,int width,int height){
        //取出标签
        //
        String tagJson = redis.getAsync(deviceId);
//        String tagJson = (String)objArr[0];
//        String ipResult = (String)objArr[1];
        TagBean tagBean = (TagBean)JsonTools.fromJson(tagJson);

        //开始匹配
        String creativeKey = adType + width + height;
        List<String> auidList = rtbIns.getCreativeMap().get(creativeKey);

        //开始遍历符合广告素材尺寸的广告
        for(String adUid : auidList){
            AdBean ad = rtbIns.getAdMap().get(adUid);

            AudienceBean audience = ad.getAudience();

            if(audience.getType().equals("location")){//地理位置
                if(audience.getGeos().equals("")){
                    //todo 省市县的匹配
                    List<AreaBean> areaList = new ArrayList<>();
                    for(AreaBean area:areaList){

                    }

                }else{//todo 按照经纬度匹配
                    List<GpsBean> geoList = new ArrayList<>();
                    for(GpsBean gps : geoList){

                    }
                }
            }else if(audience.getType().equals("demographic")){ //特定人群

            }else if(audience.getType().equals("company")){ // 具体公司

            }
            boolean isAvaliable = rtbIns.checkAvalable(adUid);
            //是否投当前的广告
            if(!isAvaliable)
                continue;
            //判断标签是否匹配
//            bean.getTaskBean().getCreativeList().

            //匹配
        }
    }

    /**
     * 对匹配的广告按照规则进行排序
     */
    public void order(){

    }




}
