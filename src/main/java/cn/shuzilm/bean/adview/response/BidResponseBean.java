package cn.shuzilm.bean.adview.response;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:    BidResponseBean  数据响应结果
* @Author:         houkp
* @CreateDate:     2018/7/10 19:27
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/10 19:27
* @UpdateRemark:   是否必传  T:true  F:false
* @Version:        1.0
*/
@Data
public class BidResponseBean  implements Serializable{
    private String id;//(T)返回 BidRequest 的唯一标识
    private SeatBid[] seatBid;//(F) 0 或多个 SeatBid 对象,表示备选的广告    目前仅支持一个
    private String bidid;//(F) BidResponse 的唯一标识,由 DSP生成  【目前 AdView 不读取这个字段】
    private String cur;//(F)  价格单位，取值: USD,RMB，缺省RMB   RMB
    private Integer nbr;//(建议) 不出价原因，参考附录 11   7
    private ResponseExt ext;//(F) 扩展对象

}
