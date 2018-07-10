package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    媒体站点对象
* @Author:         houkp
* @CreateDate:     2018/7/10 19:08
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 19:08
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class Site implements Serializable {
    private  String name;//媒体网站名称
    private  String page;//当前页面URL
    private  String ref;//referrer URL
    private  LJContent content;//视频的内容相关信息。只有视频贴片类型的广告位才会有这个字段，参见site.content对象描述
    private  String[] cat;//广告位内容分类，兼容IAB分类，符合openRTB 2.2表格6.1的分类方法。

}
