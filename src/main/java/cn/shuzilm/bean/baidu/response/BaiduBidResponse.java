package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * @Description:    百度返回参数
 * @Author:         houkp
 * @CreateDate:     2019/3/13 20:11
 * @UpdateUser:     houkp
 * @UpdateDate:     2019/3/13 20:11
 * @UpdateRemark:   修改内容
 * @Version:        1.0
 */
@Data
public class BaiduBidResponse implements Serializable {


    // 返回ID，将请求中的id赋值给返回id，便于session trace
    private String id ;// 1;

    // **** 竞价广告信息 ****
    private BaiduAd ad;//

    // **** 系统使用 ****
    // debug接口
    private String debug_String ;// 3;
    // DSP处理时间
    private Integer processing_time_ms ;// 4;


}
