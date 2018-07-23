package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.util.UrlParserUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

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

    /**
     * 根据厂商解析post参数
     * @param url
     * @param dataStr
     * @param remoteIp
     * @return
     */
    public String parseData(String url, String dataStr, String remoteIp) {
        String responseStr = "没有对应的厂商";
//        log.debug("url:{},body:{},remoteIp:{}", url, dataStr, remoteIp);
        List<String> urlList = UrlParserUtil.urlParser( url);
        Reflections reflections = new Reflections("cn.shuzilm.interf.rtb.parser");
        Set<Class<? extends RequestService>> monitorClasses = reflections.getSubTypesOf(RequestService.class);
        String className =null;
        for (Class<? extends RequestService> monitorClass : monitorClasses) {
            for (int i = 0; i <urlList.size() ; i++) {
                if (monitorClass.getName().toLowerCase().contains(urlList.get(i))){
                    className=monitorClass.getName();
                    break;
                }
            }
        }
        RequestService requestService = RequestServiceFactory.getRequestService(className);
        responseStr = requestService.parseRequest(dataStr);
        return responseStr;
    }
}
