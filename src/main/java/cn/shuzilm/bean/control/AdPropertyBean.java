package cn.shuzilm.bean.control;

import lombok.Data;
import lombok.Setter;
import org.geotools.metadata.ModifiableMetadata;

import java.math.BigDecimal;

/**
 * Created by thunders on 2018/8/8.
 * 广告属性
 */
@Data
public class AdPropertyBean implements ICommand {

	private static final long serialVersionUID = 2547420604167434540L;
    /**
     * 广告投放进度
     */
    private int impProcess = 1;

    /**
     * 素材质量打分逻辑 暂时失效
     */
    private int creativeQuality = 1;

    /**
     * 广告资金剩余额度打分
     */
    private int moneyLeft = 1;

    /**
     * 广告主打分  暂时失效
     */
    private int advertiserScore = 1;
    /**
     * 点击率打分
     */
    private int ctrScore = 1;

    public static void main(String[] args) {
        AdPropertyBean bean  = new AdPropertyBean();
        bean.setMoneyLeft(45000);
        System.out.println(45000 +" "+ bean.getMoneyLeft());
        bean.setMoneyLeft(15000);
        System.out.println(15000 + " " +bean.getMoneyLeft());
        bean.setMoneyLeft(3000);
        System.out.println(3000 + " " + bean.getMoneyLeft());
        bean.setMoneyLeft(1000);
        System.out.println(1000 + " " +bean.getMoneyLeft());
    }

    /**
     * 剩余资金打分
     * @param _moneyLeft
     */
    public void setMoneyLeft(double _moneyLeft){
        if(_moneyLeft >= 50000){
            moneyLeft = 100;
        }else if(_moneyLeft >= 20000 ){
            moneyLeft = 80;
        }else if(_moneyLeft >= 5000){
            moneyLeft = 60;
        }else if(_moneyLeft >= 2000){
            moneyLeft = 40;
        }else {
            moneyLeft = 20;
        }
    }
    
    /**
     * 剩余资金打分
     * @param _moneyLeft
     */
    public void setMoneyLeft(float _moneyLeft){
    	 if(_moneyLeft >= 50000){
             moneyLeft = 100;
         }else if(_moneyLeft >= 20000 ){
             moneyLeft = 80;
         }else if(_moneyLeft >= 5000){
             moneyLeft = 60;
         }else if(_moneyLeft >= 2000){
             moneyLeft = 40;
         }else if(_moneyLeft >= 100){
             moneyLeft = 20;
         }else{
        	 moneyLeft = 10;
         }
    }

    public void setImpProcess(double cost){
        //避免分数为 0 造成对总计算结果的影响
        if(cost == 0)
            cost = 0.01;
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
