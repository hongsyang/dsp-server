package cn.shuzilm.bean.baidu.response;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:     **** 竞价广告信息 ****
* @Author:         houkp
* @CreateDate:     2019/3/13 20:18
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/13 20:18
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
@Data
public class BaiduAd implements Serializable {
    // 广告位顺序ID
    private Integer sequence_id ;// 1;
    // 创意ID
    // 对于静态创意（dsp将创意上传到BES, BES进行广告渲染），
    // creative_id唯一标识DSP上传到BES的每个创意（目前仅支持一个创意）。
    // 对于动态创意，creative_id唯一标识html snippet。BES会对动态创
    // 意的物料、监测地址及landing page等进行审核。DSP应保证含有相同物料、监测地址及landing page
    // 的html_snippet的snippet id相同，避免重复审核。但当html snippet中的物料、监测地址及landing page
    // 发生改变时，需要生成新的creative_id。
    // 对于关键词，creative_id唯一标识关键词。
    // 对于非BES托管的原生广告，creative_id唯一标识该原生广告
    private Long creative_id ;// 2;
    // html_snippet字段仅在返回动态创意时使用。DSP将拼装完成的
    // 创意及其物料拼装到html中，返回给BES。
    // BES经过判断，认为*拥有html_snippet字段的响应*属于动态创意，
    // 并通过此类字段获取必须的物料信息。相应数据中如遗漏字段，则
    // BES不能保证其参与竞价。
    //
    // 如何获知点击信息:
    // html snippet代码。该字段中需要填充click url的位置应填充
    // 宏%%CLICK_URL_{N}%%（这里{N}从0开始，应使用具体的序号代替，
    // 并与target_url中的顺序一致），并将click url填写至
    // target_url字段。BES会根据DSP的target_url
    // 构建最终click url之后，用其替换该宏。
    // 如下html中包含两个创意，则需要注册两个宏%%CLICK_URL_0%%和
    // %%CLICK_URL_1%%。并在target_url字段中顺序赋值。
    // std::String html("<BODY>...<a href;//"%%CLICK_URL_0%%"/>.."
    //  "<a href;//"%%CLICK_URL_1%%"/>...</BODY>"
    // ad.set_html_snippet(html);
    // ad.add_target_url("http://click.dsp.com?idea;//ad1...");
    // ad.add_target_url("http://click.dsp.com?idea;//ad2...");
    // 宏的错误（如顺序、遗漏等）或者target_url的赋值错
    // 误都会导致BES对target_url填充出错。
    //
    // 如何获知竞价后的计价信息:
    // 如DSP需要获知竞价成功后创意的cpm，可在期望的monitor_url
    // 字段特定位置添加宏%%PRICE%%。BES通过替换会使用cpm替换该宏。
    // 例:
    // http://wins.dsp.com?key1;//val1&&cpm;//%%PRICE%%...
    // 仅动态创意需要填充
    private String html_snippet ;// 7
    // 广告主id。动态创意及关键词需要填充
    // 非BES托管的原生广告需要填充
    // 动态创意要求一个html snippet的所有广告属于同一个广告主。
    private Long advertiser_id ;// 8;
    // 物料尺寸 - 宽度。需与请求中的尺寸一致
    // 仅动态创意需要填充
    private Integer  width ;// 9;
    // 物料尺寸 - 高度。需与请求中的尺寸一致
    // 仅动态创意需要填充
    private Integer  height ;// 10;
    // 创意所属行业的行业id。本字段的意义与静态创意中入库物料所需
    // 行业id相同。
    // 动态创意和关键词需要填充
    // 非BES托管的原生广告需要填充
    private Integer  category ;// 11;
    // 创意的物料类型
    // 动态创意和关键词需要填充
    // 非BES托管的原生广告需要填充
    private Integer  type ;// 12;
    // 创意的landing page。要求所有创意的landing page拥有相同的域，
    // 同时landing page应为target_url的最后一次跳转。
    // 注意: 这里仅填landing page的domain信息即可。如:
    // http://landing_page.advertiser.com/example.php?param1;//...
    // 如上url的landing page应填入landing_page.advertiser.com。
    // 动态创意和关键词需要填充
    // 非BES托管的原生广告需要填充
    private String landing_page ;// 13;
    // 创意的click url。响应中含有多个创意的情况下，每个创意click
    // url的顺序应与创意在html snippet中的顺序一致。BES将顺序进行
    // click url的替换。
    // 如该顺序不正确，将引发点击的统计偏差。
    // 动态创意和关键词需要填充
    // 非BES托管的原生广告需要填充
    private String target_url ;// 14;
    // 曝光监测。
    // 关键词需要填充
    // 非BES托管的原生广告需要填充
    private String monitor_urls ;// 17;// ;
    // 最高竞价，单位分
    private Integer max_cpm ;// 3;
    // 扩展参数
    private String extdata ;// 5;
    // 替换宏 %%EXT1%%
    private String ext1 ;// 23;
    // 替换宏 %%EXT2%%
    private String ext2 ;// 24;
    // 替换宏 %%EXT3%%
    private String ext3 ;// 25;

    // 是否进行cookie mapping
    private Boolean is_cookie_matching ;// 6;
    // 如果采用优先交易，该交易的ID
    private String preferred_order_id ;// 15;
    // 如果采用包断投放，投放的id
    private Integer guaranteed_order_id ;// 20;

    // 竞价链接单元广告
    private  BaiduLinkUnitKeyword  link_unit_keyword;


    //扩展创意信息

    private BaiduExpandCreativeInfo expand_creative_info ;// 19;

    // APP 唤醒信息
    // 目前只有交互类型为应用唤醒时需要填写该字段
    private BaiduDeeplinkInfo deeplink_info ;// 21;


    //信息流
    private BaiduNativeAd native_ad ;// 22;




}
