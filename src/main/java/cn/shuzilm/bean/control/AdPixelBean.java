package cn.shuzilm.bean.control;

import lombok.Data;

/**
 * 从 PIXEL 服务器获得的 win notice 和 点击请求
 * <p>
 * Created by thunders on 2018/7/11.
 */
@Data
public class AdPixelBean implements ICommand {
    /**
     * type = 0 : 曝光
     * type = 1 : 点击
     */
    private int type;
    private String host;//根据配置文件获取
    private String adUid;//广告id
    private String adName;//可以留空
    private Long winNoticeTime;//1533628505531 时间戳
    /**
     * 成本价
     */
    private Double cost;
    /**
     * 实际耗费的量
     */
    private long winNoticeNums;
    /**
     * 点击时间戳
     */
    private long clickTime;
    /**
     * 点击次数
     */
    private long clickNums;

    /**
     * 最终消耗价
     */
    private Double finalCost;
    
    /**
     * 溢价系数
     */
    private Double premiumFactor;

}
