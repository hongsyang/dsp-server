package cn.shuzilm.bean.control;

import cn.shuzilm.bean.dmp.AudienceBean;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by thunders on 2018/7/10.
 */
@Getter
public class AdBean implements ICommand  {
    /**
     * 广告允许的最大拖欠额度，单位：元
     */
    @Setter
    private int moneyArrears;

    /**
     * 人群包列表，支持一个广告对应多个人群包
     */
    @Setter
    private List<AudienceBean> audienceList;
    /**
     * 曝光率<br>
     * wins / bids 的比值，是动态变化的，并由流量控制中心更新变化
     */
    @Setter
    private float exposureRate;

    /**
     * 该条广告的质量打分属性
     */
    @Setter
    private AdPropertyBean propertyBean;


    /**
     * 该广告所属的广告主
     */
    @Setter
    private AdvertiserBean advertiser;
    @Setter
    private String groupId;

    /**
     * 广告单元ID
     */
    @Setter
    private String adUid;
    /**
     * 广告名称
     */
    @Setter
    private String name;
    /**
     * 广告限额
     */
    @Setter
    private BigDecimal quotaAmount;

    /**
     * 广告起始时间
     */
    @Setter
    private Date startTime;
    /**
     * 广告结束时间
     */
    @Setter
    private Date endTime;

    /**
     * 该广告下创意列表
     */
    @Setter
    private List<CreativeBean> creativeList;

    /**
     * 出价模式（文本cpm/cpc）
     */
    @Setter
    private String mode;

    /**
     * 出价
     */
    @Setter
    private float price;
    /**
     * 广告排期时间（对应 二维数组：timeSchedulingArr）
     */
    @Setter
    private String scheduleTime;
    /**
     * 广告排期(对应 ad 数据表中的 time ，需要转换成 int[][]形式)
     */
    @Setter
    private int[][] timeSchedulingArr;

    /**
     * 0 加速投放 1 匀速投放
     */
    @Setter
    private int speedMode;

    /**
     * 广告优先级
     */
    @Setter
    private int priority;

    /**
     * CPM 每日限额
     */
    @Setter
    private long cpmDailyLimit;
    /**
     * CPM 每小时限额
     */
    @Setter
    private long cpmHourLimit;

    /**
     * 单用户每日频次限制
     */
    @Setter
    private int frqDaily;
    /**
     * 单用户每小时频次限制
     */
    @Setter
    private int frqHour;

    /**
     * 当前时间戳
     */
    @Setter
    private long timestamp;
    

}
