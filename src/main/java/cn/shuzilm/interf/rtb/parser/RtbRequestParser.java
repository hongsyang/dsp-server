package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.util.UrlParserUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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


    private static final Object lock = new Object();

    private static volatile Reflections reflections;

    private AppConfigs configs = null;

    private static final String FILTER_CONFIG = "filter.properties";



    private static String FILE_NAME = "cn.shuzilm.interf.rtb.parser";

    /**
     * 根据厂商解析post参数
     *
     * @param url
     * @param dataStr
     * @param remoteIp
     * @return
     */
    public String parseData(String url, String dataStr, String remoteIp) throws Exception {
        String responseStr = "没有对应的厂商";
        this.configs = AppConfigs.getInstance(FILTER_CONFIG);

        MDC.put("sift",configs.getString("ADX_REQUEST"));
        log.debug("url:{},body:{},remoteIp:{}", url, dataStr, remoteIp);
        MDC.remove("sift");
        if (Boolean.valueOf(configs.getString("FILTER_RTB"))){
            responseStr="测试请求";
            return responseStr;
        }
        List<String> urlList = UrlParserUtil.urlParser(url);
        Reflections reflections = instance(FILE_NAME);
        Set<Class<? extends RequestService>> monitorClasses = reflections.getSubTypesOf(RequestService.class);
        String className = null;
        for (Class<? extends RequestService> monitorClass : monitorClasses) {
            for (int i = 0; i < urlList.size(); i++) {
                if (monitorClass.getName().toLowerCase().contains(urlList.get(i))) {
                    className = monitorClass.getName();
                    break;
                }
            }
        }
        if (className != null) {
            RequestService requestService = RequestServiceFactory.getRequestService(className);
            responseStr = requestService.parseRequest(dataStr);
        }

        return responseStr;
    }

    /**
     * 实例化扫描器
     *
     * @param fileName
     * @return
     */
    private Reflections instance(String fileName) {
        if (reflections == null) {
            synchronized (lock) {
                if (reflections == null) {
                    reflections = new Reflections(fileName);
                }
            }
        }
        return reflections;
    }
}
