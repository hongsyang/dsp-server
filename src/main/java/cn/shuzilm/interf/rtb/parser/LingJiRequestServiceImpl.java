package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.App;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.lj.request.LJAssets;
import cn.shuzilm.bean.lj.response.LJBid;
import cn.shuzilm.bean.lj.response.LJResponseExt;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.FilterRule;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    private AppConfigs configs = null;

    private static final String FILTER_CONFIG = "filter.properties";

    private static final String ADX_NAME = "lingji";

    @Override
    public String parseRequest(String dataStr) {
        String response = "空请求";
        if (StringUtils.isNotBlank(dataStr)) {
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            log.debug(" BidRequest参数入参：{}", dataStr);
            Map msg = new HashMap();//过滤规则的返回结果
            //请求报文解析
            BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
            //创建返回结果  bidRequest请求参数保持不变
            Device userDevice = bidRequestBean.getDevice();//设备信息
            Impression userImpression = bidRequestBean.getImp().get(0);//曝光信息
            App app = bidRequestBean.getApp();//应用信息


            Integer showtype = userImpression.getExt().getShowtype();//广告类型
            String adType = convertAdType(showtype); //对应内部 广告类型
            
            if (StringUtils.isBlank(adType)) {
                response = "没有对应的广告类型";
                return response;
            }

            //文件扩展名列表
            Set<String> stringSet = new HashSet<>();
            if ("banner".equals(adType)) {
                String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
                for (String mime : mimes) {
                    stringSet.add(mime);
                }
            } else if ("fullscreen".equals(adType)) {
                String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
                for (String mime : mimes) {
                    stringSet.add(mime);
                }
            } else if ("feed".equals(adType)) {
                List<LJAssets> assets = userImpression.getNativead().getAssets();
                for (LJAssets asset : assets) {
                    if (asset.getImg() != null && asset.getRequired().equals(true)) {
                        for (String mime : asset.getImg().getMimes()) {
                            stringSet.add(mime);
                        }
                    } else if (asset.getVideo() != null && asset.getRequired().equals(true)) {
                        for (String mime : asset.getVideo().getMimes()) {
                            stringSet.add(mime);
                        }
                    }

                }
            }
            String nodes[] = {"172.17.129.116,7001", "172.17.129.116,7002", "172.17.129.116,7003", "172.17.129.116,7004", "172.17.129.116,7005", "172.17.129.116,7006"};
            //初步过滤规则开关
            if (Boolean.valueOf(configs.getString("FILTER_SWITCH"))) {
                if (FilterRule.filterRuleBidRequest(bidRequestBean, true, msg, ADX_NAME)) {
                    DUFlowBean targetDuFlowBean = RuleMatching.getInstance(nodes).match(
                            userDevice.getExt().getMac(),//设备mac的MD5
                            adType,//广告类型
                            userImpression.getBanner().getW(),//广告位的宽
                            userImpression.getBanner().getH(),//广告位的高
                            true,// 是否要求分辨率
                            5,//宽误差值
                            5,// 高误差值;
                            ADX_NAME,//ADX 服务商名称
                            stringSet//文件扩展名
                    );
                    //需要添加到Phoenix中的数据
                    targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
                    targetDuFlowBean.setImpression(bidRequestBean.getImp());//曝光id
                    targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
                    targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
                    targetDuFlowBean.setAdxAdTypeId(showtype);//广告小类对应ADX服务商的ID
                    targetDuFlowBean.setAdxId("0001");//ADX广告商id
                    targetDuFlowBean.setAdxId("0001");//bid id
                    targetDuFlowBean.setAdxId("0001");//dsp id
                    targetDuFlowBean.setAdxId("0001");//info id
                    targetDuFlowBean.setAppName(app.getName());//APP名称
                    targetDuFlowBean.setAppPackageName(app.getBundle());//APP包名
//                    targetDuFlowBean.setAppVersion(app.getExt().getSdk() == null ? app.getExt().getSdk():""  );//广告小类对应ADX服务商的ID

                    log.debug("过滤通过的targetDuFlowBean:{}", targetDuFlowBean);
                    BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, adType);
//                    pushRedis(targetDuFlowBean);//上传到redis服务器
                    log.debug("json计数");
                    response = JSON.toJSONString(bidResponseBean);
                    log.debug("过滤通过的bidResponseBean:{}", response);
                } else {
                    response = JSON.toJSONString(msg);//过滤规则结果输出
                }

            } else {
                DUFlowBean targetDuFlowBean = RuleMatching.getInstance(nodes).match(
                        userDevice.getExt().getMac(),//设备mac的MD5
                        adType,//广告类型
                        userImpression.getBanner().getW(),//广告位的宽
                        userImpression.getBanner().getH(),//广告位的高
                        true,// 是否要求分辨率
                        5,//宽误差值
                        5,// 高误差值;
                        ADX_NAME,//ADX 服务商名称
                        stringSet//文件扩展名
                );
                //需要添加到Phoenix中的数据
                targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
                targetDuFlowBean.setImpression(bidRequestBean.getImp());//曝光id
                targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
                targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
                targetDuFlowBean.setAdxAdTypeId(showtype);//广告小类对应ADX服务商的ID
                targetDuFlowBean.setAdxId("0001");//ADX广告商id
                targetDuFlowBean.setAppName(app.getName());//APP名称
                targetDuFlowBean.setAppPackageName(app.getBundle());//APP包名
//                targetDuFlowBean.setAppVersion(app.getExt().getSdk());//APP版本


                log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
                BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, adType);
//                pushRedis(targetDuFlowBean);//上传到redis服务器
                response = JSON.toJSONString(bidResponseBean);
                log.debug("没有过滤的bidResponseBean:{}", response);
            }
            return response;
        } else {
            return response;
        }
    }

    /**
     * 内部流转DUFlowBean  转换为  BidResponseBean 输出给 ADX服务器
     *
     * @param duFlowBean
     * @return
     */
    private BidResponseBean convertBidResponse(DUFlowBean duFlowBean, String adType) {
        BidResponseBean bidResponseBean = new BidResponseBean();
        //请求报文BidResponse返回
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
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
        bid.setId(format + UUID.randomUUID());//duFlowBean.getDspid()////DSP对该次出价分配的ID   时间戳+UUID
        bid.setImpid(impression.getId());//从bidRequestBean里面取
        if ("banner".equals(adType)) {
            bid.setAdm(duFlowBean.getAdm());// 广告物料数据
        }
        Double biddingPrice = duFlowBean.getBiddingPrice() * 100;
        Float price = Float.valueOf(String.valueOf(biddingPrice));
        bid.setPrice(price);//price 测试值  //CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
        bid.setCrid(duFlowBean.getCreativeUid());//duFlowBean.getCrid() 测试值//广告物料 ID  ,投放动态创意(即c类型的物料),需添加该字段
        //曝光nurl
        String nurl = "http://101.200.56.200:8880/" + "lingjiexp?" +
                "id=" + "${AUCTION_ID}" +
                "&bidid=" + "${AUCTION_BID_ID}" +
                "&impid=" + "${AUCTION_IMP_ID}" +
                "&price=" + "${AUCTION_PRICE}" +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppName() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pf=" + duFlowBean.getPremiumFactor() +
                "&pmp=" + duFlowBean.getDealid();

        bid.setNurl(nurl);

        String curl = "http://101.200.56.200:8880/" + "lingjiclick?" +
                "id=" + duFlowBean.getRequestId() +
                "&bidid=" + bidResponseBean.getBidid() +
                "&impid=" + impression.getId() +
                "&act=" + format +
                "&adx=" + duFlowBean.getAdxId() +
                "&did=" + duFlowBean.getDid() +
                "&device=" + duFlowBean.getDeviceId() +
                "&app=" + duFlowBean.getAppName() +
                "&appn=" + duFlowBean.getAppPackageName() +
                "&appv=" + duFlowBean.getAppVersion() +
                "&pmp=" + duFlowBean.getDealid();
        LJResponseExt ljResponseExt = new LJResponseExt();
        ljResponseExt.setLdp(duFlowBean.getLandingUrl());//落地页。广告点击后会跳转到物料上绑定的landingpage，还是取实时返回的ldp，参见
        //曝光监测数组
        List pm = new ArrayList();
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
        return bidResponseBean;
    }

    /**
     * 把生成的内部流转DUFlowBean上传到redis服务器 设置5分钟失效
     *
     * @param targetDuFlowBean
     */
    private void pushRedis(DUFlowBean targetDuFlowBean) {
        log.debug("redis计数");
        Jedis jedis = JedisManager.getInstance().getResource();
        if (jedis != null) {
            log.debug("jedis：{}", jedis);
            String set = jedis.set(targetDuFlowBean.getRequestId(), JSON.toJSONString(targetDuFlowBean));
            Long expire = jedis.expire(targetDuFlowBean.getRequestId(), 5 * 60);//设置超时时间为5分钟
            log.debug("推送到redis服务器是否成功;{},设置超时时间是否成功(成功返回1)：{}", set, expire);
        } else {
            log.debug("jedis为空：{}", jedis);
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
        if (showtype == 14 || showtype == 11) {
            adType = "banner";//横幅
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 13 || showtype == 20 || showtype == 19) {
            adType = "feed";//信息流
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 15 || showtype == 12 || showtype == 17) {
            adType = "fullscreen";//开屏
            log.debug("广告类型adType:{}", adType);
        } else if (showtype == 16 || showtype == 18) {
            adType = "interstitial";//插屏
            log.debug("广告类型adType:{}", adType);
        } else {
            adType = null;
        }
        return adType;
    }
}
