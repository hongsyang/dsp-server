package cn.shuzilm.bean.control;

import lombok.Data;
import lombok.Setter;

/**
 * Created by thunders on 2018/8/8.
 * 广告属性
 */
@Data
public class AdPropertyBean implements ICommand {

    /**
     * 广告投放进度
     */
    private int impProcess;

    /**
     * 素材质量打分
     */
    private int creativeQuality;

    /**
     * 广告资金剩余打分
     */
    private int moneyLeft;

    /**
     * 广告主打分
     */
    private int advertiserScore;
    /**
     * 点击率打分
     */
    private int ctrScore;


}
