package cn.shuzilm.bean.youyi.request;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    私有竞价
* @Author:         houkp
* @CreateDate:     2018/11/22 21:21
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:21
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiPmp implements Serializable {

   private String   deal_id ;//  否 string
   private String   deal_type ;//  否 DealType
   private Integer   deal_price ;//  否 int32
   private Integer   return_rate ;//  否 int32
   private Boolean   dis_enable ;//  否 bool 是;//  否支持分发下游 dsp

    
}
