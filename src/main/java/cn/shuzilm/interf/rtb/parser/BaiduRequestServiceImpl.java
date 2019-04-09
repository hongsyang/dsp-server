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
import cn.shuzilm.util.*;
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


            BaiduMobile mobile = bidRequestBean.getMobile();//设备信息
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

            if (baiduAdSlot != null) {
                width = baiduAdSlot.getActual_width();//广告位实际宽度
                height = baiduAdSlot.getActual_height();// 广告位实际高度
            }
            String deviceId = null;//设备号
            String appPackageName = "";
            if (mobile != null) {
                if ("ANDROID".equals(mobile.getPlatform())) {
                    if (mobile.getId() != null) {
                        List<BaiduID> baiduIDS = mobile.getId();
                        for (BaiduID baiduID : baiduIDS) {
                            if ("IMEI".equals(baiduID.getType())) {
                                deviceId = baiduID.getId();
                            }
                        }
                    }
                } else if ("IOS".equals(mobile.getPlatform())) {
                    if (mobile.getFor_advertising_id() != null) {
                        List<BadiduForAdvertisingId> for_advertising_idList = mobile.getFor_advertising_id();
                        for (BadiduForAdvertisingId for_advertising_id : for_advertising_idList) {
                            if ("IDFA".equals(for_advertising_id.getType())) {
                                deviceId = for_advertising_id.getId();
                            }
                        }
                    }
                }


                mobile_app = mobile.getMobile_app();//app 信息
                if (mobile_app != null) {
                    appPackageName = mobile_app.getApp_bundle_id();
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
                response = "204baidu_id:" + id;
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
            targetDuFlowBean.setBidid(format + UUID.randomUUID().toString().substring(0, 21));//bid id  时间戳+随机数不去重
            targetDuFlowBean.setDspid(format + UUID.randomUUID());//dsp id
            targetDuFlowBean.setAppPackageName(appPackageName);//APP包名
            log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);

            //测试使用
//            DUFlowBean targetDuFlowBean = new DUFlowBean();
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);//时间戳

        BaiduBidResponse baiduBidResponse = new BaiduBidResponse();
        baiduBidResponse.setId(bidRequestBean.getId());
        //广告信息
        BaiduAd baiduAd = new BaiduAd();
        //当前页面广告位顺序 id，同一页面从 1 开始
        baiduAd.setSequence_id(bidRequestBean.getAdslot().get(0).getSequence_id());
        baiduAd.setCreative_id(Long.valueOf(targetDuFlowBean.getCrid()));//推审id   ---- 314136722806L
        baiduAd.setWidth(bidRequestBean.getAdslot().get(0).getActual_width());
        baiduAd.setHeight(bidRequestBean.getAdslot().get(0).getActual_height());
        baiduAd.setCategory(targetDuFlowBean.getTradeId());//行业id ----------7605
        baiduAd.setType(1);
//        baiduAd.setLanding_page("https://www.shuzilm.cn");
//        baiduAd.setTarget_url("http://pixel.shuzijz.cn/lingjitest");
//        baiduAd.setAdvertiser_id();//广告主id 对应advertiser 的nid ------------------78197397L  先不传
        Double biddingPrice = targetDuFlowBean.getBiddingPrice() * 100;
        Integer price = Integer.valueOf(biddingPrice.intValue());
        baiduAd.setMax_cpm(price);//价格 单位：分
//        baiduAd.setMonitor_urls("http://rtb.shuzijz.cn");
        baiduBidResponse.setAd(baiduAd);


        String nurl = targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +//mysql去重id
                "&act=" + format +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dbidp=" + targetDuFlowBean.getBiddingPrice() +// 广告主出价
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&dmat=" + targetDuFlowBean.getMaterialId() + //素材id
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
        //                "&impid=" + impression.getId() +
//                "&adx=" + duFlowBean.getAdxId() +
//                "&did=" + duFlowBean.getDid() +
//                "&appv=" + duFlowBean.getAppVersion() +
//                "&dpro=" + duFlowBean.getProvince() +// 省
//                "&dcit=" + duFlowBean.getCity() +// 市
//                "&dcou=" + duFlowBean.getCountry() +// 县
//                "&pmp=" + duFlowBean.getDealid() + //私有交易
//                "&app=" + URLEncoder.encode(duFlowBean.getAppName())+

        baiduAd.setExtdata(nurl);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                SSDBUtil.pushSSDB(targetDuFlowBean);
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                RedisUtil.pushRedis(targetDuFlowBean);
            }
        });
        MDC.put("sift", "baidubidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(baiduBidResponse));
        return baiduBidResponse;

    }


}

