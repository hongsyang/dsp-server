package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: LJAssets Asset Object主要定义了请求中原生广告可以包含的素材特定要求。
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJAssets implements Serializable {
    private Integer id;//	由Exchange平台定义的唯一不重复的asset ID(素材ID)，一般是数组中该元素的计数。	required
    private Boolean required;//	boolean	默认为False。设置为True表示该元素必填(Exchange会忽略缺少必填元素的BidResponse返回，视为无效返回)	optional
    private LJNativeTitle title;//	标题内容素材
    private LJNativeImg img;//	图片内容素材
    private LJNativeVideo video;//	视频内容素材
    private LJNativeData data;//	数据内容素材
    private LJNativeExt ext;// 扩展对象

}
