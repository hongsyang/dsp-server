package cn.shuzilm.interf;

import cn.shuzilm.bean.BidRequestBean;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

public class RequestServiceImpl implements RequestService {
    @Override
    public BidRequestBean parseRequest(String dataStr) {
        if (StringUtils.isNotBlank(dataStr)) {
            JSONObject jsonObject = JSONObject.fromObject(dataStr);
            BidRequestBean bidRequestBean = (BidRequestBean) JSONObject.toBean(jsonObject, BidRequestBean.class);
            return bidRequestBean;
        }
        return null;

    }
}
