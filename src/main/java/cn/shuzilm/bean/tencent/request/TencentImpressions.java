package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentImpressions  implements Serializable {

    private String id;//是 Bid Request 范围内，Impression 唯一标识
    private Long placement_id;//是  ADX 提供的广告位 id
    private List<Integer> creative_specs;//是   广告位支持的素材规格，取值见《Tencent AdExchange 广告样式说明》
    private Integer bid_floor;//是 true 广告位的 CPM 底价。 单位：分
    private List<Long> blocking_industry_id;//否 广告位过滤的行业 ID 列表，取值见《Tencent AdExchange 广告主行业分类》 。
    private List<TencentNative> natives;//否 原生广告位信息
    private List<String> multimedia_type_white_list;//否 广告位支持的素材类型。取值范围为"gif", "jpeg",
    private List<String> blocking_keyword;//否 广告位过滤的关键字
    private List<String> advertiser_whitelist;//否 广告位的广告主白名单。
    private List<String> advertiser_blacklist;//否 广告位过滤的广告主
    private List<String> pretargeting_ids;//否 请求命中的 Pretargeting配置包 ID
    private List<String> deal_ids;//否 PMP 投放线下约定的合同号，当请求符合合同约定则为对应的合同号
    private List<TencentProductType> product_types;//否广告位支持的商品类型列表。
}
