package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    Impression 需要广告的描述
* @Author:         houkp
* @CreateDate:     2018/7/10 15:48
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 15:48
* @UpdateRemark:   是否必传  T:true  F:false
* @Version:        1.0
*/
@Data
public class Impression implements Serializable {
    private String id;// (T) 唯一标识，由 AdView 生成
    private Banner banner;//(F)  横幅或插屏广告信息描述
    private NativeBean nativeBean;//(F) 原生广告信息描述
    private Video video;//(F) 视频广告信息描述
    private Pmp pmp;//(F) 私有交易
    private Integer instl;//(T) 0- 横幅广告 1- 插屏或全插屏广告 4- 开屏广告 5- 视频广告 6- 原生广告
    private String tagid;// (F/T)  instl=5 或 6 时（T）  广告位 id
    private Integer bidfloor;//(F/T)  at=0 或 1时(T)  CPM 底价，数值为 CPM 价格*10000，如底价为 0.6 元，则该字段值为 6000   实例6000
    private String bidfloorcur;//(F)  价格单位，取值可能是: USD、RMB，缺省 RMB
    private Integer secure;//(F) 标识曝光是否需要安全的 HTTPS URL 创意素材和标记，其中 0 =不安全，1 =安全。如果省略，安全状态是未知的，但非安全 HTTP 可以假设是支持的。缺省为 0。
    private RequestExt ext;// 扩展内容
}
