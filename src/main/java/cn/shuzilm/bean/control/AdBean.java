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
    //Apache Log Config
//日志版本||URL||参数||请求时间||IP地址||UID类型||UID值||模式MODE||ADX来源||广告位ID||广告ID||资源ID||创意ID||匹配标签||广告单价||附加参数||状态码||UserAgent/设备属性||受众/环境参数||Refer/bundle ID||服务器ID||响应时间（微秒）||返回字节数||排错信息
//(LoadBalancer) LogFormat "1||%U||%q||%{%Y%m%d%H%M%S}t||%{x-forwarded-for}i||%{rec}n||%{uid}n||%{mode}n||%{adx}n||%{slot}n||%{ad}n||%{resource}n||%{material}n||%{tag}n||%{price_enc}n||%{ext}n||%>s||%{User-Agent}i||-||%{Referer}i||%{server}n||%D||%B||%{debug}n" combined
//(Direct) LogFormat "1||%U||%q||%{%Y%m%d%H%M%S}t||%a||%{rec}n||%{uid}n||%{mode}n||%{adx}n||%{slot}n||%{ad}n||%{resource}n||%{material}n||%{tag}n||%{price_enc}n||%{ext}n||%>s||%{User-Agent}i||-||%{Referer}i||%{server}n||%D||%B||%{debug}n" combined


//    URL：包含请求的exchange来源等关键标识字段，REST风格。区别（impression和click在此部分）
//    参数：“?”后面的URL传参，包含变量宏，仅作原始记录
//    UID类型：包含IMEI/MAC/IDFA/ADID/Cookie UID，仅记录一种UID
//    广告位ID：ADX/SSP给出的广告位标识
//    广告单价：加密格式，需要根据来源ADX进行解密
//    Refer/Bundle ID：如果存在，直接记录。
//    ext附加参数：参数传递宏

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
    
    /**
     * 点击目标链接（含协议头）
     */
    @Setter
    private String link;
    
    /**
     * 落地页链接（含协议头）
     */
    @Setter
    private String landing;
    
    /**
     * 曝光监测链接（null为无曝光监测）
     */
    @Setter
    private String tracking;

}
