package cn.shuzilm.bean.control;

import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;

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
     * 素材质量打分逻辑 暂时失效
     */
    private int creativeQuality;

    /**
     * 广告资金剩余额度打分
     */
    private int moneyLeft;

    /**
     * 广告主打分  暂时失效
     */
    private int advertiserScore;
    /**
     * 点击率打分
     */
    private int ctrScore;

    public void setImpProcess(double cost){
        impProcess = (int)(cost * 100);
    }

    public void setCtrScore(double score){
        if(score >= 2.0){
            ctrScore = 100;
        }else if(score >= 1.8 && score < 2.0){
            ctrScore = 80;
        }else if(score >= 1.5 && score < 1.8){
            ctrScore = 60;
        }else{
            ctrScore = 40;
        }
    }


}
