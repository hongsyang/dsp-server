package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.bean.adview.request.App;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.bean.lj.request.*;
import cn.shuzilm.bean.lj.response.*;
import cn.shuzilm.bean.youyi.BidserverSsp;
import cn.shuzilm.bean.youyi.request.YouYiAdzone;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiMobile;
import cn.shuzilm.bean.youyi.response.YouYiAd;
import cn.shuzilm.bean.youyi.response.YouYiBidResponse;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.FilterRule;
import cn.shuzilm.util.MD5Util;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

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


    private static RuleMatching ruleMatching = RuleMatching.getInstance();


    @Override
    public String parseRequest(String dataStr) throws Exception {
        String response = "";
        if (StringUtils.isNotBlank(dataStr)) {
            this.configs = AppConfigs.getInstance(FILTER_CONFIG);
            MDC.put("sift", "dsp-server");
            log.debug(" BidRequest参数入参：{}", dataStr);
            //请求报文解析
            YouYiBidRequest bidRequestBean = JSON.parseObject(dataStr, YouYiBidRequest.class);
            //创建返回结果  bidRequest请求参数保持不变
            YouYiMobile userDevice = bidRequestBean.getMobile();//设备APP信息
            YouYiAdzone userImpression = bidRequestBean.getAdzone();//曝光信息

//            App app = bidRequestBean.getApp();//应用信息
//            Integer width = null;//广告位的宽
//            Integer height = null;//广告位的高
//            Integer showtype = userImpression.getExt().getShowtype();//广告类型
//            String adType = convertAdType(showtype); //对应内部 广告类型
//            String stringSet = null;//文件类型列表
//            String deviceId = null;//设备号
//
//
//            if (StringUtils.isBlank(adType)) {
//                response = "没有对应的广告类型";
//                return response;
//
//            }
//            //设备的设备号：用于匹配数盟库中的数据
//            if (userDevice != null) {
//                if ("ios".equals(userDevice.getOs().toLowerCase())) {
//                    deviceId = userDevice.getExt().getIdfa();
//                } else if ("android".equalsIgnoreCase(userDevice.getOs().toLowerCase())) {
////                    deviceId = userDevice.getExt().getMac();
//                    deviceId = userDevice.getDidmd5();
//                } else if ("wp".equals(userDevice.getOs().toLowerCase())) {
////                    deviceId = userDevice.getExt().getMac();
//                    deviceId = userDevice.getDidmd5();
//                }
//            }
//            //支持的文件类型
//            List<LJAssets> assets = new ArrayList<>();
//            if ("banner".equals(adType)) {// banner 类型
//                width = userImpression.getBanner().getW();
//                height = userImpression.getBanner().getH();
//                String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
//                stringSet = Arrays.toString(mimes);
//
//            } else if ("fullscreen".equals(adType)) { //开屏
//                if (userImpression.getVideo() != null) {
//                    width = userImpression.getVideo().getW();
//                    height = userImpression.getVideo().getH();
//                    String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
//                    stringSet = Arrays.toString(mimes);
//                } else if (userImpression.getBanner() != null) {
//                    width = userImpression.getBanner().getW();
//                    height = userImpression.getBanner().getH();
//                    String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
//                    stringSet = Arrays.toString(mimes);
//                }
//            } else if ("interstitial".equals(adType)) {//插屏
//                if (userImpression.getVideo() != null) {
//                    width = userImpression.getVideo().getW();
//                    height = userImpression.getVideo().getH();
//                    String[] mimes = userImpression.getVideo().getMimes();//文件扩展名列表
//                    stringSet = Arrays.toString(mimes);
//                } else if (userImpression.getBanner() != null) {
//                    width = userImpression.getBanner().getW();
//                    height = userImpression.getBanner().getH();
//                    String[] mimes = userImpression.getBanner().getMimes();//文件扩展名列表
//                    stringSet = Arrays.toString(mimes);
//                }
//
//            } else if ("feed".equals(adType)) { //信息流
//                assets = userImpression.getNativead().getAssets();
//                for (LJAssets asset : assets) {
//                    if (asset.getImg() != null && asset.getRequired().equals(true)) {
//                        width = asset.getImg().getW();
//                        height = asset.getImg().getH();
//                        stringSet = Arrays.toString(asset.getImg().getMimes());
//                    } else if (asset.getVideo() != null && asset.getRequired().equals(true)) {
//                        width = asset.getVideo().getW();
//                        height = asset.getVideo().getH();
//                        stringSet = Arrays.toString(asset.getVideo().getMimes());
//                    }
//
//                }
//            }
//
//
//            //初步过滤规则开关
//            if (Boolean.valueOf(configs.getString("FILTER_SWITCH"))) {
//                if (FilterRule.filterRuleBidRequest(bidRequestBean, true, msg, ADX_NAME)) {
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
//                            userDevice.getIp()//用户ip
//                    );
//                    if (targetDuFlowBean == null) {
//                        response = "";
//                        return response;
//                    }
//                    MDC.put("sift", configs.getString("ADX_REQUEST"));
//                    //需要添加到Phoenix中的数据
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
//                    if (app.getExt() != null) {
//                        targetDuFlowBean.setAppVersion(app.getExt().getSdk() == null ? "" : app.getExt().getSdk());//APP版本
//                    }
//
//                    log.debug("过滤通过的targetDuFlowBean:{}", targetDuFlowBean);
//                    BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, adType, assets);
//                    MDC.remove("sift");
////                    pushRedis(targetDuFlowBean);//上传到redis服务器
//                    response = JSON.toJSONString(bidResponseBean);
//                    log.debug("过滤通过的bidResponseBean:{}", response);
//                } else {
//                    response = JSON.toJSONString(msg);//过滤规则结果输出
//                }
//
//            } else {
//                DUFlowBean targetDuFlowBean = ruleMatching.match(
//                        deviceId,//设备mac的MD5
//                        adType,//广告类型
//                        width,//广告位的宽
//                        height,//广告位的高
//                        true,// 是否要求分辨率
//                        5,//宽误差值
//                        5,// 高误差值;
//                        ADX_ID,//ADX 服务商ID
//                        stringSet,//文件扩展名
//                        userDevice.getIp()//用户ip
//                );
//                if (targetDuFlowBean == null) {
//                    response = "";
//                    return response;
//                }
//                MDC.put("sift", "dsp-server");
//                log.debug("bidRequestBean.id:{}", bidRequestBean.getId());
//                //需要添加到Phoenix中的数据
//                targetDuFlowBean.setRequestId(bidRequestBean.getId());//bidRequest id
//                targetDuFlowBean.setImpression(bidRequestBean.getImp());//曝光id
//                targetDuFlowBean.setAdxSource(ADX_NAME);//ADX服务商渠道
//                targetDuFlowBean.setAdTypeId(adType);//广告大类型ID
//                targetDuFlowBean.setAdxAdTypeId(showtype);//广告小类对应ADX服务商的ID
//                targetDuFlowBean.setAdxId(ADX_ID);//ADX广告商id
//                targetDuFlowBean.setBidid(MD5Util.MD5(MD5Util.MD5(bidRequestBean.getId())));//bid id
//                targetDuFlowBean.setDspid(LocalDateTime.now().toString() + UUID.randomUUID());//dsp id
//                targetDuFlowBean.setAppName(app.getName());//APP名称
//                targetDuFlowBean.setAppPackageName(app.getBundle());//APP包名
//                targetDuFlowBean.setAppId(app.getId());//APP包名
//                if (app.getExt() != null) {
//                    targetDuFlowBean.setAppVersion(app.getExt().getSdk() == null ? "" : app.getExt().getSdk());//APP版本
//                }
//
//
//                log.debug("没有过滤的targetDuFlowBean:{}", targetDuFlowBean);
//                BidResponseBean bidResponseBean = convertBidResponse(targetDuFlowBean, adType, assets);
//                MDC.remove("sift");
////                pushRedis(targetDuFlowBean);//上传到redis服务器
//                response = JSON.toJSONString(bidResponseBean);
//                MDC.put("sift", "dsp-server");
//                log.debug("没有过滤的bidResponseBean:{}", response);
//            }
            return response;
        } else {
            return response;
        }
    }

    @Override
    public String parseRequest(byte[] dataStr) {
        try {
            BidserverSsp.BidRequest bidRequest = BidserverSsp.BidRequest.parseFrom(dataStr);
            log.debug("SessionId:{}",bidRequest.getSessionId());
            log.debug("dataStr:{}",new String (dataStr));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 内部流转DUFlowBean  转换为  BidResponseBean 输出给 ADX服务器
     *
     * @param duFlowBean
     * @return
     */
    private YouYiBidResponse convertBidResponse(DUFlowBean duFlowBean, YouYiBidRequest bidRequestBean) {
        YouYiBidResponse bidResponseBean = new YouYiBidResponse();
        bidResponseBean.setSession_id(bidRequestBean.getSession_id());
        List<YouYiAd> youYiAds=new ArrayList<>();
        YouYiAd youYiAd =new YouYiAd();
        youYiAd.setAdz_id("");
        youYiAd.setAdz_array_id(0);
//        youYiAd.setBid_price();
//        youYiAd.setAdz_id();
//        youYiAd.setAdz_id();
//        youYiAds.add();
        bidResponseBean.setAds(youYiAds);
        return bidResponseBean;

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
