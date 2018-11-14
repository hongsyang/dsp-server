package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.MD5Util;
import cn.shuzilm.util.UrlParserUtil;
import cn.shuzilm.util.aes.AES;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import javax.xml.crypto.Data;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: LingJiExpParameterParserImpl  曝光量解析
 * @Author: houkp
 * @CreateDate: 2018/7/19 15:57
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 15:57
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiExpParameterParserImpl implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(LingJiExpParameterParserImpl.class);

    private static final String PIXEL_CONFIG = "pixel.properties";


    private static PixelFlowControl pixelFlowControl = PixelFlowControl.getInstance();


    private AppConfigs configs = null;

    @Override
    public String parseUrl(String url) {
        configs = AppConfigs.getInstance(PIXEL_CONFIG);
        MDC.put("sift", "LingJiExp");
        log.debug("LingJiExp曝光的nurl值:{}", url);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        log.debug("LingJiExp转换之后曝光的nurl值:{}", urlRequest);
        DUFlowBean element = new DUFlowBean();

        String requestId = urlRequest.get("id");
        element.setInfoId(requestId + UUID.randomUUID());
        element.setRequestId(requestId);
        element.setBidid(urlRequest.get("bidid"));

        String impid = urlRequest.get("impid");
        List<Impression> list = new ArrayList();
        Impression impression = new Impression();
        element.setImpression(list);
        impression.setId(impid);
        list.add(impression);


        String act = urlRequest.get("act");
        element.setWinNoticeTime(Long.valueOf(act));

        String adx = urlRequest.get("adx");
        element.setAdxId(adx);

        String did = urlRequest.get("did");
        element.setDid(did);

        String device = urlRequest.get("device");
        element.setDeviceId(device);

        String app = urlRequest.get("app").equals(null) ? "" : urlRequest.get("app");
        element.setAppName(URLDecoder.decode(app));
        String appn = urlRequest.get("appn").equals("null") ? "" : urlRequest.get("appn");
        element.setAppPackageName(appn);
        String appv = urlRequest.get("appv").equals("null") ? "" : urlRequest.get("appv");
        element.setAppVersion(appv);
        String ddem = urlRequest.get("ddem").equals("null") ? "" : urlRequest.get("ddem");
        element.setAudienceuid(ddem);
        String dcuid = urlRequest.get("dcuid").equals("null") ? "" : urlRequest.get("dcuid");
        element.setCreativeUid(dcuid);
        String dpro = urlRequest.get("dpro").equals("null") ? "" : urlRequest.get("dpro");
        element.setProvince(dpro);
        String dcit = urlRequest.get("dcit").equals("null") ? "" : urlRequest.get("dcit");
        element.setCity(dcit);
        String dcou = urlRequest.get("dcou").equals("null") ? "" : urlRequest.get("dcou");
        element.setCountry(dcou);
        String dade = urlRequest.get("dade").equals("null") ? "" : urlRequest.get("dade");
        element.setAdvertiserUid(dade);
        String dage = urlRequest.get("dage").equals("null") ? "" : urlRequest.get("dage");
        element.setAgencyUid(dage);
        String daduid = urlRequest.get("daduid").equals("null") ? "" : urlRequest.get("daduid");
        element.setAdUid(daduid);
        String pmp = urlRequest.get("pmp").equals("null") ? "" : urlRequest.get("pmp");
        element.setDealid(pmp);
        String userip = urlRequest.get("userip").equals("null") ? "" : urlRequest.get("userip");
        element.setIpAddr(userip);
        String premiumFactor = urlRequest.get("pf");//溢价系数
        element.setPremiumFactor(Double.valueOf(premiumFactor));
        element.setAdxSource("LingJi");

        if (MD5Util.MD5(MD5Util.MD5(requestId)).equals(element.getBidid())) {
            try {
                log.debug("LingJiExp曝光的requestid:{},element值:{}", requestId, element);
                MDC.put("sift", "pixel");
                AdPixelBean bean = new AdPixelBean();
                if (element != null) {
                    bean.setAdUid(element.getAdUid());
                }
                bean.setHost(configs.getString("HOST"));
                String price = urlRequest.get("price");
                String result = AES.decrypt(price, configs.getString("ADX_TOKEN"));
                log.debug("price解析结果：{}", result);
                String[] split = result.split("_");
                Double money = Double.valueOf(split[0]) / 100;
                bean.setCost(money);
                bean.setWinNoticeTime(Long.valueOf(split[1]));//设置对账时间
                bean.setWinNoticeNums(1);
                bean.setPremiumFactor(element.getPremiumFactor());
                bean.setType(0);
                //pixel服务器发送到主控模块
                log.debug("pixel服务器发送到主控模块的LingJiExpBean：{}", bean);
                AdPixelBean adPixelBean = pixelFlowControl.sendStatus(bean);//价格返回结果
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMaximumFractionDigits(5);
                //pixel服务器发送到Phoenix
                element.setInfoId(urlRequest.get("id") + UUID.randomUUID());
                element.setRequestId(requestId);
                element.setActualPrice(money);//成本价
                element.setActualPricePremium(adPixelBean.getFinalCost());//最终价格
                element.setOurProfit(adPixelBean.getDspProfit());//dsp利润
                element.setAgencyProfit(adPixelBean.getRebateProfit());//代理商利润
                element.setWinNoticeTime(Long.valueOf(split[1]));//设置对账时间
                element.setAdxSource("LingJi");
                MDC.put("sift", "LingJiExp");
                log.debug("发送到Phoenix的DUFlowBean:{}", element);
                MDC.put("phoenix", "Exp");
                log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                                "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                                "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                        element.getInfoId(), new Date().getHours(),
                        element.getWinNoticeTime(), LocalDateTime.now().toString(),
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
                        element.getDealid(), element.getAppId(),
                        element.getBidid(),price);

                MDC.remove("phoenix");
                boolean lingJiExp = JedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
                MDC.put("sift", "LingJiExp");
                if (lingJiExp) {
                    log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiExp);
                } else {
                    log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiExp);
                    throw new RuntimeException();
                }

            } catch (Exception e) {
                Help.sendAlert("pixcel异常触发报警:LingJiExp");
                MDC.put("sift", "exception");
                boolean exp_error = JedisQueueManager.putElementToQueue("EXP_ERROR", element, Priority.MAX_PRIORITY);
                log.debug("发送element：{}到EXP_ERROR队列：{}", element, exp_error);
                log.debug("element:{}", element);
                log.error("异常信息：{}", e);
                MDC.remove("sift");
            }
            String duFlowBeanJson = JSON.toJSONString(element);
            log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        } else {
            MDC.put("sift", "repeat");
            log.debug("本次请求requestId:{}；bidid:{}", requestId, element.getBidid());
        }
        return requestId;
    }
}