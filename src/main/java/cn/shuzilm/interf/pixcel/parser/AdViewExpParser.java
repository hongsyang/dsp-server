package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.adview.request.BidRequestBean;
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

import java.util.ArrayList;
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
public class AdViewExpParser implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(AdViewExpParser.class);

    private static final String PIXEL_CONFIG = "pixel.properties";

    @Override
    public String parseUrl(String url) {
        AppConfigs configs = AppConfigs.getInstance(PIXEL_CONFIG);
        DUFlowBean duFlowBean = new DUFlowBean();
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        MDC.put("sift", "AdViewWurl");
        log.debug("AdViewExp曝光的wurl值:{}", urlRequest);
        String requestId = urlRequest.get("req");
        Jedis jedis = JedisManager.getInstance().getResource();
        String elementJson = jedis.get(requestId);
        DUFlowBean element = JSON.parseObject(elementJson, DUFlowBean.class);//json转换为对象
//        DUFlowBean element = (DUFlowBean) JedisQueueManager.getElementFromQueue(urlRequest.get("req"));
        log.debug("AdViewExp曝光的requestid:{},wurl值:{}:[]", requestId, element);
        MDC.put("sift", "pixel");
        AdPixelBean bean = new AdPixelBean();
        if (element != null) {
            bean.setAdUid(element.getAdUid());
        }
        bean.setHost(configs.getString("host"));
        bean.setMoney(Float.valueOf(urlRequest.get("price")));
        bean.setWinNoticeNums(1);
        //pixel服务器发送到主控模块
        log.debug("pixel服务器发送到主控模块的AdPixelBean：{}", bean);
        PixelFlowControl.getInstance().sendStatus(bean);

        //pixel服务器发送到Phoenix
        element.setInfoId(urlRequest.get("req") + UUID.randomUUID());
        element.setRequestId(requestId);
        MDC.put("sift", "AdViewExp");
        log.debug("\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", element.getInfoId(),
                element.getDid(), element.getDeviceId(),
                element.getAdUid(), element.getAdvertiserUid(),
                element.getAdvertiserUid(), element.getAgencyUid(),
                element.getCreativeUid(), element.getProvince(),
                element.getCity(), element.getRequestId());
        JedisQueueManager.putElementToQueue("AdViewExp", element, Priority.MAX_PRIORITY);

        String duFlowBeanJson = JSON.toJSONString(duFlowBean);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return duFlowBeanJson;
    }
}