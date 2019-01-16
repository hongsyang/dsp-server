package cn.shuzilm.bean.tencent.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class YouYiAd implements Serializable{

    private String adz_id ;//是 string adx request adz_id
    private Integer adz_array_id;//是 uint32  广告主
    private Integer bid_price ;//是 uint32bid price, unit cent/CPM
    private Boolean dealid ;//是 bool pmp 订单必填 参与 pmp 流量报价的 dealid
    private String advertiser_id;//是 string dsp 返回的 yoyi 广告主 id
    private Integer creative_id ;//是 uint64dsp 返回的 yoyi 创意 id
    private String para ;//否 string ext paramter __PARA__ macro value
    private String win_para ;//否 string ext paramter __WIN_PARA__ macro value
    private String imp_para ;//否 string ext paramter __IMP_PARA__ macro value
    private String clk_para ;//否 string ext paramter __CLK_PARA__ macro value
    private String trk_para ;//否 string ext paramter __TRK_PARA__ macro value
}
