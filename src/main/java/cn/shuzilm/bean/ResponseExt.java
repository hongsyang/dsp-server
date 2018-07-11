package cn.shuzilm.bean;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    ResponseExt 扩展对象
* @Author:         houkp
* @CreateDate:     2018/7/11 19:18
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/11 19:18
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class ResponseExt implements Serializable {
    private String preimgurl;//(F) 视频播放未开始时，显示该图片

    //二者填充一个
    private String endhtml;//(F) 视频播放完成后，加载该html 片段
    private String endimgurl;//(F) 视频播放完成后，展示该图片


}
