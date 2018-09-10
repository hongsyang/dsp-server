package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: LJNativeVideo 视频素材
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJNativeVideo implements Serializable {
    private String[] mimes;//允许的视频格式，例：["flv","mp4"]
    private Integer minduration;//	允许最小视频时长秒数
    private Integer maxduration;//	允许最大视频时长秒数
    private Integer w;//	允许视频宽度
    private Integer h;//	允许视频高度

    private String  url;//视频素材地址
    private String  cover_img_url;//视频信息流cover覆盖地址 尺寸要求和视频素材的尺寸一致
    private Integer  duration;//视频时长
}
