package cn.shuzilm.bean.internalflow;

import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.util.HttpClientUtil;
import cn.shuzilm.util.HttpRequestUtil;
import lombok.Data;
import sun.net.www.http.HttpClient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

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
    private List<Impression> impression;//广告位信息
    private String bidid;//DSP给出的该次竞价的ID
    private String seat;//SeatBid 的标识,由 DSP 生成
    private String adm;//广告材料数据
    private Integer adw;//(F/admt=1|2|3|4) 广告物料宽度
    private Integer adh;//(F/admt=1|2|3|4) 广告物料高度
    private String crid;//广告物料 ID
    private Integer admt;//广告类型
    private Integer adct;//广告点击行为类型，参考附录 9
    private String infoId;//上报信息的唯一ID
    private String did;//数盟的设备id
    private String deviceId;//   唯一识别用户
    private long createTime;// timestamp  该条信息的创建时间
    private String adUid;// varchar(36)  广告ID
    private String audienceuid;//  varchar(36)  人群ID
    private String advertiserUid;//  varchar(36)  广告主ID
    private String agencyUid;//varchar(36)  代理商ID
    private String creativeUid;//varchar(36)  创意ID
    private String province;//varchar(20)  省
    private String city;//varchar(20)  市
    private Double actualPrice ;//成本价(张迁需要)
    private Double actualPricePremium ;//溢价（张迁需要）
    private Double biddingPrice  ;//广告主出价（张迁需要）
    private String adxId  ;//广告商id
    private String appId  ;//应用id
    private String appPackageName  ;//应用包名称
    private String appVersion  ;//应用版本
    private String dealid  ;//私有交易id，判断是否属于私有交易
    private Double premiumFactor;//溢价系数
    private Long winNoticeTime;//1533628505531 对账时间戳

}
