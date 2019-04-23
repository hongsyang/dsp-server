package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.*;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.lj.request.*;
import cn.shuzilm.bean.lj.response.*;
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

import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Description: LingjiParser 灵集post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:37
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:37
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiRequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(LingJiRequestServiceImpl.class);

    private static final String FILTER_CONFIG = "filter.properties";

    private static final String ADX_NAME = "LingJi";

    private static final String ADX_ID = "1";


    private static RtbJedisManager jedisManager = RtbJedisManager.getInstance("configs_rtb_redis.properties");

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static RuleMatching ruleMatching = RuleMatching.getInstance();


    private static final String RTB_REDIS_FILTER_CONFIG = "configs_rtb_redis.properties";

    private static AppConfigs redisConfigs = AppConfigs.getInstance(RTB_REDIS_FILTER_CONFIG);


    private static JedisPool resource = new JedisPool(redisConfigs.getString("REDIS_SERVER_HOST"), redisConfigs.getInt("REDIS_SERVER_PORT"));

    private static Jedis jedis = resource.getResource();

    //上传到ssdb 业务线程池
//    private ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("SSDB_EXECUTOR_THREADS"));
    private ExecutorService executor = null;

    //初始统计次数
    private static Integer lingjiNum = 1;

    //统计流量中的http和https
    private static ConcurrentHashMap<String, Integer> lingjiCountMap = new ConcurrentHashMap();


