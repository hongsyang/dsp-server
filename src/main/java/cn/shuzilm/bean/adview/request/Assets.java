package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: NativeRequest    Assets描述都需要哪些素材。
 * @Author: houkp
 * @CreateDate: 2018/7/19 17:12
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 17:12
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class Assets implements Serializable {
    private Integer id;//(T)不重复的素材id，AdView设置。一般是数组中该元素的计数。
    private Integer required;//(F)设置为1表示素材必需，此时如果返回的广告缺少对应素材则AdView认为无效。缺省为0
    private NativeRequestTitle title;//(F)文本内容需求
    private NativeRequestImage img;//(F)图片内容需求。
    private NativeRequestVideo video;//(F)视频内容需求。
    private NativeRequestData data;//(F)数据内容需求。
    private RequestExt ext;//(F) 扩展内容

}
