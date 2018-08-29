package cn.shuzilm.bean.adview.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    ResponseLink
* @Author:         houkp
* @CreateDate:     2018/8/24 10:44
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/24 10:44
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class ResponseLink implements Serializable {
    private String  url;//点击跳转URL地址
    private List<String> clicktrackers;//第三方点击监控地址，必须在点击目标地址前触发。
    private String  fallback;//替代落地页地址，如果设备无法访问url地址，使用本地址（可以用作deepLink的备选URL）。
    private ResponseExt  ext;
}
