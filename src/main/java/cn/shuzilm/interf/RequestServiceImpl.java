package cn.shuzilm.interf;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    @Override
    public BidRequestBean parseRequest(String dataStr) {
        MDC.put("sift","interface");
        log.debug(" BidRequest参数入参：{}", dataStr);
        if (StringUtils.isNotBlank(dataStr)) {
            BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
            log.debug(" json转化为BidRequestBean的结果：{}", bidRequestBean);
            MDC.remove("sift");
            return bidRequestBean;
        }
        return null;

    }
}
