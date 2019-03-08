package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.util.UrlParserUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: RtbRequestParser  post参数解析
 * @Author: houkp
 * @CreateDate: 2018/7/20 14:23
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/20 14:23
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class RtbRequestParser {

    private static final Logger log = LoggerFactory.getLogger(RtbRequestParser.class);

    private static RequestService requestService ;


    private static final String FILTER_CONFIG = "filter.properties";
    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);



    /**
     * 根据厂商解析post参数
     *
     * @param url
     * @param dataStr
     * @param remoteIp
     * @return
     */
    public String parseData(String url, String dataStr, String remoteIp, ConcurrentHashMap<String, Object> requestParser) throws Exception {
        String responseStr="";
        log.debug("requestParser.get:{}", requestParser.get(url));
        if (requestParser.get(url) != null) {
            requestService = RequestServiceFactory.getRequestService(requestParser.get(url).toString());
            responseStr = requestService.parseRequest(dataStr);
        }
        return responseStr;
    }


}
