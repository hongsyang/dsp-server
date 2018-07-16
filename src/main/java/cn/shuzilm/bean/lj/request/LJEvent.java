package cn.shuzilm.bean.lj.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 描述 监测事件或监测触发时间点。
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class LJEvent implements Serializable {
    private Float t;//描述 监测事件节点或监测触发时间点。0-start、0.25-firstQuartile、0.5-midpoint、0.75-thirdQuartile、1.0-complete这5个监测事件。若填入大于1的整数，则表示监测触发时间(s)。目前只对视频贴片广告有效，仅支持返回监测事件。
    private String[] tm;//描述 曝光监测url，指定监测事件或时间点发送的一组监测地址
}
