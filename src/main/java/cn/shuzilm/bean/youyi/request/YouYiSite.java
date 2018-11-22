package cn.shuzilm.bean.youyi.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    网站对象
* @Author:         houkp
* @CreateDate:     2018/11/22 20:54
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 20:54
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiSite  implements Serializable {

  private String  url ;//否 string
  private String  refer ;//否 string
  private String  category ;//否 string 站点分类
  private String  content_category ;//  repeated 重复 string 页面分类
  private String  inapp_app_category ;//  repeated 重复 string
  private String  sensitive_category ;//  repeated 重复 string 媒体禁止的敏感类目
  private String  excluded_product_category ;//  repeated 重复 string 媒体禁止的广告行业类目
}
