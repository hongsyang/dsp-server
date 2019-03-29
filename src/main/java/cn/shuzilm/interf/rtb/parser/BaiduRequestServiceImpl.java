package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.baidu.request.*;
import cn.shuzilm.bean.baidu.response.BaiduAd;
import cn.shuzilm.bean.baidu.response.BaiduBidResponse;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.tencent.request.*;
import cn.shuzilm.bean.tencent.response.TencentBidResponse;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.filter.FilterRule;
import cn.shuzilm.util.HttpClientUtil;
import cn.shuzilm.util.IpBlacklistUtil;
import cn.shuzilm.util.WidthAndHeightListUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;


/**
 * @Description: BaiduParser 百度post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:37
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:37
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class BaiduRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(BaiduRequestServiceImpl.class);


    private static final String FILTER_CONFIG = "filter.properties";

    private static final String ADX_NAME = "Baidu";

    private static final String ADX_ID = "5";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();

    private static WidthAndHeightListUtil widthAndHeightListUtil = WidthAndHeightListUtil.getInstance();

    private static RuleMatching ruleMatching = RuleMatching.getInstance();

    private ExecutorService executor = null;

    @Override
    public String parseRequest(String dataStr, ExecutorService executor) throws Exception {
        this.executor = executor;

        String response = "";
        if (StringUtils.isNotBlank(dataStr)) {
            MDC.put("sift", "dsp-server");
//            //请求报文解析
            log.debug(" BidRequest参数入参：{}", dataStr);

            BaiduBidRequest bidRequestBean = JSON.parseObject(dataStr, BaiduBidRequest.class);
//            //创建返回结果  bidRequest请求参数保持不变
            log.debug(" baiduBidRequest：{}", bidRequestBean);


 /*           BaiduMobile mobile = bidRequestBean.getMobile();//设备信息
            BaiduAdSlot baiduAdSlot = bidRequestBean.getAdslot().get(0);//曝光信息
            BaiduUserGeoInfo user_geo_info = bidRequestBean.getUser_geo_info();//用户信息
            BadiduMobileApp mobile_app = new BadiduMobileApp();

            String id = bidRequestBean.getId();//请求id
            String userIp = bidRequestBean.getIp();//用户ip
            String tagid = "";
            if (baiduAdSlot.getAd_block_key() != null) {
                tagid = String.valueOf(baiduAdSlot.getAd_block_key());//广告位id
            }
            Integer width = -1;//广告位的宽
            Integer height = -1;//广告位的高

            if (baiduAdSlot!=null){
                width=baiduAdSlot.getActual_width();//广告位实际宽度
                height=baiduAdSlot.getActual_height();// 广告位实际高度
            }
            String deviceId = null;//设备号
            String appPackageName = "";
            if (mobile != null) {
                if ("ANDROID".equals(mobile.getPlatform())){
                    if (mobile.getId()!=null){
                        List<BaiduID> baiduIDS = mobile.getId();
                        for (BaiduID baiduID : baiduIDS) {
                            if ("IMEI".equals(baiduID.getType())){
                                deviceId=baiduID.getId();
                            }
                        }
                    }
                }else  if ("IOS".equals(mobile.getPlatform())) {
                    if (mobile.getFor_advertising_id()!=null){
                        List<BadiduForAdvertisingId> for_advertising_idList = mobile.getFor_advertising_id();
                        for (BadiduForAdvertisingId for_advertising_id : for_advertising_idList) {
                            if ("IDFA".equals(for_advertising_id.getType())){
                                deviceId=for_advertising_id.getId();
                            }
                        }
                    }
                }



                mobile_app = mobile.getMobile_app();//app 信息
                if (mobile_app!=null){
                    appPackageName=mobile_app.getApp_bundle_id();
                }
            }

            List adxNameList = new ArrayList();//广告位列表
            adxNameList.add(ADX_ID + "_" + tagid);//添加广告位id
            log.debug("adxNameList:{}", adxNameList);

            //是否匹配长宽
            Boolean isDimension = true;



//            //是否匹配长宽
            Map msg = FilterRule.filterRuleBidRequest(deviceId, appPackageName, userIp, ADX_ID, adxNameList, width, height);//过滤规则的返回结果

            //ip黑名单和 设备黑名单，媒体黑名单 内直接返回
            if (msg.get("ipBlackList") != null) {
                return "ipBlackList";
            } else if (msg.get("bundleBlackList") != null) {
                return "bundleBlackList";
            } else if (msg.get("deviceIdBlackList") != null) {
                return "deviceIdBlackList";
            } else if (msg.get("AdTagBlackList") != null) {
                return "AdTagBlackList";
            }



            //广告匹配规则
            DUFlowBean targetDuFlowBean = ruleMatching.match(
                    deviceId,//设备mac的MD5
                    null,//广告类型
                    width,//广告位的宽
                    height,//广告位的高
                    false,// 是否要求分辨率
                    0,//宽误差值
                    0,// 高误差值;
                    ADX_ID,//ADX 服务商ID
                    null,//文件扩展名
                    userIp,//用户ip
                    appPackageName,//APP包名
                    adxNameList,//广告位列表
                    isDimension,
                    id,//请求id
                    tagid//广告位id
            );
            if (targetDuFlowBean == null) {
                response = "204baidu_id:"+id;
                return response;
            }
//            需要添加到Phoenix中的数据
            targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
            //曝光id
            List<Impression> list = new ArrayList();
            Impression impression = new Impression();
            impression.setId(tagid);
            list.add(impression);
            targetDuFlowBean.setImpression(list);//广告位id
            targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
            targetDuFlowBean.setAdxId(ADX_ID);//ADX广告商id
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String format = LocalDateTime.now().format(formatter);//时间戳
            targetDuFlowBean.setBidid(format+ UUID.randomUUID().toString().substring(0,21));//bid id  时间戳+随机数不去重
            targetDuFlowBean.setDspid(format + UUID.randomUUID());//dsp id
            targetDuFlowBean.setAppPackageName(appPackageName);//APP包名
            log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
*/

            //测试使用
            DUFlowBean targetDuFlowBean = new DUFlowBean();
            BaiduBidResponse bidResponseBean = convertBidResponse(targetDuFlowBean, bidRequestBean);
            response = JSON.toJSONString(bidResponseBean);
            MDC.put("sift", "dsp-server");
            log.debug("bidResponseBean:{}", response);

            bidRequestBean = null;
            targetDuFlowBean = null;
            return response;
        } else {
            return response;
        }

    }

    /**
     * 内部流转DUFlowBean  转换为  BidResponseBean 输出给 ADX服务器
     *
     * @param targetDuFlowBean
     * @param bidRequestBean
     * @return baiduBidResponse
     */
    private BaiduBidResponse convertBidResponse(DUFlowBean targetDuFlowBean, BaiduBidRequest bidRequestBean) {
        log.debug("进入转换为BidResponseBean ，请求id位:{}", bidRequestBean.getId());

        BaiduBidResponse baiduBidResponse = new BaiduBidResponse();
        baiduBidResponse.setId(bidRequestBean.getId());
        //广告信息
//        List ads = new ArrayList();
        //广告信息
        BaiduAd baiduAd = new BaiduAd();
        //当前页面广告位顺序 id，同一页面从 1 开始
        baiduAd.setSequence_id(bidRequestBean.getAdslot().get(0).getSequence_id());
        baiduAd.setCreative_id(97464805728L);
        baiduAd.setHtml_snippet("http://rtb.shuzijz.cn");
        baiduAd.setWidth(bidRequestBean.getAdslot().get(0).getActual_width());
        baiduAd.setHeight(bidRequestBean.getAdslot().get(0).getActual_height());
        baiduAd.setCategory(7001);
        baiduAd.setType(1);
        baiduAd.setLanding_page("http://rtb.shuzijz.cn");
        baiduAd.setTarget_url("http://rtb.shuzijz.cn");
        baiduAd.setPreferred_order_id("3");
        baiduAd.setExtdata("test");
        baiduAd.setAdvertiser_id(654321L);
        baiduAd.setMax_cpm(100000);
//        baiduAd.setMonitor_urls("http://rtb.shuzijz.cn");
//        ads.add(baiduAd);
        baiduBidResponse.setAd(baiduAd);


//        TencentSeatBid tencentSeatBid = new TencentSeatBid();
//        baiduBidResponse.setAds();
//        TencentBidResponse.setRequest_id(bidRequestBean.getId());
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
//        String format = LocalDateTime.now().format(formatter);//时间戳

//        //曝光信息
//        TencentImpressions tencentImpressions = bidRequestBean.getImpressions().get(0);
//        //曝光id
//        tencentSeatBid.setImpression_id(tencentImpressions.getId());
//        //腾讯 Bid 类型
//        List tencentBidList = new ArrayList();
//        TencentBid tencentBid = new TencentBid();
//
//        double biddingPrice = targetDuFlowBean.getBiddingPrice() * 100;//广告出价
//        tencentBid.setBid_price((int) biddingPrice);
//        tencentBid.setCreative_id(targetDuFlowBean.getCrid());//推审id
//        //曝光通知Nurl
//        String wurl = "id=" + targetDuFlowBean.getRequestId() +
//                "&bidid=" + targetDuFlowBean.getBidid() +
//                "&impid=" + tencentImpressions.getId() +
//                "&act=" + format +
//                "&adx=" + targetDuFlowBean.getAdxId() +
//                "&did=" + targetDuFlowBean.getDid() +
//                "&device=" + targetDuFlowBean.getDeviceId() +
////                "&app=" + URLEncoder.encode(targetDuFlowBean.getAppName()) +
//                "&appn=" + targetDuFlowBean.getAppPackageName() +
////                "&appv=" + targetDuFlowBean.getAppVersion() +
//                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
//                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
//                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
//                "&dpro=" + targetDuFlowBean.getProvince() +// 省
//                "&dcit=" + targetDuFlowBean.getCity() +// 市
//                "&dcou=" + targetDuFlowBean.getCountry() +// 县
//                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
//                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
//                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
////                "&pmp=" + targetDuFlowBean.getDealid() + //私有交易
//                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
////        tencentBid.setWinnotice_param(wurl);//赢价通知，按此收费
//        //曝光通知Nurl
//        String nurl = "id=" + targetDuFlowBean.getRequestId() +
//                "&bidid=" + targetDuFlowBean.getBidid() +
//                "&impid=" + tencentImpressions.getId() +
//                "&act=" + format +
//                "&adx=" + targetDuFlowBean.getAdxId() +
////                "&did=" + targetDuFlowBean.getDid() +
//                "&device=" + targetDuFlowBean.getDeviceId() +
////                "&app=" + URLEncoder.encode(targetDuFlowBean.getAppName()) +
//                "&appn=" + targetDuFlowBean.getAppPackageName() +
////                "&appv=" + targetDuFlowBean.getAppVersion() +
//                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
//                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
//                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
//                "&dpro=" + targetDuFlowBean.getProvince() +// 省
//                "&dcit=" + targetDuFlowBean.getCity() +// 市
//                "&dcou=" + targetDuFlowBean.getCountry() +// 县
//                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
//                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
//                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
////                "&pmp=" + targetDuFlowBean.getDealid() + //私有交易
//                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
//        tencentBid.setImpression_param(nurl);//曝光通知
//        String curl = "id=" + targetDuFlowBean.getRequestId() +
//                "&bidid=" + targetDuFlowBean.getBidid() +
//                "&impid=" + tencentImpressions.getId() +
//                "&act=" + format +
//                "&adx=" + targetDuFlowBean.getAdxId() +
////                "&did=" + targetDuFlowBean.getDid() +
//                "&device=" + targetDuFlowBean.getDeviceId() +
////                "&app=" + URLEncoder.encode(targetDuFlowBean.getAppName()) +
//                "&appn=" + targetDuFlowBean.getAppPackageName() +
////                "&appv=" + targetDuFlowBean.getAppVersion() +
//                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
//                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
//                "&dpro=" + targetDuFlowBean.getProvince() +// 省
//                "&dcit=" + targetDuFlowBean.getCity() +// 市
//                "&dcou=" + targetDuFlowBean.getCountry() +// 县
//                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
//                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
//                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
////                "&pmp=" + targetDuFlowBean.getDealid() + //私有交易
//                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
//        tencentBid.setClick_param(curl);//点击通知
//        //腾讯 Bid 类型列表
//        tencentBidList.add(tencentBid);
//        tencentSeatBid.setBids(tencentBidList);
//        //腾讯  seat_bids类型列表
//        ads.add(tencentSeatBid);
//        TencentBidResponse.setSeat_bids(ads);
        MDC.put("sift", "baidubidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(baiduBidResponse));
        return baiduBidResponse;

    }


}

