package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    ImpressionExt
* @Author:         houkp
* @CreateDate:     2018/8/3 12:06
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/3 12:06
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class ImpressionExt  implements Serializable{
    private  Integer showtype;//	展示类型,灵集对广告展示形式的一种分类,具体见附录B
    private  Integer has_winnotice;// 该字段表示客户端是否支持发送winnotice(nurl字段)以及支持曝光的条数：1表示会发送winnotice并且支持多条曝光,0表示不会发送winnotice并且只支持一条曝光.
    private String required;//灵集imp
    private  Integer has_clickthrough;   // 该字段表示是否支持异步点击监测(cm),以及dsp点击监测是否需要302跳转到落地页。
    // if has_winnotice=0,都不支持异步的点击监测(cm),只支持ldp字段。
    //has_clickthrough=1表示ldp字段dsp返回点击监测url必须302 redirect到广告落地页
    //has_clickthrough=0表示ldp字段只返回dsp点击监测url，不用302 redirect到落地页

    private  Integer action_type;//媒体资源位置支持的交互类型：1.支持网页打开类+下载类广告 2.只支持打开类广告 3.只支持下载类广告
}
