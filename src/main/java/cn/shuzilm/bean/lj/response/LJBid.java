package cn.shuzilm.bean.lj.response;

import cn.shuzilm.bean.adview.request.NativeBean;
import cn.shuzilm.bean.adview.response.Bid;
import cn.shuzilm.bean.adview.response.ResponseExt;
import cn.shuzilm.bean.adview.response.ResponseVideo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
* @Description:    LJBid
* @Author:         houkp
* @CreateDate:     2018/7/11 20:05
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/11 20:05
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class LJBid implements Serializable {

    private String impid;//(T) 对应 Impression 的唯一标识
    private String dealid;//(F) 参考请求里的 deal.id，判断是否投标属于私有交易
    private Integer price;//(T) CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，则取值为 6000。 //灵集DSP出价，注意单位是分/千次曝光，即CPM
    private Integer paymode;//(F) DSP 计费方式（可根据不同广告分别设置）1- CPM2- CPC【缺省为 1，竞价模式目前只支持 1】
    private Integer adct;//(T) 广告点击行为类型，参考附录 9
    private String bundle;//(F/ adct=2 时必须) 下载类广告必填，安卓下载类广告填写被下载 APP 的包名。iOS 下载类广告填写被下载 APP的 itunes id。
    private String adid;//(T) 广告 ID
    private String cid;//(T) 广告创意 ID，可用于去重
    private String adLogo;//(F) 广告来源标识，只对图片广告生效，html 广告需广告主自己封装在 html 里面。
    private Integer admt;//(T) 广告类型，参考附录 3
    private Integer adw;//(F/admt=1|2|3|4) 广告物料宽度
    private Integer adh;//(F/admt=1|2|3|4) 广告物料高度
    private Integer[] api;//(F) 如果可以，标记广告 API 框架。参考附录 10

    //admt=1|2|3 时，需要填充以下字段：
 /*   图文广告，
    adt 和 ads 至
    少填写一个。
    图文广告带图
    标时，adi 必
    填。
*/
    private String adi;//(F) 图片物料 URL
    private String adt;//(F) 文本物料标题
    private String ads;//(F) 文本物料副标题或描述，多个以换行分隔


    //admt=4 时，需要填充以下字段：
//    private String adm;//(T) 广告物料数据。填充 HTML 代码段。


    //admt=6 时，需要填充以下字段：
    private ResponseVideo video;//(F) Video 响应对象。

    //admt=8 时，需要填充以下字段：
    private NativeBean nativeBean; //根据 RTB 原生广告协议组装成 NativeResponse。参考《AdView 移动交易平台 RTB原生广告协议 V1.2》。


    //广告上报与落地页等相关字段：【视频广告的播放汇报字段在 Video 对象中,见 5.5 章节】
    private String wurl;//(F)赢价通知，由 AdView 服务器发出
//    private Map<Integer, List<String>> nurl;//(F) 带延迟的展示汇报，由客户端发送。形如：{"0":["URL1", "URL2"],"10":["URL3", "URL4"]}
    private List<String> curl;//(F)点击监控地址，客户端逐个发送通知
    private String adurl;//(T) 广告点击跳转落地页，可以支持重定向
    private String deeplink;//(F) 包含 deeplink 的点击跳转地址，无法打开则使用 adurl

    //其他字段：
    private Integer adtm;//(F) 广告需要播放的时长，主要针对插屏和开屏
    private String ade;//(F) 广告过期时间，格式：YYYYmmdd-HHMM 20140819-1230
    private String[] adomain;//(F) 广告主域名
    private String crid;//(F)广告物料 ID
    private Integer[] attr;//(F)广告属性，参考附录 4
    private ResponseExt ext;//(F) 扩展对象
    private String id;//灵集需要的参数  //DSP对该次出价分配的ID
    private String adm;// 广告物料URL。如果是动态创意，这个字段存放的是创意的HTML标签，标签中支持三种宏替换，%%CLICK_URL_ESC%%（encode的Exchange的点击监测地址）、%%CLICK_URL_UNESC%%(未encode的Exchange点击监测地址)和%%WINNING_PRICE%%（竞价最终价格）。
    private String nurl;//
}
