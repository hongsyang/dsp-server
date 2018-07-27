package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.util.UrlParserUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
* @Description:    ExposureParser  曝光量解析
* @Author:         houkp
* @CreateDate:     2018/7/19 15:57
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 15:57
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class AdViewExpParser  implements  ParameterParser{

    private static final Logger log = LoggerFactory.getLogger(AdViewExpParser.class);


    @Override
    public String parseUrl(String url) {
        DUFlowBean duFlowBean = new DUFlowBean();
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        MDC.put("sift","AdViewWurl");
        log.debug("AdViewExp曝光的wurl值:{}",urlRequest);
        String requestId = urlRequest.get("req");
        Object element = JedisQueueManager.getElementFromQueue(urlRequest.get("req"));
        log.debug("AdViewExp曝光的requestid:{},wurl值:{}:[]",requestId,element);
        MDC.put("sift","pixel");
        AdPixelBean bean = new AdPixelBean();
        bean.setAdName(urlRequest.get(""));
        log.debug("发送到pixel服务器的AdPixelBean：{}",bean);
        PixelFlowControl.getInstance().sendStatus(bean);
        duFlowBean.setInfoId( urlRequest.get("req")+ UUID.randomUUID());
        MDC.put("sift","AdViewExp");
        log.debug("\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", duFlowBean.getInfoId(),
                duFlowBean.getDid(), duFlowBean.getDeviceId(),
                duFlowBean.getAdUid(),duFlowBean.getAdvertiserUid(),
                duFlowBean.getAdvertiserUid(), duFlowBean.getAgencyUid(),
                duFlowBean.getCreativeUid(), duFlowBean.getProvince(),
                duFlowBean.getCity(),duFlowBean.getRequestId());
        String duFlowBeanJson = JSON.toJSONString(duFlowBean);
        log.debug("duFlowBeanJson:{}",duFlowBeanJson);
        return duFlowBeanJson;
    }
}