//    static {
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            MDC.put("sift", "https-count");
//            log.debug("http和https 的数据:{}", JSON.toJSONString(lingjiCountMap));
//            MDC.remove("sift");
//        }, 0, configs.getInt("COUNT_TIME"), TimeUnit.MINUTES);
//    }


    @Override
    public String parseRequest(String dataStr, ExecutorService executor, ConcurrentHashMap<String, Integer> countMap) throws Exception {
        this.executor = executor;
        String response = "空请求";
        if (StringUtils.isNotBlank(dataStr)) {
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            MDC.put("sift", "dsp-server");
            log.debug(" BidRequest参数入参：{}", dataStr);
            //请求报文解析
            BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
            //创建返回结果  bidRequest请求参数保持不变
            Device userDevice = bidRequestBean.getDevice();//设备信息
            Impression userImpression = bidRequestBean.getImp().get(0);//曝光信息
            App app = bidRequestBean.getApp();//应用信息
            Integer width = null;//广告位的宽
            Integer height = null;//广告位的高
            Integer showtype = userImpression.getExt().getShowtype();//广告类型
            String adType = convertAdType(showtype); //对应内部 广告类型

            String tagid = userImpression.getTagid();//广告位id


            String stringSet = null;//文件类型列表
            String deviceId = null;//设备号
            String appPackageName = null;//应用包名
            if (app != null) {
                appPackageName = app.getBundle();
            }


            //设备的设备号：用于匹配数盟库中的数据
            if (userDevice != null) {
                if (userDevice.getOs() != null) {
                    if ("ios".equals(userDevice.getOs().toLowerCase())) {
                        deviceId = userDevice.getExt().getIdfa();
                    } else if ("android".equalsIgnoreCase(userDevice.getOs().toLowerCase())) {
                        //竞价请求进来之前对imei和mac做过滤
                        if (userDevice.getDidmd5() != null) {
                            if (userDevice.getDidmd5().length() == 32) {
                            }
                        } else if (userDevice.getMacmd5() != null) {
                            if (userDevice.getExt().getMacmd5().length() == 32) {
                                userDevice.setDidmd5("mac-" + userDevice.getExt().getMacmd5());
                            }
                        } else {
                            log.debug("imeiMD5和macMD5不符合规则，imeiMD5:{}，macMD5:{}", userDevice.getDidmd5(), userDevice.getExt().getMacmd5());
                            response = "deviceIdBlackList";
                            return response;
                        }
                        deviceId = userDevice.getDidmd5();
                    } else if ("wp".equals(userDevice.getOs().toLowerCase())) {
//                    deviceId = userDevice.getExt().getMac();
                        deviceId = userDevice.getDidmd5();
                    }
                }
            }


            //支持的文件类型
            List<LJAssets> assets = new ArrayList<>();
            if ("banner".equals(adType)) {// banner 类型
                width = userImpression.getBanner().getW();
                height = userImpression.getBanner().getH();
                String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                stringSet = Arrays.toString(mimes);

            } else if ("fullscreen".equals(adType)) { //开屏
                if (userImpression.getVideo() != null) {
                    width = userImpression.getVideo().getW();
                    height = userImpression.getVideo().getH();
                    String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
                    stringSet = Arrays.toString(mimes);
                } else if (userImpression.getBanner() != null) {
                    width = userImpression.getBanner().getW();
                    height = userImpression.getBanner().getH();
                    String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                    stringSet = Arrays.toString(mimes);
                }
            } else if ("interstitial".equals(adType)) {//插屏
                if (userImpression.getVideo() != null) {
                    width = userImpression.getVideo().getW();
                    height = userImpression.getVideo().getH();
                    String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
                    stringSet = Arrays.toString(mimes);
                } else if (userImpression.getBanner() != null) {
                    width = userImpression.getBanner().getW();
                    height = userImpression.getBanner().getH();
                    String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                    stringSet = Arrays.toString(mimes);
                }

            } else if (userImpression.getNativead() != null) {
                assets = userImpression.getNativead().getAssets();
                for (LJAssets asset : assets) {
                    if (asset.getImg() != null && asset.getRequired() != null) {
                        if (asset.getRequired().equals(true)) {
                            width = asset.getImg().getW();
                            height = asset.getImg().getH();
                            stringSet = Arrays.toString(asset.getImg().getMimes());
                        }
                    } else if (asset.getImg() != null && asset.getRequired() != null) {
                        if (asset.getRequired().equals(true)) {
                            width = asset.getVideo().getW();
                            height = asset.getVideo().getH();
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


            List adxNameList = new ArrayList();//
            adxNameList.add(ADX_ID + "_" + tagid);//添加广告位id

            //是否匹配长宽
            Boolean isDimension = true;
            //广告位不为空
            if (tagid != null && !tagid.trim().equals("")) {
                isDimension = false;
            }

            String http = "http";
            String https = "https";
            Integer secure = null;
            if (userImpression.getSecure() != null) {
                if (userImpression.getSecure() == 1) {
                    secure = userImpression.getSecure();
                    String httpKey = LocalDate.now().toString() + "," + appPackageName + "," + https;
                    if (countMap.get(httpKey) != null) {
                        Integer linkNum = countMap.get(httpKey);
                        linkNum++;//连接次数 + 1
                        countMap.put(httpKey, linkNum);
                    } else {
                        countMap.put(httpKey, lingjiNum);
                    }

                } else if (userImpression.getSecure() == 0) {
                    secure = userImpression.getSecure();
                    String httpKey = LocalDate.now().toString() + "," + appPackageName + "," + http;
                    if (countMap.get(httpKey) != null) {
                        Integer linkNum = countMap.get(httpKey);
                        linkNum++;//连接次数 + 1
                        countMap.put(httpKey, linkNum);
                    } else {
                        countMap.put(httpKey, lingjiNum);
                    }
                } else {
                    String httpKey = LocalDate.now().toString() + "," + appPackageName + "," + userImpression.getSecure();
                    if (countMap.get(httpKey) != null) {
                        Integer linkNum = countMap.get(httpKey);
                        linkNum++;//连接次数 + 1
                        countMap.put(httpKey, linkNum);
                    } else {
                        countMap.put(httpKey, lingjiNum);
                    }
                }
            }
//            //增加统计日志
//            executor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//                        MDC.put("sift", "https-count");
//                        log.debug("http和https 的数据:{}", JSON.toJSONString(lingjiCountMap));
//                        MDC.remove("sift");
//                    }, 0, configs.getInt("COUNT_TIME"), TimeUnit.MINUTES);
//                }
//            });
            log.debug("http和https 的数据:{}", JSON.toJSONString(lingjiCountMap));
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
                    false,// 是否要求分辨率
                    5,//宽误差值
                    5,// 高误差值;
                    ADX_ID,//ADX 服务商ID
                    stringSet,//文件扩展名
                    userDevice.getIp(),//用户ip
                    appPackageName,//APP包名
                    adxNameList,
                    isDimension,
                    bidRequestBean.getId(),
                    tagid,
                    secure
            );
            if (targetDuFlowBean == null) {
                response = "";
                return response;
            }
            MDC.put("sift", "dsp-server");
            log.debug("bidRequestBean.id:{}", bidRequestBean.getId());
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
            targetDuFlowBean.setCreateTime(System.currentTimeMillis());//创建时间
            if (app.getExt() != null) {
                targetDuFlowBean.setAppVersion(app.getExt().getSdk() == null ? "" : app.getExt().getSdk());//APP版本
            }


            log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
            BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, adType, assets, secure);
            MDC.remove("sift");

            response = JSON.toJSONString(bidResponseBean);
            MDC.put("sift", "dsp-server");
            log.debug("没有过滤的bidResponseBean:{}", response);//
            //合并Jenkins

            //测试环境自动发送曝光
//            Double bidfloorcur = Double.valueOf(userImpression.getBidfloor());
//            Double v = bidfloorcur * 1.3;
//            String price = "&price=" + v;
//            String pf = "&pf=" + targetDuFlowBean.getPremiumFactor();
//            String serviceUrl = configs.getString("SERVICE_URL");
//            String s = serviceUrl + "lingjiclick?";
//            if (response.contains(s)) {
//                String substring = response.substring(response.indexOf(s));
//                String lingjiexp = substring.substring(0, substring.indexOf('"')).replace("lingjiclick", "lingjiexp");
//                String lingjiexpUrl = lingjiexp + price + pf;
//                Boolean flag = sendGetUrl(lingjiexpUrl);
//                log.debug("是否曝光成功：{},lingjiexpUrl:{}", flag, lingjiexpUrl);
//            }


            //jenkins 合并1
            targetDuFlowBean = null;
            bidRequestBean = null;
            return response;
        } else {
            return response;
        }
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


    /**
     * 内部流转DUFlowBean  转换为  BidResponseBean 输出给 ADX服务器
     *
     * @param duFlowBean
     * @return
     */
    private BidResponseBean convertBidResponse(DUFlowBean duFlowBean, String adType, List<LJAssets> ljAssets, Integer secure) {
        BidResponseBean bidResponseBean = new BidResponseBean();
        //请求报文BidResponse返回
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);//时间戳
        bidResponseBean.setId(duFlowBean.getRequestId());//从bidRequestBean里面取 bidRequest的id
        bidResponseBean.setBidid(duFlowBean.getBidid());//duFlowBean.getBidid() BidResponse 的唯一标识,由 DSP生成  时间戳+UUID
        List<SeatBid> seatBidList = new ArrayList<SeatBid>();//注意第一层数组  DSP出价 目前仅支持一个
        List<LJBid> bidList = new ArrayList<LJBid>();//注意第二层数组 针对单次曝光的出价
        SeatBid seatBid = new SeatBid();
        seatBid.setSeat(duFlowBean.getSeat());//SeatBid 的标识,由 DSP 生成
        LJBid bid = new LJBid();
        List<Impression> imp = duFlowBean.getImpression();//从bidRequestBean里面取
        Impression impression = imp.get(0);
        bid.setId(duFlowBean.getBidid());//duFlowBean.getDspid()////DSP对该次出价分配的ID   时间戳+UUID
        bid.setImpid(impression.getId());//从bidRequestBean里面取
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
        //winnotice 链接
        String nurl = serviceUrl + "lingjiexp?" +
                "id=" + "${AUCTION_ID}" +
                "&bidid=" + "${AUCTION_BID_ID}" +
                "&impid=" + "${AUCTION_IMP_ID}" +
                "&price=" + "${AUCTION_PRICE}" +
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
                "&dpro=" + duFlowBean.getProvince() +// 省
                "&dcit=" + duFlowBean.getCity() +// 市
                "&dcou=" + duFlowBean.getCountry() +// 县
                "&userip=" + duFlowBean.getIpAddr();//用户ip
        bid.setNurl(nurl);

        //曝光检测地址
        String lingjiimp = serviceUrl + "lingjiimp?" +
                "id=" + "${AUCTION_ID}" +
                "&bidid=" + "${AUCTION_BID_ID}" +
                "&impid=" + "${AUCTION_IMP_ID}" +
                "&price=" + "${AUCTION_PRICE}" +
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
                "&dpro=" + duFlowBean.getProvince() +// 省
                "&dcit=" + duFlowBean.getCity() +// 市
                "&dcou=" + duFlowBean.getCountry() +// 县
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


        String curl = serviceUrl + "lingjiclick?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
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
                "&dpro=" + duFlowBean.getProvince() +// 省
                "&dcit=" + duFlowBean.getCity() +// 市
                "&dcou=" + duFlowBean.getCountry() +// 县
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

        //人群包，创意id，省，市，广告主id，代理商id，广告id，
        String materialUrl = duFlowBean.getAdmMap().get(0);//素材地址

        if (secure != null) {
            if (secure == 1) {
                landingUrl = landingUrl.replace("http://", "https://");
                nurl = nurl.replace("http://", "https://");
                lingjiimp = lingjiimp.replace("http://", "https://");
                curl = curl.replace("http://", "https://");
                materialUrl = materialUrl.replace("http://", "https://");
            } else {
                    landingUrl = landingUrl.replace("https://", "http://");
                    nurl = nurl.replace("https://", "http://");
                    lingjiimp = nurl.replace("https://", "http://");
                    curl = nurl.replace("https://", "http://");
                    materialUrl = nurl.replace("https://", "http://");

            }
        }
        if ("banner".equals(adType)) {
            bid.setAdm(materialUrl);//  横幅
        } else if ("fullscreen".equals(adType)) {
            bid.setAdm(materialUrl);// 开屏
        } else if ("interstitial".equals(adType)) {
            bid.setAdm(materialUrl);// 插屏
        } else if ("feed".equals(adType)) {//信息流
            bid.setNurl("");//
            LJNativeResponse ljNativeResponse = new LJNativeResponse();

            NativeAD nativeAD = new NativeAD();
            List urls = new ArrayList();
            urls.add(nurl);
            nativeAD.setImptrackers(urls);// 展示曝光URL数组

            LJLink ljLink = new LJLink();//	点击跳转URL地址(落地页)
            ljLink.setUrl(landingUrl);//落地页
            List curls = new ArrayList();
            curls.add(curl);
            ljLink.setClicktrackers(curls);
            ljLink.setAction(2);
            nativeAD.setLink(ljLink);

            List<LJEvent> ljEvents = new ArrayList<>();
            LJEvent event = new LJEvent();
            event.setV("0");
            ljEvents.add(event);
            nativeAD.setEvent(ljEvents);

            List<LJAssets> ljAssetsList = new ArrayList<>();
            LJAssets assetsTitle = new LJAssets();

            LJNativeTitle ljNativeTitle = new LJNativeTitle();//标题
            ljNativeTitle.setText(duFlowBean.getTitle());
            assetsTitle.setTitle(ljNativeTitle);
            ljAssetsList.add(assetsTitle);


            LJAssets assetsData = new LJAssets();
            LJNativeData ljNativeData = new LJNativeData();

            ljNativeData.setValue(duFlowBean.getDesc());
            assetsData.setData(ljNativeData);
            ljAssetsList.add(assetsData);


            for (LJAssets ljAsset : ljAssets) {
                if (ljAsset.getTitle() != null && ljAsset.getRequired() != null) {
                    if (ljAsset.getRequired().equals(true)) {
                        assetsTitle.setId(ljAsset.getId());
                    }
                } else if (ljAsset.getData() != null && ljAsset.getRequired() != null) {
                    if (ljAsset.getRequired().equals(true)) {
                        assetsData.setId(ljAsset.getId());
                    }
                } else if (ljAsset.getImg() != null && ljAsset.getRequired() != null) {
                    if (ljAsset.getRequired().equals(true)) {
                        LJAssets assetsImg = new LJAssets();
                        LJNativeImg ljNativeImg = new LJNativeImg();
                        String imgUrl = materialUrl;
                        List<String> imgUrls = new ArrayList<>();
                        imgUrls.add(imgUrl);
                        ljNativeImg.setUrls(imgUrls);
                        assetsImg.setImg(ljNativeImg);
                        assetsImg.setId(ljAsset.getId());
                        ljAssetsList.add(assetsImg);
                    }
                } else if (ljAsset.getVideo() != null && ljAsset.getRequired() != null) {
                    if (ljAsset.getRequired().equals(true)) {
                        LJAssets assetsVideo = new LJAssets();
                        LJNativeVideo ljNativeVideo = new LJNativeVideo();
                        String videoUrl = materialUrl;
                        ljNativeVideo.setUrl(videoUrl);
                        assetsVideo.setVideo(ljNativeVideo);
                        assetsVideo.setId(ljAsset.getId());
                        ljAssetsList.add(assetsVideo);
                    }
                }
            }


            nativeAD.setAssets(ljAssetsList);
            ljNativeResponse.setNativead(nativeAD);
            String nativeADJsonString = JSON.toJSONString(ljNativeResponse);
            log.debug("nativeADJsonString:{}", nativeADJsonString);
            String encodeString = URLEncoder.encode(nativeADJsonString.trim());
            log.debug("encodeString:{}", encodeString);
            bid.setAdm(encodeString);// 广告物料数据
        }
        Double biddingPrice = duFlowBean.getBiddingPrice() * 100;
        Float price = Float.valueOf(String.valueOf(biddingPrice));
        bid.setPrice(price);//price 测试值  //CPM 出价，数值为 CPM 实际价格*100，如出价为 0.6 元， 单位：分
        bid.setCrid(duFlowBean.getCrid());//duFlowBean.getCrid() 测试值//广告物料 ID  ,投放动态创意(即c类型的物料),需添加该字段


        LJResponseExt ljResponseExt = new LJResponseExt();

        ljResponseExt.setLdp(landingUrl);//落地页。广告点击后会跳转到物料上绑定的landingpage，还是取实时返回的ldp，参见
        //曝光监测数组
        List pm = new ArrayList();

        pm.add(lingjiimp);
        pm.add(duFlowBean.getTracking());
        ljResponseExt.setPm(pm);//注意曝光监测url是数组
        //点击监测数组
        List curls = new ArrayList();
        curls.add(curl);
        curls.add(duFlowBean.getLinkUrl());
        ljResponseExt.setCm(curls);//注意点击监测url是数组
        bid.setExt(ljResponseExt);

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
                RedisUtil.pushRedis(duFlowBean);
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
}
