package cn.shuzilm.bean.baidu.request;


import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * @Description:    BidRequest 请求体
 * @Author:         houkp
 * @CreateDate:     2019/3/13 17:47
 * @UpdateUser:     houkp
 * @UpdateDate:     2019/3/13 17:47
 * @UpdateRemark:   修改内容
 * @Version:        1.0
 */
@Data
public class BaiduBidRequest implements Serializable {

    // 请求ID，唯一标识本次请求，明文字符串
    private String id;// 1;

    // **** 用户信息 ****
    // 用户IP地址，点分十进制字符串
    private String ip;// 2;
    // User-Agent
    private String user_agent;// 3;

    // 用户分类
    private List<Integer> user_category;// 6;


    // 用户ID
    // 在2015年4月30日之前将会同时发送版本1和版本2的cookie
    // 从2015年5月1日起只发送版本2的cookie
    private List<BaiduIdList>  baidu_id_list;

    // **** 页面信息 ****
    // 当前页面URL
    private String url;// 11;
    // 请求的refer
    private String referer;// 12;
    // 网站分类
    private Integer site_category;// 13;
    // 网站质量类型
    private Integer site_quality;// 14;

    // 页面类型
    private Integer page_type;// 15;
    // 页面关键词
    private List<String> page_keyword;// 17;
    // 页面内容质量
    private Integer page_quality;// 18;
    // 页面分类
    private Integer page_vertical;// 21;



    // 默认每次请求一个广告位
    private List<BaiduAdSlot> adslot;// 20;


    // DSP 托管到 BES的 USER ID
    private String buyer_user_id;// 32;



    // 自定义的用户标签
    private BaiduCustomizedUserTag customized_user_tag;// 31;
    // 用户性别
    private String gender;// 7;
    // 页面语言
    private String detected_language;// 9;

    // 发布商不允许的广告行业
    private List<Integer> excluded_product_category;// 19 [packed;//true];

    // flash版本
    private String flash_version;// 10;
    // **** 系统使用 ****
    private Boolean is_test;// 26 [default;//false];
    private Boolean is_ping;// 27 [default;//false];



    // 用户位置信息
    private BaiduUserGeoInfo user_geo_info;// 28;


