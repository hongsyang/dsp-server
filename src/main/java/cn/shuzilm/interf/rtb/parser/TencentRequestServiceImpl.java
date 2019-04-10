package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.tencent.request.*;
import cn.shuzilm.bean.tencent.response.TencentBid;
import cn.shuzilm.bean.tencent.response.TencentBidResponse;
import cn.shuzilm.bean.tencent.response.TencentSeatBid;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.RtbJedisManager;
import cn.shuzilm.filter.FilterRule;
import cn.shuzilm.util.*;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: TencentParser 腾讯post参数解析
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

    private static RtbJedisManager jedisManager = RtbJedisManager.getInstance("configs_rtb_redis.properties");


    private static WidthAndHeightListUtil widthAndHeightListUtil = WidthAndHeightListUtil.getInstance();

    private static RuleMatching ruleMatching = RuleMatching.getInstance();


    private static final String RTB_REDIS_FILTER_CONFIG = "configs_rtb_redis.properties";

    private static AppConfigs redisConfigs = AppConfigs.getInstance(RTB_REDIS_FILTER_CONFIG);


    private static JedisPool resource = new JedisPool(redisConfigs.getString("REDIS_SERVER_HOST"), redisConfigs.getInt("REDIS_SERVER_PORT"));

    private static Jedis jedis = resource.getResource();

    //上传到ssdb 业务线程池
//    private ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("SSDB_EXECUTOR_THREADS"));
    private ExecutorService executor = null;

    @Override
    public String parseRequest(String dataStr, ExecutorService executor) throws Exception {
        this.executor = executor;
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
            String appPackageName = "";
            String userIp = "";
            if (bidRequestBean.getIp() != null) {
                userIp = bidRequestBean.getIp();
            }
            if (app != null) {
                appPackageName = app.getApp_bundle_id();
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


//            //支持的文件类型
            if (adzone.getMultimedia_type_white_list() != null) {
                stringSet = adzone.getMultimedia_type_white_list().toString();
            } else {
                stringSet = "[video/mp4, application/x-shockwave-flash，video/x-flv,image/jpeg, image/png]";
            }

            List adxNameList = new ArrayList();//
            List<Integer> creative_specs = adzone.getCreative_specs();
            for (Integer creative_spec : creative_specs) {
                adxNameList.add(adxId + "_" + creative_spec);
            }
            log.debug("adxNameList:{}", adxNameList);
            //是否匹配长宽
            Boolean isDimension = false;

            Map msg = FilterRule.filterRuleBidRequest(deviceId, appPackageName, userIp, adxId, adxNameList, width, height);//过滤规则的返回结果

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
            targetDuFlowBean.setBidid(format + UUID.randomUUID().toString().substring(0, 21));//bid id  时间戳+随机数不去重
            targetDuFlowBean.setDspid(format + UUID.randomUUID());//dsp id
            targetDuFlowBean.setAppName("");//APP名称
            targetDuFlowBean.setAppPackageName(appPackageName);//APP包名
            targetDuFlowBean.setCreateTime(System.currentTimeMillis());//创建时间
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
        String wurl = targetDuFlowBean.getRequestId() +
//                "&bidid=" + targetDuFlowBean.getBidid() +
//                "&impid=" + tencentImpressions.getId() +
                "&act=" + format +
//                "&adx=" + targetDuFlowBean.getAdxId() +
//                "&did=" + targetDuFlowBean.getDid() +
//                "&device=" + targetDuFlowBean.getDeviceId() +
//                "&dade=" + targetDuFlowBean.getAdvertiserUid() +// 广告主id
//                "&dage=" + targetDuFlowBean.getAgencyUid() + //代理商id
//                "&daduid=" + targetDuFlowBean.getAdUid() + // 广告id，
//                "&dmat=" + targetDuFlowBean.getMaterialId() + //素材id
//                "&pf=" + targetDuFlowBean.getPremiumFactor() +//溢价系数
//                "&ddem=" + targetDuFlowBean.getAudienceuid() + //人群id
//                "&dcuid=" + targetDuFlowBean.getCreativeUid() + // 创意id
//                "&dbidp=" + targetDuFlowBean.getBiddingPrice() +// 广告主出价
//                "&dpro=" + targetDuFlowBean.getProvince() +// 省
//                "&dcit=" + targetDuFlowBean.getCity() +// 市
//                "&dcou=" + targetDuFlowBean.getCountry() +// 县
                "&userip=" + targetDuFlowBean.getIpAddr() +//用户ip

                "&appn=" + targetDuFlowBean.getAppPackageName();
//        tencentBid.setWinnotice_param(wurl);//赢价通知，暂无
        //曝光通知Nurl
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
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
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
        tencentBid.setImpression_param(nurl);//曝光通知  按此收费

        String curl = targetDuFlowBean.getRequestId() +
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
                "&dpro=" + targetDuFlowBean.getProvince() +// 省
                "&dcit=" + targetDuFlowBean.getCity() +// 市
                "&dcou=" + targetDuFlowBean.getCountry() +// 县
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
        tencentBid.setClick_param(curl);//点击通知
        //腾讯 Bid 类型列表
        tencentBidList.add(tencentBid);
        tencentSeatBid.setBids(tencentBidList);
        //腾讯  seat_bids类型列表
        ads.add(tencentSeatBid);
        TencentBidResponse.setSeat_bids(ads);
        long start = System.currentTimeMillis();
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

//        pushRedis(targetDuFlowBean);
        long end = System.currentTimeMillis();
        log.debug("上传到ssdb的时间:{}", end - start);
        MDC.put("sift", "bidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(TencentBidResponse));
        return TencentBidResponse;

    }


    /**
     * 把生成的内部流转DUFlowBean上传到redis服务器 设置60分钟失效
     *
     * @param targetDuFlowBean
     */
    private void pushRedis(DUFlowBean targetDuFlowBean) {
        MDC.put("sift", "redis");
        try {
            if (jedis != null) {
                String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
                Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 60 * 60);//设置超时时间为60分钟
                log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{},RequestId;{}", set, expire, targetDuFlowBean.getRequestId());
            } else {
                jedis = RtbJedisManager.getInstance("configs_rtb_redis.properties").getResource();
                String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
                Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 60 * 60);//设置超时时间为60分钟
                log.debug("jedis为空：{},重新加载", jedis);
                log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{},RequestId;{}", set, expire, targetDuFlowBean.getRequestId());
                MDC.remove("sift");
            }
        } catch (Exception e) {
            resource.returnBrokenResource(jedis);
            MDC.put("sift", "redis");
            log.error(" jedis Exception :{}", e);
            MDC.remove("sift");
        } finally {
            resource.returnResource(jedis);
        }
    }

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

