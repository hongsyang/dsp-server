package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    文本内容需求。
* @Author:         houkp
* @CreateDate:     2018/7/19 17:20
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 17:20
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class NativeRequestTitle implements Serializable{
    private Integer len;//最大文本长度，0不限制
    private RequestExt ext;//(F) 扩展内容
}
