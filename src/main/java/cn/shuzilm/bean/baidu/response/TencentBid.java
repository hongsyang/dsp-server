package cn.shuzilm.bean.baidu.response;

import cn.shuzilm.bean.tencent.response.TencentDynamicCreative;
import lombok.Data;

import java.io.Serializable;

@Data
public class TencentBid implements Serializable {

    private String creative_id;// 是 小于 128 字节 DSP 侧的素材 id。
    private Integer bid_price;//是 DSP 的 CPM 出价。单位： 分。
    private String click_param;// 否 移动联盟流量需保证做完宏替换的点击跳转地址 base64 encode 之后
    private String impression_param;//否 impression_param 小于500 字节，整体 url 做完
    private String winnotice_param;//否  替换 Win Notice 地址中的__WINNOTICE_PARAM__宏
    private TencentDynamicCreative dynamic_creative;//否  若回复的广告是动态创意，需在该结构中填写相应的值

    private Boolean app_filter;//否 是否需要 Tencent Ad Exchange 进行已安装 App 过滤的标识， 默认为 false。若 DSP 需要针对该次回复的广告做已安装 App 过滤， 此字段的值为 true； 否则为 false。目前仅支持安卓用户的已安装过滤
    private String deal_id;//否  若该字段与 request 中的 deal_ids 匹配，且返回的 bid::creaive_id 对应的广告主 id 是合同中约定的广告主 id，表示参加 PD 竞价；若该字段与请求deal_ids 不一致，广告会被过滤；若为空，表示参加普通竞价

}
