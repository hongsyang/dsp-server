package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.UrlParserUtil;
import cn.shuzilm.util.base64.AdViewDecodeUtil;
import cn.shuzilm.util.base64.Decrypter;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @Description: ExposureParser  曝光量解析
 * @Author: houkp
 * @CreateDate: 2018/7/19 15:57
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 15:57
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AdViewExpParameterParserImpl implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(AdViewExpParameterParserImpl.class);

    private AppConfigs configs = null;

    private static PixelFlowControl pixelFlowControl = PixelFlowControl.getInstance();

    private static final String PIXEL_CONFIG = "pixel.properties";

    @Override
    public String parseUrl(String url) {
        this.configs = AppConfigs.getInstance(PIXEL_CONFIG);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        MDC.put("sift", "AdViewExp");
        log.debug("AdViewExp曝光的wurl值:{}", urlRequest);
        String requestId = urlRequest.get("id");
        if (requestId != null) {
            Jedis jedis = JedisManager.getInstance().getResource();
            String elementJson = jedis.get(requestId);
            DUFlowBean element = JSON.parseObject(elementJson, DUFlowBean.class);//json转换为对象
            try {
                log.debug("AdViewExp曝光的requestid:{},element对象:{}", requestId, elementJson);
                MDC.put("sift", "pixel");
                AdPixelBean bean = new AdPixelBean();
                if (element != null) {
                    bean.setAdUid(element.getAdUid());
                }
                bean.setPremiumFactor(element.getPremiumFactor());
                bean.setHost(configs.getString("HOST"));
                String price = urlRequest.get("price");
                Long priceLong = AdViewDecodeUtil.priceDecode(price, configs.getString("EKEY"), configs.getString("IKEY"));
                bean.setCost(Double.valueOf(priceLong) / 10000);
                bean.setWinNoticeNums(1);
                //pixel服务器发送到主控模块
                log.debug("pixel服务器发送到主控模块的AdViewExpBean：{}", bean);
                AdPixelBean adPixelBean = pixelFlowControl.sendStatus(bean);//价格返回结果

                //pixel服务器发送到Phoenix
                element.setInfoId(urlRequest.get("id") + UUID.randomUUID());
                element.setRequestId(requestId);
                element.setActualPrice(Double.valueOf(priceLong) / 10000);//成本价
                element.setActualPricePremium(adPixelBean.getFinalCost());//最终价格
                element.setOurProfit(adPixelBean.getDspProfit());//dsp利润
                element.setAgencyProfit(adPixelBean.getRebateProfit());//代理商利润
                MDC.put("sift", "AdViewExp");
                log.debug("发送到Phoenix的DUFlowBean:{}", elementJson);
                MDC.put("phoenix", "Exp");
                log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                                "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                                "\t{}\t{}\t{}\t{}\t{}\t{}",
                        element.getInfoId(), element.getHour(),
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
                        element.getDealid(), element.getAppId());

                MDC.remove("phoenix");
                MDC.put("sift", "AdViewExp");
                boolean lingJiClick = JedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
                if (lingJiClick) {
                    log.debug("发送elemen :{}到Phoenix是否成功：{}", elementJson, lingJiClick);
                } else {
                    log.debug("发送elemen :{}到Phoenix是否成功：{}", elementJson, lingJiClick);
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                Help.sendAlert("pixcel异常触发报警:AdViewExp");
                MDC.put("sift", "exception");
                boolean exp_error = JedisQueueManager.putElementToQueue("EXP_ERROR", element, Priority.MAX_PRIORITY);
                log.debug("发送到EXP_ERROR队列：{}", exp_error);
                log.debug("element{}", elementJson);
                log.error("异常信息：{}", e);
                MDC.remove("sift");
            } finally {
                jedis.close();
            }

            String duFlowBeanJson = JSON.toJSONString(element);
            log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        }
        return requestId;
    }
}
