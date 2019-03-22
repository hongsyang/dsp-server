package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    java类作用描述
* @Author:         houkp
* @CreateDate:     2019/3/13 20:27
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/13 20:27
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduLinkUnitKeyword implements Serializable {

    // 竞价链接单元广告的关键词, utf-8编码，dsp需要为每个竞价的关键词设置如下信息
    // creative_id: 创意id
    // max_cpm: 关键词报价，单位分
    // selected_style_type: 关键词竞价的样式
    // advertiser_id: 广告主id
    // category: 关键词所属行业
    // landing_page: 着陆页
    // target_url: 点击串
    // monitor_urls: 展示监测
    // type: 创意的物料类型
    private String keyword ;// 1;
    // 关键词竞价的样式
    // dsp为关键词选择的样式id,可以从请求中的候选样式中选择多个
    // 最终的样式是由bes考量多重因素从候选样式中选择的最优样式进行展现
    // 为空表示可以适用于request中的所有样式,如果dsp对样式无特别要求
    // 建议为空,dsp不设置该字段,可以提高胜出率
    private List<Integer> selected_style_type ;// 2;
}
