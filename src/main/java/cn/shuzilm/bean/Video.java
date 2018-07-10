package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: Video  视频广告信息描述
 * @Author: houkp
 * @CreateDate: 2018/7/10 16:01
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/10 16:01
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class Video implements Serializable {
    private String[] mimes;//(T) 支持播放的视频格式，目前支持： video/x-flv，application/xshockwave-flash、video/mp4  video/mp4
    private Integer[] protocols;//(F) 所支持的视频广告投放协议 ，参考附录 12，默认是 3
    private String linearity;//(F) 广告展现样式，1-线性，2-非线性，为空表示不限制。
    private Integer sequence;//(F) 曝光所处的位序。比如贴片的话就表明该曝光是第几贴。
    private Integer[] battr;//(F) 拒绝的广告创意属性，参考附录4
    private Integer minbitrate;//(F) 视频最小 kb
    private Integer maxbitrate;//(F) 视频最大 kb
    private Integer minduration;//(F) 视频广告最短播放时长，单位是秒
    private Integer maxduration;//(F) 视频广告最长播放时长，单位是秒
    private Integer w;//(F) 广告位宽度
    private Integer h;//(F) 广告位高度
    private Integer[] api;//(F)  列出支持的广告 API
    private RequestExt requestExt;//（F）扩展内容
}
