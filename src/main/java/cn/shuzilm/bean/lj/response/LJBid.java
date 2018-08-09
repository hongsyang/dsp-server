package cn.shuzilm.bean.lj.response;

import cn.shuzilm.bean.adview.request.NativeBean;
import cn.shuzilm.bean.adview.response.Bid;
import cn.shuzilm.bean.adview.response.ResponseExt;
import cn.shuzilm.bean.adview.response.ResponseVideo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
* @Description:    LJBid
* @Author:         houkp
* @CreateDate:     2018/7/11 20:05
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/11 20:05
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class LJBid implements Serializable {
    private String id;//灵集需要的参数  //DSP对该次出价分配的ID
    private String impid;//(T) 对应 Impression 的唯一标识
    private Float price;//DSP出价，单位是分/千次曝光，即CPM
    private String nurl;//win notice url,处理和曝光监测一样，nurl是否支持发送 参见
    private String adm;// 广告物料URL。如果是动态创意，这个字段存放的是创意的HTML标签，标签中支持三种宏替换，%%CLICK_URL_ESC%%（encode的Exchange的点击监测地址）、%%CLICK_URL_UNESC%%(未encode的Exchange点击监测地址)和%%WINNING_PRICE%%（竞价最终价格）。
    private String crid;//DSP系统中的创意ID，对于后审核的创意(即动态创意)，这个字段可以留作历史查证。
    private String dealid;//(F) 参考请求里的 deal.id，判断是否投标属于私有交易 	Dsp参加的deal的id
    private String cid;//投放活动id
    private ResponseExt ext;//(F) 扩展对象
}
