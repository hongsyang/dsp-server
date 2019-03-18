package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.UrlParserUtil;
import cn.shuzilm.util.tencent.GdtWinPriceDecoder;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: ExposureParser 赢价通知量解析
 * @Author: houkp
 * @CreateDate: 2018/7/19 15:57
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 15:57
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class TencentImpParameterParserImpl implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(TencentImpParameterParserImpl.class);


    private static PixelFlowControl pixelFlowControl = PixelFlowControl.getInstance();

    private static final String PIXEL_CONFIG = "pixel.properties";

    private static AppConfigs configs = AppConfigs.getInstance(PIXEL_CONFIG);

    public static String parseUrlStr(String url) {
        MDC.put("sift", "TencentImp");
        log.debug("TencentImp曝光的url值:{}", url);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        log.debug("TencentImp曝光转换之后的url值:{}", urlRequest);

        DUFlowBean element = new DUFlowBean();
        String requestId = urlRequest.get("impparam");
        try {
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
            element.setWinNoticeTime(new Date().getTime());

            String adx = urlRequest.get("adx");
            element.setAdxId(adx);

            String did = urlRequest.get("did");
            element.setDid(did);

            String device = urlRequest.get("device");
            element.setDeviceId(device);
//            if (urlRequest.get("app")!=null){
//                String app = "null".equals(urlRequest.get("app")) ? "" : urlRequest.get("app");
//                element.setAppName(URLDecoder.decode(app));
//            }
            String appn = urlRequest.get("appn").equals("null") ? "" : urlRequest.get("appn");
            element.setAppPackageName(appn);

//            String appv = urlRequest.get("appv").equals("null") ? "" : urlRequest.get("appv");
//            element.setAppVersion(appv);
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
//            String pmp = urlRequest.get("pmp").equals("null") ? "" : urlRequest.get("pmp");
//            element.setDealid(pmp);
            if (urlRequest.get("dmat")!=null) {
                String dmat = urlRequest.get("dmat").equals("null") ? "" : urlRequest.get("dmat");//
                element.setMaterialId(dmat);//素材id
            }
            String userip = urlRequest.get("userip").equals("null") ? "" : urlRequest.get("userip");
            element.setIpAddr(userip);

            //广告主出价
            if (urlRequest.get("dbidp")!=null) {
                String dbidp = urlRequest.get("dbidp").equals("null") ? "" : urlRequest.get("dbidp");
                element.setBiddingPrice(Double.valueOf(dbidp));
            }
            String premiumFactor = urlRequest.get("pf");//溢价系数
            element.setPremiumFactor(Double.valueOf(premiumFactor));
            element.setAdxSource("Tencent");

            log.debug("TencentImp曝光的requestid:{},element对象:{}", requestId, element);
            MDC.put("sift", "pixel");
            AdPixelBean bean = new AdPixelBean();
            if (element != null) {
                bean.setAdUid(element.getAdUid());
            }
            bean.setPremiumFactor(element.getPremiumFactor());
            bean.setHost(configs.getString("HOST"));
            bean.setRequestId(requestId);//请求id
            bean.setBidPrice(element.getBiddingPrice());//广告主出价
            String price = urlRequest.get("win");
            GdtWinPriceDecoder gdtWinPriceDecoder =new GdtWinPriceDecoder();
            String price_str = gdtWinPriceDecoder.DecodePrice(price, configs.getString("TENCENT_EKEY")).trim();
            bean.setCost(Double.valueOf( Integer.valueOf(price_str)) / 100);
            bean.setWinNoticeNums(1);
            //pixel服务器发送到主控模块
            log.debug("pixel服务器发送到主控模块的TencentImpBean：{}", bean);
            AdPixelBean adPixelBean = pixelFlowControl.sendStatus(bean);//价格返回结果

            //pixel服务器发送到Phoenix
            element.setInfoId(urlRequest.get("id") + UUID.randomUUID());
            element.setRequestId(requestId);
            element.setActualPrice(Double.valueOf( Integer.valueOf(price_str)) / 100);//成本价
            element.setActualPricePremium(adPixelBean.getFinalCost());//最终价格
            element.setOurProfit(adPixelBean.getDspProfit());//dsp利润
            element.setAgencyProfit(adPixelBean.getRebateProfit());//代理商利润
            MDC.put("sift", "TencentImp");
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
                    price,element.getIpAddr(),urlRequest.get("remoteIp"),
                    element.getMaterialId());

            MDC.remove("phoenix");
            MDC.put("sift", "TencentImp");
            boolean lingJiClick = RedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
            if (lingJiClick) {
                log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiClick);
            } else {
                log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiClick);
                throw new RuntimeException();
            }
        } catch (Exception e) {
            Help.sendAlert("发送到" + configs.getString("HOST")+"失败,TencentImp");
            MDC.put("sift", "exception");
            boolean exp_error = RedisQueueManager.putElementToQueue("EXP_ERROR", element, Priority.MAX_PRIORITY);
            log.debug("发送到EXP_ERROR队列：{}", exp_error);
            log.debug("element:{}",JSON.toJSONString(element));
            log.error("异常信息:{}", e);
            MDC.remove("sift");
        }

        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return requestId;
    }

    @Override
    public String parseUrl(String url) {
        return null;
    }
}
