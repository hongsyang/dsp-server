package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentBidRequest implements Serializable {

    private String id;//是 小于 128 字节 ADX 提供的 BidRequest 唯一标识
    private Boolean is_ping;//否 true 表示探测网络延迟，不触发竞价逻辑,DSP 必须支持此特性。
    private Boolean is_test;//否 true 表示测试请求，竞价成功的广告不会被展示和计费。 DSP 必须支持此特性。
    private List<TencentImpressions> impressions;//否 每个impression代表一个广告位的请求。
    private TencentDevice device;//否 设备信息，相关政策参
    private String ip;//否 设备联网ip。
    private Integer area_code;//否 部分流量上可能没有有效IP，此时用地域码做定向。
    private TencentUser user;//否 用户信息，仅对部分DSP 开放
    private TencentGeo geo;//否 地理位置信息。
    private TencentApp app;//否 设备联网ip。
    private Integer boss_qq;//否 设备联网ip。
    private Boolean support_deep_link;//否 广告位是;//否支持 deep link，默认为不支持。
    private List<Integer> wx_flow_class;//否 微信公众号分类 ID 信息,暂无启用
}
