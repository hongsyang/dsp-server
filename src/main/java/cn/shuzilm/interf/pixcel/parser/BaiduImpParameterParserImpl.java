package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.RedisUtil;
import cn.shuzilm.util.SSDBUtil;
import cn.shuzilm.util.UrlParserUtil;
import cn.shuzilm.util.baidudecode.BaiduPriceDecryptUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: 百度曝光解析
 * @Author: houkp
 * @CreateDate: 2019/4/9 10:41
 * @UpdateUser: houkp
 * @UpdateDate: 2019/4/9 10:41
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class BaiduImpParameterParserImpl {


    private static final Logger log = LoggerFactory.getLogger(BaiduImpParameterParserImpl.class);


    private static PixelFlowControl pixelFlowControl = PixelFlowControl.getInstance();

    private static final String PIXEL_CONFIG = "pixel.properties";

    private static AppConfigs configs = AppConfigs.getInstance(PIXEL_CONFIG);

    public static String parseUrlStr(String url) {
        MDC.put("sift", "BaiduImp");
        log.debug("BaiduImp曝光的url值:{}", url);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        log.debug("BaiduImp曝光转换之后的url值:{}", urlRequest);

        DUFlowBean element = new DUFlowBean();
        String requestId = urlRequest.get("reqid");
        try {
            if (SSDBUtil.getDUFlowBean(requestId) != null) {
                element = SSDBUtil.getDUFlowBean(requestId);
            } else {
                element.setInfoId(requestId + UUID.randomUUID());//2019年03月27号 现阶段不用
                element.setRequestId(requestId);//请求id
                element.setBidid(urlRequest.get("bidid"));//去重id
                //点击 不计算价格

                String act = urlRequest.get("act");
                element.setWinNoticeTime(new Date().getTime());
                String did = urlRequest.get("device");//数盟设备id
                element.setDid(did);
                String device = urlRequest.get("device");//设备id
                element.setDeviceId(device);
                String appn = urlRequest.get("appn").equals("null") ? "" : urlRequest.get("appn");//App包名
                element.setAppPackageName(appn);
                //溢价系数
                String premiumFactor = urlRequest.get("pf");//溢价系数
                element.setPremiumFactor(Double.valueOf(premiumFactor));


                String ddem = urlRequest.get("ddem").equals("null") ? "" : urlRequest.get("ddem");//人群ID
                element.setAudienceuid(ddem);
                String dcuid = urlRequest.get("dcuid").equals("null") ? "" : urlRequest.get("dcuid");//创意id
                element.setCreativeUid(dcuid);
                String dbidp = urlRequest.get("dbidp").equals("null") ? "" : urlRequest.get("dbidp");//广告主出价
                element.setBiddingPrice(Double.valueOf(dbidp));
                String dade = urlRequest.get("dade").equals("null") ? "" : urlRequest.get("dade");//广告主ID
                element.setAdvertiserUid(dade);
                String dage = urlRequest.get("dage").equals("null") ? "" : urlRequest.get("dage");//代理商ID
                element.setAgencyUid(dage);
                String daduid = urlRequest.get("daduid").equals("null") ? "" : urlRequest.get("daduid");//广告ID
                element.setAdUid(daduid);
                String dmat = urlRequest.get("dmat").equals("null") ? "" : urlRequest.get("dmat");//素材id
                element.setMaterialId(dmat);
                if (urlRequest.get("dpro") != null && urlRequest.get("dcit") != null && urlRequest.get("dcou") != null) {
                    String dpro = urlRequest.get("dpro").equals("null") ? "" : urlRequest.get("dpro");//省
                    element.setProvince(dpro);
                    String dcit = urlRequest.get("dcit").equals("null") ? "" : urlRequest.get("dcit");//市
                    element.setCity(dcit);
                    String dcou = urlRequest.get("dcou").equals("null") ? "" : urlRequest.get("dcou");//县
                    element.setCountry(dcou);
                }
                String userip = urlRequest.get("userip").equals("null") ? "" : urlRequest.get("userip");//用户ip
                element.setIpAddr(userip);
                element.setAdxId("5");
                element.setAdxSource("Baidu");
                //--------------------------------------------------不一定传过来，从redis中获取，如果没有就不要了

                if (RedisUtil.getDUFlowBean(requestId) != null) {
                    DUFlowBean duFlowBean = RedisUtil.getDUFlowBean(requestId);
                    List<Impression> list = new ArrayList();
                    Impression impression = new Impression();
                    impression.setId(duFlowBean.getImpression().get(0).getId());
                    list.add(impression);
                    element.setImpression(list);
                    String app = duFlowBean.getAppName().equals("null") ? "" : urlRequest.get("app");
                    element.setAppName(app);
                    String appv = duFlowBean.getAppVersion().equals("null") ? "" : urlRequest.get("appv");
                    element.setAppVersion(appv);
                    String dpro = duFlowBean.getProvince().equals("null") ? "" : urlRequest.get("dpro");
                    element.setProvince(dpro);
                    String dcit = duFlowBean.getCity().equals("null") ? "" : urlRequest.get("dcit");
                    element.setCity(dcit);
                    String dcou = duFlowBean.getCountry().equals("null") ? "" : urlRequest.get("dcou");
                    element.setCountry(dcou);
                    String pmp = duFlowBean.getDealid().equals("null") ? "" : urlRequest.get("pmp");
                    element.setDealid(pmp);

                } else {
                    List<Impression> list = new ArrayList();
                    Impression impression = new Impression();
                    impression.setId("");
                    list.add(impression);
                    element.setImpression(list);
                    element.setAppName("");
                    element.setAppVersion("");
                    element.setProvince("");
                    element.setCity("");
                    element.setCountry("");
                    element.setDealid("");
                }
            }
            log.debug("BaiduImp曝光的requestid:{},element对象:{}", requestId, element);
            MDC.put("sift", "pixel");
            AdPixelBean bean = new AdPixelBean();
            if (element != null) {
                bean.setAdUid(element.getAdUid());
            }
            bean.setPremiumFactor(element.getPremiumFactor());
            bean.setHost(configs.getString("HOST"));
            bean.setRequestId(requestId);//请求id
            bean.setBidPrice(element.getBiddingPrice());//广告主出价
            //解析价格
            String price = urlRequest.get("price");
            int decodePrice = BaiduPriceDecryptUtil.decodePrice(price);
            bean.setCost(Double.valueOf(Integer.valueOf(decodePrice)) / 100);
            bean.setWinNoticeNums(1);
            //pixel服务器发送到主控模块
            log.debug("pixel服务器发送到主控模块的BaiduImpBean：{}", bean);
            AdPixelBean adPixelBean = pixelFlowControl.sendStatus(bean);//价格返回结果

            //pixel服务器发送到Phoenix
            element.setInfoId(urlRequest.get("id") + UUID.randomUUID());
            element.setRequestId(requestId);
            element.setActualPrice(Double.valueOf(Integer.valueOf(decodePrice)) / 100);//成本价
            element.setActualPricePremium(adPixelBean.getFinalCost());//最终价格
            element.setOurProfit(adPixelBean.getDspProfit());//dsp利润
            element.setAgencyProfit(adPixelBean.getRebateProfit());//代理商利润
            MDC.put("sift", "BaiduImp");
            element.setWinNoticeTime(new Date().getTime());
            log.debug("发送到Phoenix的DUFlowBean:{}", element);
            MDC.put("phoenix", "Exp");
            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                    element.getInfoId(), new Date().getHours(),
                    new Date().getTime(), LocalDateTime.now().toString(),
                    element.getDid(), element.getDeviceId(),
                    element.getAdUid(), element.getAudienceuid(),
                    element.getAgencyUid(), element.getAdvertiserUid(),
                    element.getCreativeUid(), element.getProvince(),
                    element.getCity(), element.getActualPricePremium(),
                    element.getBiddingPrice(), element.getActualPrice(),
                    element.getAgencyProfit(), element.getOurProfit(),
                    element.getAdxId(), element.getAppName(),
                    element.getAppPackageName(), element.getAppVersion(),
                    element.getRequestId(), element.getImpression().get(0).getId(),
                    element.getDealid(), element.getAppId(), element.getBidid(),
                    price, element.getIpAddr(), urlRequest.get("remoteIp"),
                    element.getMaterialId());

            MDC.remove("phoenix");
            MDC.put("sift", "BaiduImp");
            boolean lingJiClick = RedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
            if (lingJiClick) {
                log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiClick);
            } else {
                log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiClick);
                throw new RuntimeException();
            }
        } catch (Exception e) {
            Help.sendAlert("发送到" + configs.getString("HOST") + "失败,BaiduImp");
            MDC.put("sift", "exception");
            boolean exp_error = RedisQueueManager.putElementToQueue("EXP_ERROR", element, Priority.MAX_PRIORITY);
            log.debug("发送到EXP_ERROR队列：{}", exp_error);
            log.debug("element:{}", JSON.toJSONString(element));
            log.error("异常信息:{}", e);
            MDC.remove("sift");
        }

        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return requestId;
    }
}
