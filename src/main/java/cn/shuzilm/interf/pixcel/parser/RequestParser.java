package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.util.UrlParserUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.Set;

/**
 * @Description: RequestParser get请求解析
 * @Author: houkp
 * @CreateDate: 2018/7/19 15:38
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 15:38
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class RequestParser {

    private static final Logger log = LoggerFactory.getLogger(RequestParser.class);


    public String parseData(String url, String dataStr, String remoteIp) {
        String responseStr = "没有对应的解析器";
        MDC.put("sift", "userip");
        log.debug("url:{},body:{},remoteIp:{}", url, dataStr, remoteIp);
        List<String> urlList = UrlParserUtil.urlParser(url);
        Reflections reflections = new Reflections("cn.shuzilm.interf.pixcel.parser");
        Set<Class<? extends ParameterParser>> monitorClasses = reflections.getSubTypesOf(ParameterParser.class);
        String className = null;
        for (Class<? extends ParameterParser> monitorClass : monitorClasses) {
            for (int i = 0; i < urlList.size(); i++) {
                if (monitorClass.getName().toLowerCase().contains(urlList.get(i))) {
                    className = monitorClass.getName();
                    break;
                }
            }
        }
        ParameterParser parameterParser = ParameterParserFactory.getParameterParser(className);
        responseStr = parameterParser.parseUrl(url);
        return responseStr;
    }


}
