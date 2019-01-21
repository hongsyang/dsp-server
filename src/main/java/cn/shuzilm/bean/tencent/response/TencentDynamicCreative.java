package cn.shuzilm.bean.tencent.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TencentDynamicCreative  implements Serializable{

    private  String html_snippet;//  否 动态创意代码片段
    private  Boolean  support_gdt_helper_js;//  否动态创意代码片段是否内嵌了 TencentAd Exchange 提供的辅助 JS 代码
    private List<TencentProductInfo> product_infos;//  否  动态创意对应的商品信息
    private List<String> click_url_domain_whitelist;//  否  预留，暂未启用


}
