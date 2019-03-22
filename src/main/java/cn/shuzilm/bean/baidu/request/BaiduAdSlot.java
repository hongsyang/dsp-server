package cn.shuzilm.bean.baidu.request;


import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
* @Description:     **** 广告位信息 ****
* @Author:         houkp
* @CreateDate:     2019/3/13 17:46
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/13 17:46
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduAdSlot implements Serializable {
    // 广告位ID, 全局唯一id
    private Long ad_block_key;// 1;
    // 当前页面广告位顺序id，同一页面从1开始
    private Integer sequence_id;// 2;
    // 展示类型
    private Integer adslot_type;// 3;
    // 宽
    private Integer width;// 4;
    // 高
    private Integer height;// 5;
    // 广告位实际宽度
    private Integer actual_width;// 18;
    // 广告位实际高度
    private Integer actual_height;// 19;
    // 展示位置
    private Integer slot_visibility;// 6;
    // 发布商允许的创意类型    // 广告位需要的创意封装和渲染类型
    private List<Integer> creative_type;// 7 [packed;//true];       // 1 静态创意 // 2 动态创意

    // 发布商不允许的landing page url
    private List<String> excluded_landing_page_url;// 8;

    // 媒体保护设置信息的ID
    private Long publisher_settings_list_id;// 14;
    // 发布商设置的底价，单位分
    private Integer minimum_cpm;// 9;

}
