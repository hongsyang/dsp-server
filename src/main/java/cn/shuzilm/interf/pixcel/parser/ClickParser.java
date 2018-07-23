package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.util.UrlParserUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
public class ClickParser implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(ClickParser.class);

    @Override
    public String parseUrl(String url) {
        MDC.put("sift","rtb");
        DUFlowBean duFlowBean = new DUFlowBean();
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        log.debug("urlRequest:{}",urlRequest);
        duFlowBean.setInfoId( urlRequest.get("houkp")+ UUID.randomUUID());
        String duFlowBeanJson = JSON.toJSONString(duFlowBean);
        log.debug("duFlowBeanJson:{}",duFlowBeanJson);
        MDC.remove("sift");
        return duFlowBeanJson;

    }
}
