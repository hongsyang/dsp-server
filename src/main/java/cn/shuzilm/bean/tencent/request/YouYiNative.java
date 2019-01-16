package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    Native 对象信息
* @Author:         houkp
* @CreateDate:     2018/11/22 21:14
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:14
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiNative implements Serializable {

   private Integer  native_id ;//  否 int32
   private Boolean  is_deep_link ;//  否 bool
   private Boolean  native_use_dimession_filter ;//  否 bool
   private List<YouYiAsset> assets ;//  否 ASSET
   private Integer  width ;//  否 int32
   private Integer  height ;//  否
}
