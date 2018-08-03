package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
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

import java.util.Map;
import java.util.UUID;

/**
* @Description:    ClickParser 点击解析
* @Author:         houkp
* @CreateDate:     2018/7/19 18:44
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 18:44
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class AdViewClickParameterParserImpl implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(AdViewClickParameterParserImpl.class);

    private AppConfigs configs = null;

    private static final String PIXEL_CONFIG = "pixel.properties";

    @Override
    public String parseUrl(String url) {
        this.configs= AppConfigs.getInstance(PIXEL_CONFIG);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        MDC.put("sift", "AdViewClick");
        log.debug("AdViewClick曝光的curl值:{}", urlRequest);
        String requestId = urlRequest.get("id");
        Jedis jedis = JedisManager.getInstance().getResource();
        String elementJson = jedis.get(requestId);
        DUFlowBean element = JSON.parseObject(elementJson, DUFlowBean.class);//json转换为对象
        try {
            log.debug("AdViewClick曝光的requestid:{},curl值:{}:[]", requestId, element);
            MDC.put("sift", "pixel");
            AdPixelBean bean = new AdPixelBean();
            if (element != null) {
                bean.setAdUid(element.getAdUid());
            }
            bean.setHost(configs.getString("HOST"));
            bean.setMoney(Float.valueOf(urlRequest.get("price")));
            bean.setWinNoticeNums(1);
            //pixel服务器发送到主控模块
            log.debug("pixel服务器发送到主控模块的AdViewClickBean：{}", bean);
            PixelFlowControl.getInstance().sendStatus(bean);

            //pixel服务器发送到Phoenix
            element.setInfoId(urlRequest.get("id") + UUID.randomUUID());
            element.setRequestId(requestId);
            MDC.put("sift", "AdViewClick");
            log.debug("\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", element.getInfoId(),
                    element.getDid(), element.getDeviceId(),
                    element.getAdUid(), element.getAdvertiserUid(),
                    element.getAdvertiserUid(), element.getAgencyUid(),
                    element.getCreativeUid(), element.getProvince(),
                    element.getCity(), element.getRequestId());
            boolean lingJiClick = JedisQueueManager.putElementToQueue("AdViewClick", element, Priority.MAX_PRIORITY);
            if (lingJiClick) {
                log.debug("发送到Phoenix：{}", lingJiClick);
            } else {
                log.debug("发送到Phoenix：{}", lingJiClick);
            }
        }catch (Exception e){
            log.error("redis获取失败或者超时 ，异常：{}",e);
        }
        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return duFlowBeanJson;
    }
}
