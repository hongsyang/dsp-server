package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.util.IpBlacklistUtil;
import cn.shuzilm.util.MD5Util;
import com.google.common.collect.Lists;

import bidserver.BidserverSsp;
import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.youyi.request.YouYiAdzone;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiMobile;
import cn.shuzilm.bean.youyi.request.YouYiUser;
import cn.shuzilm.bean.youyi.response.YouYiAd;
import cn.shuzilm.bean.youyi.response.YouYiBidResponse;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description: YouYiParser 灵集post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:37
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:37
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class YouYiRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(YouYiRequestServiceImpl.class);

    private AppConfigs configs = null;

    private static final String FILTER_CONFIG = "filter.properties";

    private static final String ADX_NAME = "YouYi";

    private static final String ADX_ID = "3";

    private static JedisManager jedisManager = JedisManager.getInstance();

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();

    private static RuleMatching ruleMatching = RuleMatching.getInstance();


    @Override
    public String parseRequest(String dataStr) throws Exception {
        String adxId = "3";
        String response = "";
        if (StringUtils.isNotBlank(dataStr)) {
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            MDC.put("sift", "dsp-server");
            log.debug(" BidRequest参数入参：{}", dataStr);
            //请求报文解析
            YouYiBidRequest bidRequestBean = JSON.parseObject(dataStr, YouYiBidRequest.class);
            //创建返回结果  bidRequest请求参数保持不变
            YouYiMobile userDevice = bidRequestBean.getMobile();//设备APP信息
            YouYiAdzone adzone = bidRequestBean.getAdzone().get(0);//曝光信息
            YouYiUser user = bidRequestBean.getUser();//用户信息

            Integer width = null;//广告位的宽
            Integer height = null;//广告位的高
//            Integer showtype = userImpression.getExt().getShowtype();//广告类型
            String adType = null; //对应内部 广告类型
            String stringSet = null;//文件类型列表
            String deviceId = null;//设备号
            //ip 黑名单规则  在黑名单内直接返回
            if (ipBlacklist.isIpBlacklist(user.getUser_ip())){
                log.debug("IP黑名单:{}", user.getUser_ip());
                response = "";
                return response;
            }


//            if (StringUtils.isBlank(adType)) {
//                response = "没有对应的广告类型";
//                return response;
//
//            }


//            //设备的设备号：用于匹配数盟库中的数据

            if (userDevice != null) {
                if ("ios".equals(userDevice.getDevice_os().toLowerCase())) {
                    deviceId = userDevice.getIdfa();
                } else if ("android".equalsIgnoreCase(userDevice.getDevice_os().toLowerCase())) {
//                    deviceId = userDevice.getExt().getMac();
                    deviceId = userDevice.getMd5_imei();
                }
            }

//            //支持的文件类型
            String adz_type = adzone.getAdz_type();
            if (adz_type.equals("ADZONE_TYPE_INAPP_BANNER") | adz_type.equals("ADZONE_TYPE_WAP_BANNER")) {
                stringSet = "[image/jpeg, image/png]";
                //广告位的宽和高
                width = adzone.getAdz_width();
                height = adzone.getAdz_height();
            } else if (adz_type.equals("ADZONE_TYPE_INAPP_VIDEO") | adz_type.equals("ADZONE_TYPE_WAP_VIDEO")) {
                stringSet = "[ application/x-shockwave-flash，video/x-flv]";
                //广告位的宽和高
                width = adzone.getAdz_width();
                height = adzone.getAdz_height();
            } else if (adz_type.equals("ADZONE_TYPE_INAPP_NATIVE")) {
                stringSet = "[image/jpeg, image/png]";
                adxId = adxId + "_"+adzone.getNative().get(0).getNative_id();
                //广告位的宽和高
                width = adzone.getAdz_width();
                height = adzone.getAdz_height();
            }else {
                response = "pc 不竞价";
                return response;
            }

            //广告匹配规则
            DUFlowBean targetDuFlowBean = ruleMatching.match(
                    deviceId,//设备mac的MD5
                    adType,//广告类型
                    width,//广告位的宽
                    height,//广告位的高
                    true,// 是否要求分辨率
                    0,//宽误差值
                    0,// 高误差值;
                    adxId,//ADX 服务商ID
                    stringSet,//文件扩展名
                    user.getUser_ip(),//用户ip
                    userDevice.getApp_bundle()//APP包名
            );
            if (targetDuFlowBean == null) {
                response = "";
                return response;
            }
            //需要添加到Phoenix中的数据
            targetDuFlowBean.setRequestId(bidRequestBean.getSession_id());//bidRequest id
            //曝光id
            List<Impression> list = new ArrayList();
            Impression impression = new Impression();
            impression.setId(adzone.getAdz_id());
            list.add(impression);
            targetDuFlowBean.setImpression(list);//曝光id
            targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
            targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
            targetDuFlowBean.setAdxId(ADX_ID);//ADX广告商id
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String format = LocalDateTime.now().format(formatter);//时间戳
            targetDuFlowBean.setBidid(format + UUID.randomUUID());//bid id  时间戳+随机数不去重
            targetDuFlowBean.setDspid(format + UUID.randomUUID());//dsp id
            targetDuFlowBean.setAppName(userDevice.getApp_name());//APP名称
            targetDuFlowBean.setAppPackageName(userDevice.getApp_bundle());//APP包名
            log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
            YouYiBidResponse bidResponseBean = convertBidResponse(targetDuFlowBean, bidRequestBean);
            response = JSON.toJSONString(bidResponseBean);
            MDC.put("sift", "dsp-server");
            log.debug("bidResponseBean:{}", response);
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
     * @return YouYiBidResponse
     */
    private YouYiBidResponse convertBidResponse(DUFlowBean targetDuFlowBean, YouYiBidRequest bidRequestBean) {
        YouYiBidResponse youYiBidResponse = new YouYiBidResponse();
        youYiBidResponse.setSession_id(bidRequestBean.getSession_id());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);//时间戳
        //广告信息
        List ads = new ArrayList();
        YouYiAd youYiAd = new YouYiAd();
        YouYiAdzone youYiAdzone = bidRequestBean.getAdzone().get(0);//曝光信息
        youYiAd.setAdz_id(youYiAdzone.getAdz_id());//广告位id
        youYiAd.setAdz_array_id(0);//广告数组id
        double biddingPrice = targetDuFlowBean.getBiddingPrice() * 100;//广告出价
        youYiAd.setBid_price((int) biddingPrice);
        youYiAd.setAdvertiser_id(targetDuFlowBean.getAdvertiserUid());//广告主id
        youYiAd.setCreative_id(Integer.valueOf(targetDuFlowBean.getCrid()));//推审id
        //曝光通知Nurl
        String wurl = "id=" + targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +
                "&impid=" +  youYiAdzone.getAdz_id() +
                "&price=" + "__PRICE__" +
                "&act=" + format +
                "&adx=" + targetDuFlowBean.getAdxId() +
                "&did=" + targetDuFlowBean.getDid() +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&app=" + URLEncoder.encode(targetDuFlowBean.getAppName()) +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&appv=" + targetDuFlowBean.getAppVersion() +
                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&pmp=" + targetDuFlowBean.getDealid()+ //私有交易
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
        youYiAd.setWin_para(wurl);//赢价通知，按此收费
        //曝光通知Nurl
        String nurl = "id=" + targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +
                "&impid=" +  youYiAdzone.getAdz_id() +
                "&price=" + "__PRICE__" +
                "&act=" + format +
                "&adx=" + targetDuFlowBean.getAdxId() +
                "&did=" + targetDuFlowBean.getDid() +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&app=" + URLEncoder.encode(targetDuFlowBean.getAppName()) +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&appv=" + targetDuFlowBean.getAppVersion() +
                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&pmp=" + targetDuFlowBean.getDealid()+ //私有交易
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
        youYiAd.setImp_para(nurl);//曝光通知
        String curl = "id=" + targetDuFlowBean.getRequestId() +
                "&bidid=" + targetDuFlowBean.getBidid() +
                "&impid=" + youYiAdzone.getAdz_id() +
                "&act=" + format +
                "&adx=" + targetDuFlowBean.getAdxId() +
                "&did=" + targetDuFlowBean.getDid() +
                "&device=" + targetDuFlowBean.getDeviceId() +
                "&app=" + URLEncoder.encode(targetDuFlowBean.getAppName()) +
                "&appn=" + targetDuFlowBean.getAppPackageName() +
                "&appv=" + targetDuFlowBean.getAppVersion() +
                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
                "&pmp=" + targetDuFlowBean.getDealid() + //私有交易
                "&userip=" + targetDuFlowBean.getIpAddr();//用户ip
        youYiAd.setClk_para(curl);//点击通知
        ads.add(youYiAd);
        youYiBidResponse.setAds(ads);
        MDC.put("sift", "bidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(youYiBidResponse));
        return youYiBidResponse;

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


}
