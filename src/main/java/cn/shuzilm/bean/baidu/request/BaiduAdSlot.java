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
    private Double ad_block_key;// 1;
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
    private List<Integer> creative_type;// 7 [packed;//true];

    // 视频广告的最小时长。该字段将于2015年6月3日后停止使用，6月4日开始使用新的字段
    private Integer min_video_duration;// 18;
    // 视频广告的最大时长。该字段将于2015年6月3日后停止使用，6月4日开始使用新的字段
    private Integer max_video_duration;// 18;

    //创意类型描述
    private List<String> creative_desc_type;// 7 [packed;//true];

    // 发布商不允许的landing page url
    private List<String> excluded_landing_page_url;// 8;

    // 媒体保护设置信息的ID
    private List<Long> publisher_settings_list_id;// 14;
    // 发布商设置的底价，单位分
    private Integer minimum_cpm;// 9;

    // 是否为HTTPS请求
    // 如果为true，则所有资源（图片、视频等）必须以HTTPS返回
    // 注意：url字段对应协议与secure字段的值并无严格对应关系，
    //       比如，存在url协议为HTTP而secure为true的情况，
    //       因此，需要使用secure字段来决定是否以HTTPS返回资源，而不要依赖url字段
    private  Boolean secure;

}
