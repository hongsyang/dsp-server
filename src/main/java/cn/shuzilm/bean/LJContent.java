package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;
/**
* @Description:    视频的内容相关信息
* @Author:         houkp
* @CreateDate:     2018/7/10 19:10
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 19:10
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class LJContent  implements Serializable{
    private String title;//视频标题名称
    private String keywords;//视频标签关键字，如果是多个关键字，则使用英文逗号分隔
    private RequestExt ext;//参见site.content.ext描述

}
