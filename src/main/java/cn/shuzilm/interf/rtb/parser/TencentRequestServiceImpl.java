package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.tencent.request.*;
import cn.shuzilm.bean.tencent.response.TencentBid;
import cn.shuzilm.bean.tencent.response.TencentBidResponse;
import cn.shuzilm.bean.tencent.response.TencentSeatBid;
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

/**
 * @Description: YouYiParser 灵集post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:37
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:37
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class TencentRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(TencentRequestServiceImpl.class);


    private static final String FILTER_CONFIG = "filter.properties";

    private static final String ADX_NAME = "Tencent";

    private static final String ADX_ID = "4";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();

    private static WidthAndHeightListUtil widthAndHeightListUtil = WidthAndHeightListUtil.getInstance();

    private static RuleMatching ruleMatching = RuleMatching.getInstance();


    @Override
    public String parseRequest(String dataStr) throws Exception {
        String adxId = "4";
        String response = "";
        if (StringUtils.isNotBlank(dataStr)) {
            MDC.put("sift", "dsp-server");
            log.debug(" BidRequest参数入参：{}", dataStr);
            //请求报文解析
            TencentBidRequest bidRequestBean = JSON.parseObject(dataStr, TencentBidRequest.class);
            //创建返回结果  bidRequest请求参数保持不变
            TencentDevice userDevice = bidRequestBean.getDevice();//设备信息
            TencentImpressions adzone = bidRequestBean.getImpressions().get(0);//曝光信息
            TencentUser user = bidRequestBean.getUser();//用户信息
            TencentApp app = bidRequestBean.getApp();
            String appPackageName="";
            String userIp="";
            if (bidRequestBean.getIp()!=null){
                userIp=bidRequestBean.getIp();
            }
            if (app!=null){
                appPackageName=app.getApp_bundle_id();
            }
            Integer width = -1;//广告位的宽
            Integer height = -1;//广告位的高
            String adType = ""; //对应内部 广告类型
            String stringSet = null;//文件类型列表
            String deviceId = null;//设备号
            String tagid = String.valueOf(adzone.getPlacement_id());


            //设备的设备号：用于匹配数盟库中的数据
            if (userDevice != null) {
                if (userDevice.getOs().toLowerCase().contains("ios")) {
                    deviceId = userDevice.getIdfa();
                } else if (userDevice.getOs().toLowerCase().contains("android")) {
                    //广点通设备mac地址
                    deviceId = userDevice.getId();
                }
            }

            Map msg = FilterRule.filterRuleBidRequest(deviceId,appPackageName,userIp);//过滤规则的返回结果

            //ip黑名单和 设备黑名单，媒体黑名单 内直接返回
            if (msg.get("ipBlackList") != null) {
                return "ipBlackList";
            } else if (msg.get("bundleBlackList") != null) {
                return "bundleBlackList";
            } else if (msg.get("deviceIdBlackList") != null) {
                return "deviceIdBlackList";
            }


//            //支持的文件类型
            if (adzone.getMultimedia_type_white_list() != null) {
                stringSet = adzone.getMultimedia_type_white_list().toString();
            } else {
                stringSet = "[video/mp4, application/x-shockwave-flash，video/x-flv,image/jpeg, image/png]";
            }

            List adxNameList = new ArrayList();//
            List<Integer> creative_specs = adzone.getCreative_specs();
            for (Integer creative_spec : creative_specs) {
                adxNameList.add(adxId+"_"+creative_spec);
            }
            log.debug("adxNameList:{}", adxNameList);
            //是否匹配长宽
            Boolean  isDimension=false;
            //广告匹配规则
            DUFlowBean targetDuFlowBean = ruleMatching.match(
                    deviceId,//设备mac的MD5
                    adType,//广告类型
                    width,//广告位的宽
                    height,//广告位的高
                    false,// 是否要求分辨率
                    0,//宽误差值
                    0,// 高误差值;
                    adxId,//ADX 服务商ID
                    stringSet,//文件扩展名
                    userIp,//用户ip
                    appPackageName,//APP包名
                    adxNameList,//长宽列表
                    isDimension,
                    bidRequestBean.getId(),
                    tagid
            );
            if (targetDuFlowBean == null) {
                response = "";
                return response;
            }
            //需要添加到Phoenix中的数据
            targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
            //曝光id
            List<Impression> list = new ArrayList();
            Impression impression = new Impression();
            impression.setId(adzone.getId());
            list.add(impression);
            targetDuFlowBean.setImpression(list);//曝光id
            targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
            targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
            targetDuFlowBean.setAdxId(ADX_ID);//ADX广告商id
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String format = LocalDateTime.now().format(formatter);//时间戳
            targetDuFlowBean.setBidid(format+UUID.randomUUID().toString().substring(0,21));//bid id  时间戳+随机数不去重
            targetDuFlowBean.setDspid(format + UUID.randomUUID());//dsp id
            targetDuFlowBean.setAppName("");//APP名称
            targetDuFlowBean.setAppPackageName(appPackageName);//APP包名
            log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
            TencentBidResponse bidResponseBean = convertBidResponse(targetDuFlowBean, bidRequestBean);
            response = JSON.toJSONString(bidResponseBean);
            MDC.put("sift", "dsp-server");
            log.debug("bidResponseBean:{}", response);


            //测试环境自动发送曝光
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
     * @return TencentBidResponse
     */
    private TencentBidResponse convertBidResponse(DUFlowBean targetDuFlowBean, TencentBidRequest bidRequestBean) {
        TencentBidResponse TencentBidResponse = new TencentBidResponse();
        TencentBidResponse.setRequest_id(bidRequestBean.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);//时间戳
        //广告信息
        List ads = new ArrayList();
        TencentSeatBid tencentSeatBid = new TencentSeatBid();
        //曝光信息
        TencentImpressions tencentImpressions = bidRequestBean.getImpressions().get(0);
        //曝光id
        tencentSeatBid.setImpression_id(tencentImpressions.getId());
        //腾讯 Bid 类型
        List tencentBidList = new ArrayList();
        TencentBid tencentBid = new TencentBid();

        double biddingPrice = targetDuFlowBean.getBiddingPrice() * 100;//广告出价
        tencentBid.setBid_price((int) biddingPrice);
        tencentBid.setCreative_id(targetDuFlowBean.getCrid());//推审id
        //曝光通知Nurl
        String wurl =  targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +
                "&impid=" + tencentImpressions.getId() +
                "&act=" + format +
                "&adx=" + targetDuFlowBean.getAdxId() +
                "&did=" + targetDuFlowBean.getDid() +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&dmat=" + targetDuFlowBean.getMaterialId() + //素材id
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
//        tencentBid.setWinnotice_param(wurl);//赢价通知，按此收费
        //曝光通知Nurl
        String nurl =  targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +
                "&impid=" + tencentImpressions.getId() +
                "&act=" + format +
                "&adx=" + targetDuFlowBean.getAdxId() +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&dmat=" + targetDuFlowBean.getMaterialId() + //素材id
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
        tencentBid.setImpression_param(nurl);//曝光通知
        String curl = targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +
                "&impid=" + tencentImpressions.getId() +
                "&act=" + format +
                "&adx=" + targetDuFlowBean.getAdxId() +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&dmat=" + targetDuFlowBean.getMaterialId() + //素材id
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
        tencentBid.setClick_param(curl);//点击通知
        //腾讯 Bid 类型列表
        tencentBidList.add(tencentBid);
        tencentSeatBid.setBids(tencentBidList);
        //腾讯  seat_bids类型列表
        ads.add(tencentSeatBid);
        TencentBidResponse.setSeat_bids(ads);
        MDC.put("sift", "bidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(TencentBidResponse));
        return TencentBidResponse;

    }


    /**
     * 把生成的内部流转DUFlowBean上传到redis服务器 设置5分钟失效
     *
     * @param targetDuFlowBean
     */
/*    private void pushRedis(DUFlowBean targetDuFlowBean) {
        log.debug("redis连接时间计数");
        Jedis jedis = jedisManager.getResource();
        try {
            if (jedis != null) {
                log.debug("jedis：{}", jedis);
                String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
                Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 5 * 60);//设置超时时间为5分钟
                log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{}", set, expire);
            } else {
                log.debug("jedis为空：{}", jedis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
    }*/

    /**
     * 广告类型转换
     *
     * @param showtype
     * @return
     */
    private String convertAdType(Integer showtype) {
        String adType = "";
        if (showtype == 14 | showtype == 11) {
            adType = "banner";//横幅
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 13 | showtype == 20 | showtype == 19) {
            adType = "feed";//信息流
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 15 | showtype == 12 | showtype == 17) {
            adType = "fullscreen";//开屏
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 16 | showtype == 18 | showtype == 4) {
            adType = "interstitial";//插屏
            log.debug("广告类型adType:{}", adType);
        } else {
            adType = null;
        }
        return adType;
    }

    /**
     * 发送曝光请求
     *
     * @param lingjiexp
     */
    private Boolean sendGetUrl(String lingjiexp) {
        String s = HttpClientUtil.get(lingjiexp);
        if (s != null) {
            return true;
        } else {
            return false;
        }
    }


}

