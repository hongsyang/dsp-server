package cn.shuzilm.bean.youyi.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    视频信息
* @Author:         houkp
* @CreateDate:     2018/11/22 21:09
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:09
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiVideo  implements Serializable {

  private String   title ;//  否 string 视频标题
  private Integer   duration ;//  否 int32 视频时长
  private List<String> keywords ;//  否string 关联的关键字
  private List<String>   video_format ;//  否VideoFormat
  private Integer   video_start_delay;//  否 int32  贴片位置相对于所在视频的起始时间， 0 表示前贴片, -1 表示后贴片，大于 0 的值  表示中插
  private Integer   min_ad_duration ;//  否 int32 贴片最小播放时间长度,视频创意播放时 间不可小于该值，单位秒
  private Integer   max_ad_duration ;//  否 int32 贴片最大播放时间长度，单位秒
  private String   protocol ;//  否 string    vast 协议版本,取值为: // VAST 1.0 / VAST 2.0 / VAST 3.0
  private String   program_id ;//  否 string 节目 id
  private String   channel_id ;//  否 string 频道 id, 以英文逗号分隔
}
