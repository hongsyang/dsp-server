package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    NativeBean  原生广告信息描述
* @Author:         houkp
* @CreateDate:     2018/7/10 16:00
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 16:00
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class NativeBean implements Serializable {
    private RequestBean request;//(T) 根据 RTB 原生广告协议组装成NativeRequest。参考《AdView移动交易平台 RTB 原生广告协议 V1.2》
    private String ver;//(建议使用) 原生广告协议版本。
    private Integer[] api;//(F) 列出支持的广告 API，参考附录10
    private Integer[] battr;//(F) 拒绝的广告创意属性，参考附录4
    private RequestExt ext;//(F) 扩展内容
}
