package cn.shuzilm.bean.lj.response;

import cn.shuzilm.bean.lj.request.LJAssets;
import cn.shuzilm.bean.lj.request.LJEvent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: NativeAD 信息流返回结果
 * @Author: houkp
 * @CreateDate: 2018/7/11 20:11
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/11 20:11
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class NativeAD implements Serializable {
    private List<LJAssets> assets; //原生广告的多个素材数组
    private LJLink link; //目标连接对象
    private List<String> imptrackers; //展示曝光URL数组
    private List<LJEvent> event; //描述 监测事件或监测触发时间点
}
