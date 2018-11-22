package cn.shuzilm.bean.youyi.request;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    广告位信息
* @Author:         houkp
* @CreateDate:     2018/11/22 21:09
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:09
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiAdzone implements Serializable {
  private String  pid ;//  否 string 广告位 id
  private String  adz_id ;//  否 string 广告位序列 Id
  private String  adz_type ;//  否 string
  private String  view_type ;//  否 string
  private Integer  adz_width ;//  否 int32 adzone size
  private Integer  adz_height ;//  否 int32 adzone size
  private Integer  adz_ad_count ;//  否 int32 请求的 广告数
  private Integer  adz_position ;//  否 int32  广告位位置： 1,首屏； 0,  非首屏
  private String  reserve_price ;//  否 string 最低竞标价格，货币单位  为人民币分，数值含义为 分/千次展现
  private Integer  exclude_landing_page_url;//  否int32 禁止的落地页
  private Integer  creative_specs ;//  否int32 广点通 creative_specs 标识流量来源的位置
  private String  seat_ids ;//  否string 灵集流量中增加竞价席位
  private YouYiVideo  video ;//  否 Video
  private YouYiNative  Native ;//  否 NATIVE
  private YouYiPmp   pmp ;//  否 PMP
  private String  is_deep_link ;//  否 bool
  private String  billing_id ;//  否string
  private String  mimes ;//  否string 素材类型
  private YouYiOTT  ott ;//  否OTT
  private String  imp_date ;//  否 string   预加载广告位用 请求曝光  的日期（闪屏用）格式   YYYY-MM-DD HH:mm:ss
  private String  preload_adz_type ;//  否 PreloadAdzFormat
  private String  sub_media ;//  否 string 标识子媒体

}
