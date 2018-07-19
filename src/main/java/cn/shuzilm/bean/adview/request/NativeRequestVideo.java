package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    视频内容需求。
* @Author:         houkp
* @CreateDate:     2018/7/19 17:20
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 17:20
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class NativeRequestVideo implements Serializable{
    private Integer w;//(T)视频广告位宽
    private Integer h;//(T)视频广告位高
    private String[]  mimes;//(F）支持的视频格式。
    private Integer minduration;//(F)视频最短长度-秒数
    private Integer maxduration;//(F)视频最大长度-秒数
    private Integer[]  protocols;//(F)支持的视频广告投放协议，见《AdView移动广告交易平台RTB接口协议》附录6.12
    private RequestExt ext;//(F) 扩展内容
}
