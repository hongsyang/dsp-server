package cn.shuzilm.bean.internalflow;

import cn.shuzilm.bean.adview.request.Impression;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: DUFlowBean 数盟流转Bean
 * @Author: houkp
 * @CreateDate: 2018/7/13 11:56
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/13 11:56
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class DUFlowBean implements Serializable {

    private static final long serialVersionUID = -6743567631108323096L;

    private String requestId;//对应Bid Request中的id

    private String adTypeId;//广告大类型ID
    private String adxAdTypeId;//广告小类对应ADX服务商的ID
    private String widthHeightRatio;//  Width/Height 宽高比
    private String platform;//不同平台 dsp传
    private String demographicTagId;//不同人群  dsp传 用逗号隔开
    private Integer hour;// winNotice返回的小时数 （ 统计需求 价格随时段变化 ）

    private List<Impression> impression;//广告位信息
    private String bidid;//DSP给出的该次竞价的ID
    private String dspid;//DSP对该次出价分配的ID
    private String seat;//SeatBid 的标识,由 DSP 生成
    private String adm;//广告材料数据
    private Integer adw;//(F/admt=1|2|3|4) 广告物料宽度
    private Integer adh;//(F/admt=1|2|3|4) 广告物料高度
    private String crid;//广告物料 ID
    private String admt;//广告类型
    private Integer adct;//广告点击行为类型，参考附录 9
    private String infoId;//上报信息的唯一ID
    private String did;//数盟的设备id
    private String deviceId;//   唯一识别用户
    private Long createTime;// timestamp  该条信息的创建时间
    private String adUid;// varchar(36)  广告ID
    private String audienceuid;//  varchar(36)  人群ID
    private String advertiserUid;//  varchar(36)  广告主ID
    private String agencyUid;//varchar(36)  代理商ID
    private String creativeUid;//varchar(36)  创意ID
    private String province;//varchar(20)  省
    private String city;//varchar(20)  市
    private Double actualPrice;//成本价(张迁需要)
    private Double premiumFactor;//溢价系数
    private Double actualPricePremium;//溢价（张迁需要）
    private Double biddingPrice;//广告主出价（张迁需要）
    private String adxId;//广告商id
    private String appId;//应用id
    private String appPackageName;//应用包名称（统计需求  媒体）
    private String appVersion;//应用版本
    private String dealid;//私有交易id，判断是否属于私有交易
    private Long winNoticeTime;//1533628505531 对账时间戳
    private String landingUrl;//点击广告之后的跳转url
    private String linkUrl;//点击目标链接
    private String tracking;//曝光监测链接
    private String adxSource;//ADX服务商渠道


}
