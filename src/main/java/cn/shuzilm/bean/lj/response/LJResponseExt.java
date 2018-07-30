package cn.shuzilm.bean.lj.response;

import cn.shuzilm.bean.adview.response.ResponseExt;
import cn.shuzilm.bean.lj.request.LJEvent;
import lombok.Data;

import java.util.List;

/**
 * @Description: LJResponseExt  扩展对象
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:07
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:07
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJResponseExt extends ResponseExt {
    private String ldp;//点击目标URL。广告点击后会跳转到物料上绑定的landingpage，还是取实时返回的ldp，参见投放说明
    private String deeplinkurl;//应用直达URL，当返回了deeplinkurl，优先唤醒本地app，如果无法唤醒，则调用ldp(打开或者下载)
    private List pm;//曝光监测URL，监测数组支持的曝光条数和广告展现时是否会发物料上绑定的monitor地址，参见投放说明
    private List cm;//点击监测URL，监测数组支持的点击监测条数，参见投放说明
    private Integer action_type;//媒体资源位置支持的交互类型：1.download---下载类广告 2.landingpage---打开落地页型广告
    private String type;//物料的类型，包括png，gif，jpg，swf，flv，c和x。具体参见Exchange物料类型  注意：c和x类型物料必须指定类型，其他物料可以不用指定这个字段。
    private String imp_ext1;//曝光宏值，替换动态内容，该宏对搜狐(sourceid=3)和腾讯(sourceid=7)的Bidresponse返回有效；宏定义是单个宏字段，必须像数组一样按照顺序使用
    private String imp_ext2;//曝光宏值，替换动态内容，该宏对搜狐(sourceid=3)和腾讯(sourceid=7)的Bidresponse返回有效；宏定义是单个宏字段，必须像数组一样按照顺序使用
    private String imp_ext3;//曝光宏值，替换动态内容，该宏对搜狐(sourceid=3)和腾讯(sourceid=7)的Bidresponse返回有效；宏定义是单个宏字段，必须像数组一样按照顺序使用
    private String imp_ext4;//曝光宏值，替换动态内容，该宏对搜狐(sourceid=3)和腾讯(sourceid=7)的Bidresponse返回有效；宏定义是单个宏字段，必须像数组一样按照顺序使用
    private String clk_ext1;//点击宏值，该宏对搜狐(sourceid=3)和腾讯(sourceid=7)的Bidresponse返回有效；宏定义是单个宏字段，必须像数组一样按照顺序使用
    private String clk_ext2;//	点击宏值，该宏对搜狐(sourceid=3)和腾讯(sourceid=7)的Bidresponse返回有效；宏定义是单个宏字段，必须像数组一样按照顺序使用
    private LJEvent event; //描述 监测事件或监测触发时间点。目前只对视频贴片广告有效。event object 可以有多个；目前支持event监测事件的视频媒体：优土、爱奇艺、乐视；其他视频媒体暂不支持

}
