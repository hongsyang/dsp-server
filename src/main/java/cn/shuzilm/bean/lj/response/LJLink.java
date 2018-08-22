package cn.shuzilm.bean.lj.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: LJLink 不同素材用户行为记录的连接对象,当点击动作执行时调用。如果素材没有定义点击对象,使用上层的NativeAdResponse中的链接地址(目前XTrader平台暂时不处理该字段，不支持素材绑定自定义链接对象)
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJLink  implements Serializable {
    private String url;//	点击跳转URL地址(落地页)
    private String deeplinkurl;//应用直达URL，当返回了deeplinkurl，优先唤醒本地app，如果无法唤醒，则调用ldp(打开或者下载)搜狐、腾讯、美团、微博这些媒体投放统一使用物料上传时指定的deeplinkurl，其他媒体投放使用RTB接口实时返回的deeplinkurl
    private String intro_url;//Android应用下载介绍页面
    private List<String> clicktrackers;// 点击监播地址
    private Integer action;// 	信息流广告操作行为:1.download---下载类广告 2.landingpage---打开落地页型广告 详见
    private String downloadurl;//应用下载类广告下载url,应用下载广告必填

}
