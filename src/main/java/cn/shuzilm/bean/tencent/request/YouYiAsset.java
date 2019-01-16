package cn.shuzilm.bean.tencent.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    Asset 对象
* @Author:         houkp
* @CreateDate:     2018/11/22 21:16
* @UpdateUser:     houkp
* @UpdateDate:     2018/11/22 21:16
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class YouYiAsset implements Serializable {

  private Integer  asset_id ;//  否 int32
  private String  asset_type;//  否 string title, shareTitle, longTitle, summary, description,  boardCustName, buttonText video, sharePicture, dynamicPicture, littleSizePicture, icon, logo, picture1, picture2...
  private Integer  width ;//  否 int32
  private Integer  height ;//  否 int32

}
