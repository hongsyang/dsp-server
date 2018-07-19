package cn.shuzilm.bean.adview.request;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: BidRequest Bean
 * @Author: houkp
 * @CreateDate: 2018/7/10 11:32
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 11:32
 * @UpdateRemark: 是否必传  T:true  F:false
 * @Version: 1.0
 */
@Data
public class BidRequestBean implements Serializable{
    private String id;//(T)   广告请求唯一标识
    private List<Impression> imp;// (T)   曝光对象，一次request可以包含多个imp  ( Array of Impression) 需要广告的描述
    private App app;//  (T)  应用对象（内含移动应用的信息）
    private Device device;//  (T)设备信息
    private User user;// (F)用户信息 (F)
    private Integer at;//  (T)竞拍类型：0 – 最高价格成交(参与竞价)  1 – 以次高价格成交（参与竞价） 2 – 优先购买(不参与竞价) (T)
    private Integer tmax;//  (F)  超时时间，单位毫秒  300
    private String[] weat;//(F)  广告主缩略名称数组，只接受这些广告主 ["4"]
    private String[] wcid ;//(F) 广告创意数组，代表本次请求只接受这些创意 [“001”]
    private String[] cur ;//(F) 允许结算的货币，包括以下： [“USD”,“RMB”]
    private String[] bcat ;//(F) 广告创意数组，代表本次请求只接受这些创意 ["001]
    private String[] badv ;//(F) 拒绝接受的广告主名单，采用域名标示广告主
    private String callback ;//(F) IOS 的激活回调 url，仅对下载IOS 类广告有效。
    private Regs regs ;//(T) 必须（美国流量）
    private Site site;//	媒体站点对象 （暂不支持，灵集系统支持）
    private RequestExt ext;//扩展字段，参看request.ext字段说明
}

