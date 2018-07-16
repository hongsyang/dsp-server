package cn.shuzilm.bean.adview.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    SeatBid  0 或多个 SeatBid 对象,表示备选的广告
* @Author:         houkp
* @CreateDate:     2018/7/11 19:19
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/11 19:19
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class SeatBid implements Serializable {
    private List<Bid> bid;//(T) DSP 给的竞价广告对象
    private String seat;//(F) SeatBid 的标识,由 DSP 生成
    private ResponseExt ext;//(F) 扩展对象
}
