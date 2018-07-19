package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.util.UrlParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
* @Description:    ExposureParser  曝光量解析
* @Author:         houkp
* @CreateDate:     2018/7/19 15:57
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 15:57
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class ExposureParser {


    private static final Logger log = LoggerFactory.getLogger(ExposureParser.class);

    private ArrayList<String> tokenList = null;

    public String parseData(String url, String body, String remoteIp) {
        log.debug("url:{},body:{}", url, body);
        Map<String, String> map = UrlParserUtil.urlRequest(url);
        if (!map.containsKey("token") || !map.containsKey("uid") || !map.containsKey("type")) {
            return UrlParserUtil.packageResponse("400", "Missing required parameters", null);
        } else if (map.containsKey("token")) {
            String token = map.get("token").toLowerCase();
            if (!tokenList.contains(token)) {
                return UrlParserUtil.packageResponse("401", "Requested with invalid token", null);
            }
        }
        return url;
    }

}
