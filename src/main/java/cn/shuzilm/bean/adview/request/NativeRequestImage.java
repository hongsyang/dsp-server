package cn.shuzilm.bean.adview.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    图片内容需求
* @Author:         houkp
* @CreateDate:     2018/7/19 17:20
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 17:20
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class NativeRequestImage implements Serializable {
    private Integer type;//(F)图片类型
    private Integer w;//(F)图片宽度
    private Integer wmin;//(F)最小宽度，如果只有w，认为需要确切匹配宽度
    private Integer h;//(F)图片高度
    private String[] mimes;//(F)额外支持的图片格式。默认支持["image/jpg","image/gif"]
    private RequestExt ext;//(F) 扩展内容
}
