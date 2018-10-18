package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.UrlParserUtil;
import cn.shuzilm.util.aes.AES;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import javax.xml.crypto.Data;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

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
        String requestId = urlRequest.get("id");
        Jedis jedis = JedisManager.getInstance().getResource();
        String elementJson = jedis.get(requestId);
        DUFlowBean element = JSON.parseObject(elementJson, DUFlowBean.class);//json转换为对象
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
                            "\t{}\t{}\t{}\t{}\t{}",
                    element.getInfoId(), element.getHour(),
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
                    element.getRequestId(), element.getImpression().get(0).getId(), element.getDealid());
            MDC.remove("phoenix");

        } catch (Exception e) {
            log.error("异常信息：{}", e);
        } finally {
            jedis.close();
        }
        boolean lingJiExp = JedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
        MDC.put("sift", "LingJiExp");
        if (lingJiExp) {
            log.debug("发送到Phoenix：{}", lingJiExp);
        } else {
            log.debug("发送到Phoenix：{}", lingJiExp);
        }
        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return requestId;
    }
}