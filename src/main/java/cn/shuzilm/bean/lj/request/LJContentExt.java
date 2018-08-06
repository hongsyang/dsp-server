package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    LJContentExt
* @Author:         houkp
* @CreateDate:     2018/8/3 12:12
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/3 12:12
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class LJContentExt implements Serializable{
    private String channel;//视频的频道ID，例如"1"。频道id需要与sourceid配合使用。视频流量的频道字典详见
    private String cs;// 二级频道ID
    private Integer copyright;// 版权信息 0---版权信息未知 1---有版权
    private Integer quality;// 流量质量 1---流量质量保障 2---流量质量未知
}