    // 移动设备信息
    private  BaiduMobile  mobile;



//__________________________________________________________________________________________________
    //测试阶段字段不全，待定
    // 新版移动设备用户识别号字段，允许多种类型同时存在
    // 推荐使用本字段获取用户ID，原字段会逐步废弃。
//        message ForAdvertisingID {
//        enum IDType {
//            UNKNOWN;// 0;    // 未知
//            ANDROID_ID;// 4; // Android适用
//            IDFA;// 5;       // IOS适用
//        }
//        private IDType type;// 1; // ID类型
//        private String id;// 2; // ID内容
//
//        private ForAdvertisingID for_advertising_id;// 14;




//    // 视频流量的媒体信息
//    message Video
//
//    {
//        // 视频的标题
//        private String title;// 1;
//        // 视频的标签
//        private String tags;// 2;
//        // 视频的播放时长
//        private Integer content_length;// 3;
//        //频道信息
//        private int64 channel_id;// 4;
//    }
//
//    private Video video;// 30;
//
//
//
//
//
//
//
//
//
//
//        // 视频广告的最大时长。该字段将于2015年6月3日后停止使用，6月4日开始使用新的字段
//        private Integer max_video_duration;// 10;
//        // 视频广告的最小时长。该字段将于2015年6月3日后停止使用，6月4日开始使用新的字段
//        private Integer min_video_duration;// 11;
//        // 视频广告的播出延时， 0及正整数表示前贴，-1表示中贴，-2表示后贴。
//        // 该字段将于2015年6月3日后停止使用，6月4日开始使用新的字段
//        private Integer video_start_delay;// 12;
//
//        // 建议使用新的字段接入video
//        message VideoInfo {
//        // 视频广告的最大时长
//        private Integer max_video_duration;// 1;
//        // 视频广告的最小时长
//        private Integer min_video_duration;// 2;
//        // 视频广告的播出延时， 0及正整数表示前贴，-1表示中贴，-2表示后贴
//        private Integer video_start_delay;// 3;
//    }
//        private VideoInfo video_info;// 15;
//
//        message LinkUnitInfo {
//        // 链接单元广告候选样式
//        // 在request中，BES会计算在此次请求中最优的广告样式集合，并将此集合
//        // 发送给DSP，最后使用的样式将根据DSP返回的关键词组合进行确定。
//        private Integer style_type;// 1;
//        // 链接单元广告可以返回的关键词个数
//        private Integer req_keyword_num;// 2;
//        // BES推荐使用的关键词, dsp可选择使用，也可以使用自己认为更好的关键词, 使用utf-8编码
//        private String proposed_keyword;// 3;
//
//        // 替代proposed_keyword, 容纳词明文之外的其它字段。
//        message ProposedKeyword {
//            // BES推荐使用的关键词, dsp可选择使用，也可以使用自己认为更好的关键词, 使用utf-8编码
//            // 与原proposed_keyword字段一致，做为原字段的替代。
//            private String text;// 1;
//            // 推荐词的点击率预估，供DSP参考。单位十亿分之1。
//            private float pctr1;// 2;
//        }
//        private ProposedKeyword keywords;// 4;
//    }
//        private LinkUnitInfo link_unit_info;// 16;
//
//        // 优先交易信息
//        message PreferredOrderInfo {
//        message PreferredOrder {
//            // 优先交易ID
//            private String order_id;// 1;
//            // 该优先交易的交易价格
//            private int64 fixed_cpm;// 2;
//        }
//        // 一次请求可能包含多个优先交易
//        private PreferredOrder preferred_orders;// 1;
//        // 允许优先交易的同时，是否允许竞价
//        private bool allow_auction;// 2 [default;//true];
//    }
//        private PreferredOrderInfo preferred_order_info;// 13;
//
//        // 包断订单信息
//        message GuaranteedOrder {
//        // 订单id，全局唯一
//        private uInteger order_id;// 1;
//        message Charge {
//            // 订单价格
//            private Integer price;// 1;
//            // charging type
//            //广告计费模式
//            enum ChargeType {
//                CHARGE_GD_CPM;// 2;  // 包断CPM
//            }
//            private ChargeType charge_mode;// 2 [default ;// CHARGE_GD_CPM];
//        }
//        private Charge charge;// 2;  //计费类型和cpm
//    }
//        private GuaranteedOrder guaranteed_orders;// 20;
//
//        // 该广告位允许的扩展创意信息
//        message ExpandCreativeInfo {
//        // 扩展动作类型
//        enum ActionType {
//            // 悬停
//            HOVER;// 1;
//            // 点击
//            CLICK;// 2;
//            // 加载
//            LOAD;// 3;
//        }
//        private ActionType expand_action_type;// 1;
//        // 扩展方向
//        enum Direction {
//            EXPAND_NONE;// 0;
//            // 向上扩展
//            EXPAND_UP;// 1;
//            // 向下扩展
//            EXPAND_DOWN;// 2;
//            // 向左扩展
//            EXPAND_LEFT;// 3;
//            // 向右扩展
//            EXPAND_RIGHT;// 4;
//        } ;
//        private Direction expand_direction;// 2;
//        // 允许的创意类型
//        private Integer expand_creative_type;// 3 [packed;//true];
//        // 扩展后的广告位宽度
//        private Integer expand_width;// 4;
//        // 扩展后的广告位高度
//        private Integer expand_height;// 5;
//        // 扩展展现时长
//        private Integer expand_duration;// 6;
//    }
//        private ExpandCreativeInfo expand_creative_info;// 17;
//
//        // 广告位级别
//        enum AdSlotLevel {
//            UNKNOWN_ADB_LEVEL;// 0;
//            TOP;// 1;    // 优质广告位
//            MED;// 2;    // 中端广告位
//            TAIL;// 3;   // 长尾广告位
//            LOW;// 4;    // 低俗广告位
//        }
//        private AdSlotLevel adslot_level;// 21 [default;//UNKNOWN_ADB_LEVEL];
//        // 原生广告诉求参数
//        message NativeAdParam {
//        enum Fields {
//            // 标题
//            TITLE;// 0x1;
//            // 内容描述
//            DESC;// 0x2;
//            // 主题图
//            IMAGE;// 0x4;
//            // logo 图标
//            LOGOICON;// 0x8;
//            // 投放时下载的APP大小
//            APPSIZE;// 0x10;
//        }
//        message ImageEle {
//            // 宽
//            private Integer width;// 1;
//            // 高
//            private Integer height;// 2;
//            // 形状，
//            // 0没有形状要求，
//            // 1矩形，
//            // 2圆形，
//            // 3半圆形
//            private Integer shape;// 3 [default ;// 0];
//        }
//        // 按照位图设置相应的位为1，不需要请求的位保持0
//        // 示例：原生广告必须包含标题和图标，则required_fields ;// (0x1 | 0x8) ;// 9
//        private int64 required_fields;// 1;
//        // 标题最大长度
//        private Integer title_max_length;// 2;
//        // 描述最大长度
//        private Integer desc_max_length;// 3;
//        // 广告主logo或图标的宽高、形状要求
//        private ImageEle logo_icon;// 4;
//        // 主题图的宽高、形状要求
//        private ImageEle image;// 5;
//        // 主题图数量
//        private Integer image_num;// 6;
//    }
//        // 是否允许返回非原生广告创意，
//        // 如果为true，则对于存在原生诉求的情况下可以返回非原生广告创意，
//        // 如果为false，则必须返回原生创意
//        private bool allowed_non_nativead;// 23 [default;//true];
//        private NativeAdParam nativead_param;// 24;
//        // 是否为HTTPS请求
//        // 如果为true，则所有资源（图片、视频等）必须以HTTPS返回
//        // 注意：url字段对应协议与secure字段的值并无严格对应关系，
//        //       比如，存在url协议为HTTP而secure为true的情况，
//        //       因此，需要使用secure字段来决定是否以HTTPS返回资源，而不要依赖url字段
//        private bool secure;// 25 [default;//false];
//    }


}
