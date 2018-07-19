package cn.shuzilm.backend.rtb;

import java.util.Random;

/**
 * Created by thunders on 2018/7/17.
 */
public class RuleMatching {
    /**
     * 随机数控制开启标签顺序，雨露均沾
     */
    private Random tagRandom;
    /**
     * 随机数控制广告进度等顺序，雨露均沾
     */
    private Random adRandom;

    public void init(){
        tagRandom = new Random();
        adRandom = new Random();
        //获得广告价格、素材、广告主资金余额、
        //TODO
        //加载标签溢价比例
        //TODO
    }
    /**
     * 将设备ID 的标签从加速层取出，并做规则判断
     * @param deviceId
     */
    public void match(String deviceId){
        //取出标签
//TODO
        //
    }

    /**
     * 对匹配的广告按照规则进行排序
     */
    public void order(){

    }




}
