package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.UrlParserUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: ClickParser 点击解析
 * @Author: houkp
 * @CreateDate: 2018/7/19 18:44
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 18:44
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiClickParameterParserImpl implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(LingJiClickParameterParserImpl.class);

    private AppConfigs configs = null;

    private static final String PIXEL_CONFIG = "pixel.properties";

    @Override
    public String parseUrl(String url) {
        configs = AppConfigs.getInstance(PIXEL_CONFIG);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        MDC.put("sift", "LingJiClick");
        log.debug("LingJiClick点击的curl值:{}", urlRequest);
        DUFlowBean element = new DUFlowBean();

        String requestId = urlRequest.get("id");
        element.setInfoId(requestId+ UUID.randomUUID());

        element.setBidid(urlRequest.get("bidid"));

        String impid = urlRequest.get("impid");
        List<Impression> list =new ArrayList();
        Impression impression = new Impression();
        element.setImpression(list);
        impression.setId(impid);
        list.add(impression);


//        String act = urlRequest.get("act");
//        element.setHour(Integer.valueOf(act));

        String adx = urlRequest.get("adx");
        element.setAdxId(adx);

        String did = urlRequest.get("did");
        element.setDid(did);

        String device = urlRequest.get("device");
        element.setDeviceId(device);

        String app = urlRequest.get("app");
        element.setAppName(app);
        String appn = urlRequest.get("appn");
        element.setAppPackageName(appn);
        String appv = urlRequest.get("appv");
        element.setAppVersion(appv);
        String ddem = urlRequest.get("ddem");
        element.setDemographicTagId(ddem);
        String dcuid = urlRequest.get("dcuid");
        element.setCreativeUid(dcuid);
        String dpro = urlRequest.get("dpro");
        element.setProvince(dpro);
        String dcit = urlRequest.get("dcit");
        element.setCity(dcit);
        String dcou = urlRequest.get("dcou");
        element.setCountry(dcou);
        String dade = urlRequest.get("dade");
        element.setAdvertiserUid(dade);
        String dage = urlRequest.get("dage");
        element.setAgencyUid(dage);
        String daduid = urlRequest.get("daduid");
        element.setAdUid(daduid);
        String pmp = urlRequest.get("pmp");
        element.setDealid(pmp);

        element.setAdxSource("LingJi");

        try {
            log.debug("LingJiClick点击的requestid:{},element值:{}:[]", requestId, element);
            MDC.put("sift", "pixel");
            AdPixelBean bean = new AdPixelBean();
            if (element != null) {
                bean.setAdUid(element.getAdUid());
            }
            bean.setHost(configs.getString("HOST"));
            bean.setClickNums(1);
            bean.setClickTime(new Date().getTime());
            bean.setType(1);
            //pixel服务器发送到主控模块
            log.debug("pixel服务器发送到主控模块的LingJiClickBean：{}", bean);
            PixelFlowControl.getInstance().sendStatus(bean);

            //pixel服务器发送到Phoenix
            MDC.put("sift", "LingJiClick");
            log.debug("发送到Phoenix的DUFlowBean:{}", element);
            MDC.put("phoenix", "Click");
            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}",
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
                    element.getRequestId(), element.getImpression().get(0).getId(), element.getDealid());
            MDC.remove("phoenix");
        } catch (Exception e) {
            log.error("异常信息：{}", e);
        }
        boolean lingJiClick = JedisQueueManager.putElementToQueue("CLICK", element, Priority.MAX_PRIORITY);
        if (lingJiClick) {
            log.debug("发送到Phoenix：{}", lingJiClick);
        } else {
            log.debug("发送到Phoenix：{}", lingJiClick);
        }
        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return requestId;
    }
}
