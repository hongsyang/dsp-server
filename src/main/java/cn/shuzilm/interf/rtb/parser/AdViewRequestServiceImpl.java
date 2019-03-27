package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.*;
import cn.shuzilm.bean.adview.response.*;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.RtbJedisManager;
import cn.shuzilm.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import cn.shuzilm.filter.FilterRule;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;

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

    private static RtbJedisManager jedisManager = RtbJedisManager.getInstance("configs_rtb_redis.properties");

    private  Jedis jedis = jedisManager.getResource();

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();

    private static RuleMatching ruleMatching = RuleMatching.getInstance();

    private static final String ADX_NAME = "AdView";

    private static final String ADX_ID = "2";


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);


    private static final String RTB_REDIS_FILTER_CONFIG = "configs_rtb_redis.properties";

    private static AppConfigs redisConfigs = AppConfigs.getInstance(RTB_REDIS_FILTER_CONFIG);


    private JedisPool resource = new JedisPool(redisConfigs.getString("REDIS_SERVER_HOST"), redisConfigs.getInt("REDIS_SERVER_PORT"));

    //上传到ssdb 业务线程池
//    private ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("SSDB_EXECUTOR_THREADS"));
    private ExecutorService executor = null;

    @Override
    public String parseRequest(String dataStr, ExecutorService executor) throws Exception {
        this.executor = executor;
        String response = "空请求";
        if (StringUtils.isNotBlank(dataStr)) {
            MDC.put("sift", "dsp-server");
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            log.debug(" BidRequest参数入参：{}", dataStr);
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
            String appPackageName = null;//应用包名
            String tagid = userImpression.getTagid();//广告位id
            if (app != null) {
                appPackageName = app.getBundle();
            }


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
            //长宽为空的，默认为-1
            if (width == null || width == 0 | height == null || height == 0) {
                width = -1;
                height = -1;
            }


            //广告位列表 只有悠易和广点通需要
            List adxNameList = new ArrayList();//

            adxNameList.add(ADX_ID + "_" + tagid);//添加广告位id

            //是否匹配长宽
            Boolean isDimension = true;
            //广告位不为空
            if (tagid != null && !tagid.trim().equals("")) {
                isDimension = false;
            }


            Map msg = FilterRule.filterRuleBidRequest(deviceId, appPackageName, userDevice.getIp(), ADX_ID, adxNameList, width, height);//过滤规则的返回结果

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
                    appPackageName,//APP包名
                    adxNameList,//宽列表
                    isDimension,
                    bidRequestBean.getId(),
                    tagid//广告id
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
            targetDuFlowBean.setCreateTime(System.currentTimeMillis());//创建时间
            log.debug("拷贝没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
            BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, bidRequestBean);
            MDC.remove("sift");
            MDC.put("sift", "dsp-server");
            response = JSON.toJSONString(bidResponseBean);
            log.debug("没有过滤的bidResponseBean:{}", response);


            //测试环境自动发送曝光
//            Double bidfloorcur = Double.valueOf(userImpression.getBidfloor());
//            Double v = bidfloorcur * 1.3;
//            String price = "&price=" + v;
//            String pf = "&pf=" + targetDuFlowBean.getPremiumFactor();
//            String serviceUrl = configs.getString("SERVICE_URL");
//            String s = serviceUrl + "adviewclick?";
//            if (response.contains(s)) {
//                String substring = response.substring(response.indexOf(s));
//                String adviewexp = substring.substring(0, substring.indexOf('"')).replace("adviewclick", "adviewnurl");
//                String adviewexpUrl = adviewexp + price + pf;
//                Boolean flag = sendGetUrl(adviewexpUrl);
//                log.debug("是否曝光成功：{},adviewexpUrl:{}", flag, adviewexpUrl);
//            }

            bidRequestBean = null;
            targetDuFlowBean = null;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
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
                "id=" + duFlowBean.getRequestId() + // 请求id
                "&bidid=" + bidResponseBean.getBidid() +//mysql去重id
                "&act=" + format +
                "&device=" + duFlowBean.getDeviceId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&pf=" + duFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + duFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + duFlowBean.getCreativeUid() + // 创意id
                "&dbidp=" + duFlowBean.getBiddingPrice() +// 广告主出价
                "&dade=" + duFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + duFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + duFlowBean.getAdUid() + // 广告id，
                "&dmat=" + duFlowBean.getMaterialId() + //素材id
                "&userip=" + duFlowBean.getIpAddr();//用户ip
//                "&impid=" + impression.getId() +
//                "&adx=" + duFlowBean.getAdxId() +
//                "&did=" + duFlowBean.getDid() +
//                "&appv=" + duFlowBean.getAppVersion() +
//                "&dpro=" + duFlowBean.getProvince() +// 省
//                "&dcit=" + duFlowBean.getCity() +// 市
//                "&dcou=" + duFlowBean.getCountry() +// 县
//                "&pmp=" + duFlowBean.getDealid() + //私有交易
//                "&app=" + URLEncoder.encode(duFlowBean.getAppName())+
        if (instl == 0 | instl == 4 | instl == 1) {
            bid.setAdmt(1);//duFlowBean.getAdmt()广告类型
            bid.setCrid(duFlowBean.getCrid());//duFlowBean.getCrid()广告物料 ID
            bid.setAdi(duFlowBean.getAdmMap().get(0));//图片路径 duFlowBean.getAdm() 广告物料html数据
            bid.setAdh(duFlowBean.getAdh());//duFlowBean.getAdh()广告物料高度
            bid.setAdw(duFlowBean.getAdw());//duFlowBean.getAdw()广告物料宽度
        } else if (instl == 5) {
            bid.setAdmt(6);//duFlowBean.getAdmt()广告类型  视频广告
            ResponseVideo responseVideo = new ResponseVideo();
            responseVideo.setXmltype(2);
            responseVideo.setVideourl(duFlowBean.getAdmMap().get(0));
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
                    image.setUrl(duFlowBean.getAdmMap().get(0));
                    assetsImg.setImg(image);
                    assetsList.add(assetsImg);
                } else if (asset.getVideo() != null) {
                    NativeRequestVideo video = new NativeRequestVideo();
                    video.setXmltype(2);
                    video.setVideourl(duFlowBean.getAdmMap().get(0));
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
                "id=" + duFlowBean.getRequestId() + // 请求id
                "&bidid=" + bidResponseBean.getBidid() +//mysql去重id
                "&price=" + "%%WIN_PRICE%%" +
                "&act=" + format +
                "&device=" + duFlowBean.getDeviceId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&pf=" + duFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + duFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + duFlowBean.getCreativeUid() + // 创意id
                "&dbidp=" + duFlowBean.getBiddingPrice() +// 广告主出价
                "&dade=" + duFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + duFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + duFlowBean.getAdUid() + // 广告id，
                "&dmat=" + duFlowBean.getMaterialId() + //素材id
                "&userip=" + duFlowBean.getIpAddr();//用户ip
//                "&impid=" + impression.getId() +
//                "&adx=" + duFlowBean.getAdxId() +
//                "&did=" + duFlowBean.getDid() +
//                "&appv=" + duFlowBean.getAppVersion() +
//                "&dpro=" + duFlowBean.getProvince() +// 省
//                "&dcit=" + duFlowBean.getCity() +// 市
//                "&dcou=" + duFlowBean.getCountry() +// 县
//                "&pmp=" + duFlowBean.getDealid() + //私有交易
//                "&app=" + URLEncoder.encode(duFlowBean.getAppName())+


        bid.setWurl(wurl);//赢价通知，由 AdView 服务器 发出  编码格式的 CPM 价格*10000，如价格为 CPM 价格 0.6 元，则取值0.6*10000=6000。

        bid.setAdurl(landingUrl);//广告点击跳转落地页，可以支持重定向
        //曝光通知Nurl
        String nurl = serviceUrl + "adviewnurl?" +
                "id=" + duFlowBean.getRequestId() + // 请求id
                "&bidid=" + bidResponseBean.getBidid() +//mysql去重id
                "&price=" + "%%WIN_PRICE%%" +
                "&act=" + format +
                "&device=" + duFlowBean.getDeviceId() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&pf=" + duFlowBean.getPremiumFactor() +//溢价系数
                "&ddem=" + duFlowBean.getAudienceuid() + //人群id
                "&dcuid=" + duFlowBean.getCreativeUid() + // 创意id
                "&dbidp=" + duFlowBean.getBiddingPrice() +// 广告主出价
                "&dade=" + duFlowBean.getAdvertiserUid() +// 广告主id
                "&dage=" + duFlowBean.getAgencyUid() + //代理商id
                "&daduid=" + duFlowBean.getAdUid() + // 广告id，
                "&dmat=" + duFlowBean.getMaterialId() + //素材id
                "&userip=" + duFlowBean.getIpAddr();//用户ip
//                "&impid=" + impression.getId() +
//                "&adx=" + duFlowBean.getAdxId() +
//                "&did=" + duFlowBean.getDid() +
//                "&appv=" + duFlowBean.getAppVersion() +
//                "&dpro=" + duFlowBean.getProvince() +// 省
//                "&dcit=" + duFlowBean.getCity() +// 市
//                "&dcou=" + duFlowBean.getCountry() +// 县
//                "&pmp=" + duFlowBean.getDealid() + //私有交易
//                "&app=" + URLEncoder.encode(duFlowBean.getAppName())+

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
        long start = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                SSDBUtil.pushSSDB(duFlowBean);
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                MDC.put("sift", "redis");
//                log.debug("duFlowBean:{}", JSON.toJSONString(duFlowBean));
                pushRedis(duFlowBean);
            }
        });


//        pushRedis(duFlowBean);
        long end = System.currentTimeMillis();
        log.debug("上传到ssdb的时间:{}", end - start);
        MDC.put("sift", "bidResponseBean");
        log.debug("bidResponseBean:{}", JSON.toJSONString(bidResponseBean));
        return bidResponseBean;
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
            MDC.put("sift", "redis");
            log.error(" jedis Exception :{}", e);
            MDC.remove("sift");
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
