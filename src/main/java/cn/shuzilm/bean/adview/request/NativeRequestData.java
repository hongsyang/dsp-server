package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    数据内容需求。
* @Author:         houkp
* @CreateDate:     2018/7/19 17:21
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 17:21
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class NativeRequestData implements Serializable {
    private Integer type;//(T)需要的数据类型
    private Integer len;//(F)返回的内容的最大文本长度。缺省认为没有限制
    private RequestExt ext;//(F) 扩展内容


    private String label;//格式化文本串名称。
    private String value;//格式化文本内容，可以包括修饰字符，比如"$10"
}
