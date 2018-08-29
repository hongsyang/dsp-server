package cn.shuzilm.bean.adview.response;

import cn.shuzilm.bean.adview.request.Assets;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    NativeResponseBean 原生类
* @Author:         houkp
* @CreateDate:     2018/8/24 10:40
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/24 10:40
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class NativeResponseBean implements Serializable {
    private String ver;// 1
    private String assetsid;
    private List<Assets> assets;
    private ResponseLink link;
    private List<String> imptrackers;
    private ResponseExt ext;

}
