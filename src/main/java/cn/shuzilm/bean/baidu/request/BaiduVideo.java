package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 视频类型
 * @Author: houkp
 * @CreateDate: 2019/3/13 17:51
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 17:51
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BaiduVideo  implements Serializable{

    // 视频的标题
    private String title;// 1;
    // 视频的标签
    private String tags;// 2;
    // 视频的播放时长
    private Integer content_length;// 3;
    //频道信息
    private Double channel_id;// 4;

}
