package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    用户信息
* @Author:         houkp
* @CreateDate:     2018/11/22 21:02
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:02
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiUser  implements Serializable {
  private String  user_exid ;//  否 string  exchange cookie id, 无线时使用  device_id
  private String  user_yyid ;//  否 string 悠易 id。进行 cookie mapping 后得到的 id, 无线时使用 device_id
  private String  user_ip ;//  否 string ipv4:10.0.2.12
  private String  adx_crowd_tags ;//  否 string exchange tag
  private String  user_agent ;//  否 string 用户浏览器
  private String  user_gender ;//  否 GenderType
  private String  user_crowd_tags ;//  否 string 人群标签。 adx 标签映射成悠易内部 的标签，多个标签之间用逗号分隔开
  private String  baidu_user_id ;//  否 string 当百度 mobile 流量时， device id 写进   user_exid， baidu_user_id 则写进该字 段
  private String  baidu_user_id_yoyi_cookie;//  否 string  当百度 mobile 流量时， device id 写进 user_yyid， baidu_user_id_yoyi_cookie 则写进该字段
  private String  baidu_tag_id ;//  否 string 百度用户分类信息
  private String  boss_qq ;//  否 string 广点通流量中标识广告主 Boss_qq
  private String  user_area ;//  否 string
  private Integer  user_age ;//  否 uint64 用户年龄，按照 YOYI 的标签体系传 递值： 1~8，表示： <18,18-24,25-         29,30-34,35-39,40-44,45-50,50+
  private String  adx_cookie ;//  否 string exchange cookie
  private String  yoyi_cookie ;//  否 string yoyi cookie, pc 或 mob web 流量有值
  private Integer  user_yyid_type ;//  否 uint32 user_yyid 类型 * 1: yoyi cookie * 2: IDFA 原文  * 3: imei md5 * 4: android id md5 * 5: mac md5 * 6: exchange user id
}
