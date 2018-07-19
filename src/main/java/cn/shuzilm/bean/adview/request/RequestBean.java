package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    RequestBean  根据 RTB 原生广告协议组装成NativeRequest。
* @Author:         houkp
* @CreateDate:     2018/7/10 16:19
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 16:19
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class RequestBean implements Serializable{
    private String  ver;//(T)使用原生广告的协议版本。
    private Integer  layout;//(T)原生广告的版式ID,参考附录1
    private Integer  adunit;//(T)原生广告单元ID
    private Integer  plcmtcnt;//(F)同一版式布局中，相同广告位的个数。
    private Integer  seq;//(F)原生广告单元的序号，0表示第一个广告，1表示第2个广告，依次类推。
    private String  styleDesc;//描述
    private String  style;//新增的不知道有什么用
    private List<Assets> assets;//(T)描述都需要哪些素材
    private RequestExt ext;//(F) 扩展内容
}
