package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    Video 响应对象。
* @Author:         houkp
* @CreateDate:     2018/7/11 19:39
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/11 19:39
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class ResponseVideo implements Serializable{
    private Integer  xmltype;//(T)  返回格式类型1- VAST XML    2- 元素组合

    //xmltype=1 时，需要填充以下字段：
    private String  vastxml;//(T)   视频 VAST3.0 xml 代码

    //xmltype=2 时，需要填充以下字段：
    private String  videourl;//(T) 视频广告素材地址 URL
    private String  iconurl;//(F) icon 地址 UR
    private String  title;//(F) 视频广告标题
    private String  desc;//(F) 视频广告描述
    private Integer  duration;//(T) 视频播放时长，单位秒
    private Integer  width;//(T)  视频宽
    private Integer  height;//(T) 视频高
    private String[]  sptrackers;//(F) start_play_trackers，视频开始播放时，客户端会逐个上报
    private String[]  mptrackers;//(F) midpoint_play_trackers，视频播放 1/2 时，客户端会逐个上报
    private String[]  cptrackers;//(F) complete_play_trackers，视频播放完成时，客户端会逐个上报
    private String  errurl;//(F) 视频广告无法播放或播放错误时，客户端会上报
    private ResponseExt ext;//(F) 扩展对象
}
