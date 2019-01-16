package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    ott对象
* @Author:         houkp
* @CreateDate:     2018/11/22 21:20
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:20
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiOTT implements Serializable{

  private List<Boolean> is_pre_load;//  否 bool true： OTT 预加载请求广告位； false： OTT 真实的 价请求
  private List<Integer>  pre_adid ;//  否int32预存在 OTT 的订单 ID
}
