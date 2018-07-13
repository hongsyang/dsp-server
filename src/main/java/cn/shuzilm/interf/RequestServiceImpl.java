package cn.shuzilm.interf;

import cn.shuzilm.bean.BidRequestBean;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceImpl implements RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceImpl.class);

    @Override
    public BidRequestBean parseRequest(String dataStr) {
        log.debug(" BidRequest参数入参：{}", dataStr);
//        if (StringUtils.isNotBlank(dataStr)) {
            JSONObject jsonObject = JSONObject.fromObject(dataStr);
            log.debug(" jsonObject的结果：{}", jsonObject);
            BidRequestBean bidRequestBean = (BidRequestBean) JSONObject.toBean(jsonObject, BidRequestBean.class);
            log.debug(" json转化为BidRequestBean的结果：{}", bidRequestBean);
            return bidRequestBean;
//        }
//        return null;

    }
}
