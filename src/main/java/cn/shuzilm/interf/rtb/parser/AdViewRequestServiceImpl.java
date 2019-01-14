package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.*;
import cn.shuzilm.bean.adview.response.*;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import cn.shuzilm.filter.FilterRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description: KuaiyouParser 快友post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:39
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:39
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AdViewRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(AdViewRequestServiceImpl.class);

    private static JedisManager jedisManager = JedisManager.getInstance();

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();

    private static RuleMatching ruleMatching = RuleMatching.getInstance();

    private static final String ADX_NAME = "AdView";

    private static final String ADX_ID = "2";

    private AppConfigs configs = null;

    private static final String FILTER_CONFIG = "filter.properties";

    @Override
    public String parseRequest(String dataStr) throws Exception {
        String response = "空请求";
        if (StringUtils.isNotBlank(dataStr)) {
            MDC.put("sift", "dsp-server");
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            log.debug(" BidRequest参数入参：{}", dataStr);
            Map msg = new HashMap();//过滤规则的返回结果
            //请求报文解析
            BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
            //创建返回结果  bidRequest请求参数保持不变
            Device userDevice = bidRequestBean.getDevice();//设备信息
            Impression userImpression = bidRequestBean.getImp().get(0);//曝光信息
            App app = bidRequestBean.getApp();//应用信息
            Integer width = null;//广告位的宽
            Integer height = null;//广告位的高
            Integer showtype = userImpression.getInstl();//广告类型
            String adType = convertAdType(showtype); //对应内部 广告类型
            String stringSet = null;//文件类型列表
            String deviceId = null;//设备号
            //ip 黑名单规则  在黑名单内直接返回
            if (Boolean.valueOf(configs.getString("IP_BLACK_LIST"))) {
                if (ipBlacklist.isIpBlacklist(userDevice.getIp())) {
                    log.debug("IP黑名单:{}", userDevice.getIp());
                    response = "ipBlackList";
                    return response;
                }
            }
            // 过滤媒体黑名单
            if (Boolean.valueOf(configs.getString("BUNDLE_BLACK_LIST"))) {
                if (app != null) {
                    String bundle = app.getBundle();
                    if (AppBlackListUtil.inAppBlackList(bundle)) {
                        log.debug("媒体黑名单:{}", bundle);
                        response = "bundleBlackList";
                        return response;
                    }
                }
            }

//            if (StringUtils.isBlank(adType)) {
//                response = "";
//                return response;
//            }
            //设备的设备号：用于匹配数盟库中的数据
            if (userDevice != null) {
                if ("ios".equals(userDevice.getOs().toLowerCase())) {
                    deviceId = userDevice.getIfa();
                } else if ("android".equalsIgnoreCase(userDevice.getOs().toLowerCase())) {
                    //竞价请求进来之前对imei和mac做过滤
                    if (userDevice.getDidmd5() != null) {
                        if (userDevice.getDidmd5().length() == 32) {
                        }
                    } else if (userDevice.getMacmd5() != null) {
                        if (userDevice.getMacmd5().length() == 32) {
                            userDevice.setDidmd5("mac-" + userDevice.getMacmd5());
                        }
                    } else {
                        log.debug("imeiMD5和macMD5不符合规则，imeiMD5:{}，macMD5:{}", userDevice.getDidmd5(), userDevice.getMacmd5());
                        response = "deviceIdBlackList";
                        return response;
                    }
                    deviceId = userDevice.getDidmd5();
                } else if ("wp".equals(userDevice.getOs().toLowerCase())) {
                    deviceId = userDevice.getDidmd5();
                }
            }

            // 过滤设备黑名单
            if (Boolean.valueOf(configs.getString("DEVICE_ID_BLACK_LIST"))) {
                if (DeviceBlackListUtil.inDeviceBlackList(deviceId)) {
                    log.debug("设备黑名单:{}", deviceId);
                    response = "deviceIdBlackList";
                    return response;
                }
            }
            //支持的文件类型
            List<Assets> assets = new ArrayList<>();
            if ("banner".equals(adType)) {// banner 类型
                width = userImpression.getBanner().getW();
                height = userImpression.getBanner().getH();
                String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                if (mimes == null) {
                    stringSet = "[image/jpeg, image/png]";
                } else {
                    stringSet = Arrays.toString(mimes);
                }

            } else if ("fullscreen".equals(adType)) { //开屏
                if (userImpression.getVideo() != null) {
                    width = userImpression.getVideo().getW();
                    height = userImpression.getVideo().getH();
                    String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
                    if (mimes == null) {
                        stringSet = "[image/jpeg, image/png]";
                    } else {
                        stringSet = Arrays.toString(mimes);
                    }
                } else if (userImpression.getBanner() != null) {
                    width = userImpression.getBanner().getW();
                    height = userImpression.getBanner().getH();
                    String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                    if (mimes == null) {
                        stringSet = "[image/jpeg, image/png]";
                    } else {
                        stringSet = Arrays.toString(mimes);
                    }
                }
            } else if ("interstitial".equals(adType)) {//插屏
                if (userImpression.getVideo() != null) {
                    width = userImpression.getVideo().getW();
                    height = userImpression.getVideo().getH();
                    String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
                    if (mimes == null) {
                        stringSet = "[image/jpeg, image/png]";
                    } else {
                        stringSet = Arrays.toString(mimes);
                    }
                } else if (userImpression.getBanner() != null) {
                    width = userImpression.getBanner().getW();
                    height = userImpression.getBanner().getH();
                    String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                    if (mimes == null) {
                        stringSet = "[image/jpeg, image/png]";
                    } else {
                        stringSet = Arrays.toString(mimes);
                    }
                }

            } else if ("feed".equals(adType)) { //信息流
                assets = userImpression.getNative().getRequest().getAssets();
                for (Assets asset : assets) {
                    if (asset.getImg() != null && asset.getRequired().equals(1)) {
                        width = asset.getImg().getW();
                        height = asset.getImg().getH();
                        if (asset.getImg().getMimes() == null) {
                            stringSet = "[image/jpeg, image/png]";
                        } else {
                            stringSet = Arrays.toString(asset.getImg().getMimes());
                        }
                    } else if (asset.getVideo() != null && asset.getRequired().equals(1)) {
                        width = asset.getVideo().getW();
                        height = asset.getVideo().getH();
                        if (asset.getVideo().getMimes() == null) {
                            stringSet = "[image/jpeg, image/png]";
                        } else {
                            stringSet = Arrays.toString(asset.getVideo().getMimes());
                        }
                    }

                }
            }
            if (width == null||width == 0| height == null||height == 0) {
                width = -1;
                height = -1;
            }
//            MDC.put("sift", "rtb-adview");
//            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" ,deviceId,adType,width,height,ADX_ID,stringSet, userDevice.getIp(),app.getBundle());
//            MDC.put("sift", "dsp-server");
            //初步过滤规则开关
//            if (Boolean.valueOf(configs.getString("FILTER_SWITCH"))) {
//                if (FilterRule.filterRuleBidRequest(bidRequestBean, true, msg, "adview")) {
//                    DUFlowBean targetDuFlowBean = ruleMatching.match(
//                            deviceId,//设备mac的MD5
//                            adType,//广告类型
//                            width,//广告位的宽
//                            height,//广告位的高
//                            true,// 是否要求分辨率
//                            5,//宽误差值
//                            5,// 高误差值;
//                            ADX_ID,//ADX 服务商ID
//                            stringSet,//文件扩展名
//                            userDevice.getIp(),//用户ip
//                            app.getBundle()//APP包名
//                    );
//                    if (targetDuFlowBean == null) {
//                        response = "";
//                        return response;
//                    }
//                    targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
//                    targetDuFlowBean.setImpression(bidRequestBean.getImp());//曝光id
//                    targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
//                    targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
//                    targetDuFlowBean.setAdxAdTypeId(showtype);//广告小类对应ADX服务商的ID
//                    targetDuFlowBean.setAdxId(ADX_ID);//ADX广告商id
//                    targetDuFlowBean.setBidid(MD5Util.MD5(MD5Util.MD5(bidRequestBean.getId())));//bid id
//                    targetDuFlowBean.setDspid(LocalDateTime.now().toString() + UUID.randomUUID());//dsp id
//                    targetDuFlowBean.setAppName(app.getName());//APP名称
//                    targetDuFlowBean.setAppPackageName(app.getBundle());//APP包名
//                    targetDuFlowBean.setAppVersion(app.getVer());//设备版本号
//                    log.debug("拷贝过滤通过的targetDuFlowBean:{}", targetDuFlowBean);
//                    BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, bidRequestBean);
//                    MDC.remove("sift");
////                    pushRedis(targetDuFlowBean);//上传到redis服务器
//                    response = JSON.toJSONString(bidResponseBean);
//                    log.debug("过滤通过的bidResponseBean:{}", response);
//                } else {
//                    response = JSON.toJSONString(msg);//过滤规则结果输出
//                }
//                msg.clear();
//                msg = null;
//            } else {
            DUFlowBean targetDuFlowBean = ruleMatching.match(
                    deviceId,//设备mac的MD5
                    adType,//广告类型
                    width,//广告位的宽
                    height,//广告位的高
                    true,// 是否要求分辨率
                    5,//宽误差值
                    5,// 高误差值;
                    ADX_ID,//ADX 服务商ID
                    stringSet,//文件扩展名
                    userDevice.getIp(),//用户ip
                    app.getBundle(),//APP包名
                    bidRequestBean.getId()
            );
            if (targetDuFlowBean == null) {
                response = "";
                return response;
            }
            MDC.put("sift", "dsp-server");
            //需要添加到Phoenix中的数据
            targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
            targetDuFlowBean.setImpression(bidRequestBean.getImp());//曝光id
            targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
            targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
            targetDuFlowBean.setAdxAdTypeId(showtype);//广告小类对应ADX服务商的ID
            targetDuFlowBean.setAdxId(ADX_ID);//ADX广告商id
            targetDuFlowBean.setBidid(MD5Util.MD5(MD5Util.MD5(bidRequestBean.getId())));//bid id
            targetDuFlowBean.setDspid(LocalDateTime.now().toString() + UUID.randomUUID());//dsp id
            targetDuFlowBean.setAppName(app.getName());//APP名称
            targetDuFlowBean.setAppPackageName(app.getBundle());//APP包名
            targetDuFlowBean.setAppId(app.getId());//APP包名
            targetDuFlowBean.setAppVersion(app.getVer());//设备版本号
            log.debug("拷贝没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
            BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, bidRequestBean);
            MDC.remove("sift");
            MDC.put("sift", "dsp-server");
            response = JSON.toJSONString(bidResponseBean);
            log.debug("没有过滤的bidResponseBean:{}", response);

            response = JSON.toJSONString(bidResponseBean);

            bidRequestBean = null;
            targetDuFlowBean = null;
//            }
            return response;
        } else {
            return response;
        }
    }

    /**
     * 发送曝光请求
     *
     * @param adviewexp
     * @return
     */
    private Boolean sendGetUrl(String adviewexp) {
        String s = HttpClientUtil.get(adviewexp);
        if (s != null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 内部流转DUFlowBean  转换为  BidResponseBean 输出给 ADX服务器
     *
     * @param duFlowBean
     * @return
     */
    private BidResponseBean convertBidResponse(DUFlowBean duFlowBean, BidRequestBean bidRequestBean) {
        BidResponseBean bidResponseBean = new BidResponseBean();
        //请求报文BidResponse返回
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String format = LocalDateTime.now().format(formatter);//时间戳
        bidResponseBean.setId(duFlowBean.getRequestId());//从bidRequestBean里面取 bidRequest的id
        bidResponseBean.setBidid(duFlowBean.getBidid());//BidResponse 的唯一标识,由 DSP生成
        List<SeatBid> seatBidList = new ArrayList<SeatBid>();//注意第一层数组  DSP出价 目前仅支持一个
        List<Bid> bidList = new ArrayList<Bid>();//注意第二层数组 针对单次曝光的出价
        SeatBid seatBid = new SeatBid();
        seatBid.setSeat(duFlowBean.getSeat());//SeatBid 的标识,由 DSP 生成
        Bid bid = new Bid();
        List<Impression> imp = duFlowBean.getImpression();//从bidRequestBean里面取
        Impression impression = imp.get(0);
        bid.setImpid(impression.getId());//从bidRequestBean里面取
        bid.setAdid(duFlowBean.getMaterialId());//duFlowBean.getAdUid()广告id，对应数据库Uid；
        Integer instl = bidRequestBean.getImp().get(0).getInstl();
        String landingUrlA = duFlowBean.getLandingUrl();//落地页
        String landingUrl = "";
        if (landingUrl.contains("?")) {
            landingUrl = landingUrlA +
                    "&advertiserUid=" + duFlowBean.getAdvertiserUid() +
                    "&adUid=" + duFlowBean.getAdUid() +
                    "&creativeUid=" + duFlowBean.getCreativeUid() +
                    "&materialId=" + duFlowBean.getMaterialId();

        } else {
            landingUrl = landingUrlA +
                    "?advertiserUid=" + duFlowBean.getAdvertiserUid() +
                    "&adUid=" + duFlowBean.getAdUid() +
                    "&creativeUid=" + duFlowBean.getCreativeUid() +
                    "&materialId=" + duFlowBean.getMaterialId();
        }
        String serviceUrl = configs.getString("SERVICE_URL");
        String curl = serviceUrl + "adviewclick?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + URLEncoder.encode(duFlowBean.getAppName()) +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&ddem=" + duFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + duFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + duFlowBean.getProvince() +// 省
                "&dcit=" + duFlowBean.getCity() +// 市
                "&dcou=" + duFlowBean.getCountry() +// 县
                "&dade=" + duFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + duFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + duFlowBean.getAdUid() + // 广告id，
                "&pmp=" + duFlowBean.getDealid() + //私有交易
                "&dmat=" + duFlowBean.getMaterialId() + //素材id
                "&userip=" + duFlowBean.getIpAddr();//用户ip
        if (instl == 0 | instl == 4 | instl == 1) {
            bid.setAdmt(1);//duFlowBean.getAdmt()广告类型
            bid.setCrid(duFlowBean.getCrid());//duFlowBean.getCrid()广告物料 ID
            bid.setAdi(duFlowBean.getAdm());//图片路径 duFlowBean.getAdm() 广告物料html数据
            bid.setAdh(duFlowBean.getAdh());//duFlowBean.getAdh()广告物料高度
            bid.setAdw(duFlowBean.getAdw());//duFlowBean.getAdw()广告物料宽度
        } else if (instl == 5) {
            bid.setAdmt(6);//duFlowBean.getAdmt()广告类型  视频广告
            ResponseVideo responseVideo = new ResponseVideo();
            responseVideo.setXmltype(2);
            responseVideo.setVideourl(duFlowBean.getAdm());
            responseVideo.setDuration(15);
            responseVideo.setWidth(duFlowBean.getAdw());
            responseVideo.setHeight(duFlowBean.getAdw());
            bid.setVideo(responseVideo);

        } else if (instl == 6) {
            bid.setAdmt(8);//duFlowBean.getAdmt()广告类型   信息流广告
            NativeResponseBean nativeResponseBean = new NativeResponseBean();
            nativeResponseBean.setVer("1");
            List<Assets> assets = bidRequestBean.getImp().get(0).getNative().getRequest().getAssets();
            List<Assets> assetsList = new ArrayList<>();
            Assets assetsTitle = new Assets();
            Assets assetsData = new Assets();
            Assets assetsImg = new Assets();
            Assets assetsVideo = new Assets();
            for (Assets asset : assets) {
                if (asset.getTitle() != null) {
                    assetsTitle.setId(asset.getId());
                } else if (asset.getData() != null) {
                    assetsData.setId(asset.getId());
                } else if (asset.getImg() != null) {
                    assetsImg.setId(asset.getId());
                } else if (asset.getVideo() != null) {
                    assetsVideo.setId(asset.getId());
                }
            }
            NativeRequestTitle title = new NativeRequestTitle();
            title.setText(duFlowBean.getTitle());
            assetsTitle.setTitle(title);
            assetsList.add(assetsTitle);

            NativeRequestData data = new NativeRequestData();
            data.setValue(duFlowBean.getDesc());
            assetsData.setData(data);
            assetsList.add(assetsData);
            for (Assets asset : assets) {
                if (asset.getImg() != null) {
                    NativeRequestImage image = new NativeRequestImage();
                    image.setW(duFlowBean.getAdw());
                    image.setH(duFlowBean.getAdh());
                    image.setUrl(duFlowBean.getAdm());
                    assetsImg.setImg(image);
                    assetsList.add(assetsImg);
                } else if (asset.getVideo() != null) {
                    NativeRequestVideo video = new NativeRequestVideo();
                    video.setXmltype(2);
                    video.setVideourl(duFlowBean.getAdm());
                    video.setDuration(15);
                    video.setWidth(duFlowBean.getAdw());
                    video.setHeight(duFlowBean.getAdh());
                    assetsVideo.setVideo(video);
                    assetsList.add(assetsVideo);
                }
            }


            nativeResponseBean.setAssets(assetsList);
            ResponseLink link = new ResponseLink();
            List<String> linkCurls = new ArrayList<>();
            linkCurls.add(curl);
            link.setUrl(landingUrl);
//            link.setClicktrackers(linkCurls);
            nativeResponseBean.setLink(link);
            List<String> curls = new ArrayList<>();
            curls.add(curl);
//            nativeResponseBean.setImptrackers(curls);//点击检测
            log.debug("nativeResponseBean:{}", nativeResponseBean);
            bid.setNative(nativeResponseBean);
        } else {
            log.debug("无此类型广告：{}", instl);
        }
        //赢价通知wurl
        String wurl = serviceUrl + "adviewexp?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&price=" + "%%WIN_PRICE%%" +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + URLEncoder.encode(duFlowBean.getAppName()) +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pf=" + duFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + duFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + duFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + duFlowBean.getProvince() +// 省
                "&dcit=" + duFlowBean.getCity() +// 市
                "&dcou=" + duFlowBean.getCountry() +// 县
                "&dade=" + duFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + duFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + duFlowBean.getAdUid() + // 广告id，
                "&pmp=" + duFlowBean.getDealid() + //私有交易
                "&dmat=" + duFlowBean.getMaterialId() + //素材id
                "&userip=" + duFlowBean.getIpAddr();//用户ip

        bid.setWurl(wurl);//赢价通知，由 AdView 服务器 发出  编码格式的 CPM 价格*10000，如价格为 CPM 价格 0.6 元，则取值0.6*10000=6000。

        bid.setAdurl(landingUrl);//广告点击跳转落地页，可以支持重定向
        //曝光通知Nurl
        String nurl = serviceUrl + "adviewnurl?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&price=" + "%%WIN_PRICE%%" +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + URLEncoder.encode(duFlowBean.getAppName()) +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pf=" + duFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + duFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + duFlowBean.getCreativeUid() + // 创意id
                "&dpro=" + duFlowBean.getProvince() +// 省
                "&dcit=" + duFlowBean.getCity() +// 市
                "&dcou=" + duFlowBean.getCountry() +// 县
                "&dade=" + duFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + duFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + duFlowBean.getAdUid() + // 广告id，
                "&pmp=" + duFlowBean.getDealid() + //私有交易
                "&dmat=" + duFlowBean.getMaterialId() + //素材id
                "&userip=" + duFlowBean.getIpAddr();//用户ip

        Map nurlMap = new HashMap();
        List<String> trackingurls = new ArrayList<>();
        trackingurls.add(duFlowBean.getTracking());
        trackingurls.add(nurl);
        nurlMap.put("0", trackingurls);
        bid.setNurl(nurlMap);//带延迟的曝光，由客户端发送  //曝光监测


        List curls = new ArrayList();
        curls.add(curl);
        curls.add(duFlowBean.getLinkUrl());
        bid.setCurl(curls);//点击监控地址，客户端逐个发送通知

        double biddingPrice = duFlowBean.getBiddingPrice() * 10000;
        Integer price = (int) biddingPrice;
        bid.setPrice(price);//CPM 出价

        bid.setAdct(0);//duFlowBean.getAdct() 广告点击行为类型，参考附录 9
        bid.setCid(duFlowBean.getCrid());//duFlowBean.getCreativeUid()广告创意 ID，可用于去重
        //添加到list中
        bidList.add(bid);
        seatBid.setBid(bidList);
        seatBidList.add(seatBid);
        bidResponseBean.setSeatbid(seatBidList);
        MDC.put("sift", "bidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(bidResponseBean));
        return bidResponseBean;
    }

    /**
     * 把生成的内部流转DUFlowBean上传到redis服务器 设置5分钟失效
     *
     * @param targetDuFlowBean
     */
    private void pushRedis(DUFlowBean targetDuFlowBean) {
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
    }

    /**
     * 广告类型转换
     *
     * @param showtype
     * @return
     */
    private String convertAdType(Integer showtype) {
        String adType = "";
        if (showtype == 0) {
            adType = "banner";//横幅
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 6) {
            adType = "feed";//信息流
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 4 || showtype == 5) {
            adType = "fullscreen";//开屏
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 1) {
            adType = "interstitial";//插屏
            log.debug("广告类型adType:{}", adType);
        } else {
            adType = null;
        }
        return adType;
    }
}
