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
            log.debug("LingJiExp曝光的requestid:{},nurl值:{}:[]", requestId, element);
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
            //pixel服务器发送到主控模块
            log.debug("pixel服务器发送到主控模块的LingJiExpBean：{}", bean);
            PixelFlowControl.getInstance().sendStatus(bean);

            //pixel服务器发送到Phoenix
            element.setInfoId(urlRequest.get("id") + UUID.randomUUID());
            element.setRequestId(requestId);
            element.setActualPricePremium(money);//成本价
            element.setActualPricePremium(money * element.getPremiumFactor());//溢价
            element.setWinNoticeTime(Long.valueOf(split[1]));//设置对账时间
            element.setAdxSource("LingJi");
            MDC.put("sift", "LingJiExp");
            log.debug("\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", element.getInfoId(),
                    element.getDid(), element.getDeviceId(),
                    element.getAdUid(), element.getAdvertiserUid(),
                    element.getAdvertiserUid(), element.getAgencyUid(),
                    element.getCreativeUid(), element.getProvince(),
                    element.getCity(), element.getRequestId(),
                    element.getActualPrice(), element.getBiddingPrice(),
                    element.getWinNoticeTime(), element.getPremiumFactor());
            boolean lingJiExp = JedisQueueManager.putElementToQueue("EXP", element, Priority.MAX_PRIORITY);
            if (lingJiExp) {
                log.debug("发送到Phoenix：{}", lingJiExp);
            } else {
                log.debug("发送到Phoenix：{}", lingJiExp);
            }

        } catch (Exception e) {
            log.error("redis获取失败或者超时 ，异常：{}", e);
        }
        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return duFlowBeanJson;
    }
}