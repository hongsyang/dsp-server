package cn.shuzilm.bean.lj.response;

import cn.shuzilm.bean.adview.response.Bid;
import lombok.Data;

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
public class LJBid extends Bid {
    private String id;//灵集需要的参数  //DSP对该次出价分配的ID
    private String adm;// 广告物料URL。如果是动态创意，这个字段存放的是创意的HTML标签，标签中支持三种宏替换，%%CLICK_URL_ESC%%（encode的Exchange的点击监测地址）、%%CLICK_URL_UNESC%%(未encode的Exchange点击监测地址)和%%WINNING_PRICE%%（竞价最终价格）。
    private String nurl;//
}
