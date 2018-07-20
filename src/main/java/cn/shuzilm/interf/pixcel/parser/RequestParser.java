package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.bean.control.TagBean;
import cn.shuzilm.util.UrlParserUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:  RequestParser get请求解析
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
        String responseStr="没有对应的解析器";
        log.debug("url:{},body:{},remoteIp:{}", url, dataStr, remoteIp);
        List<String> urlList = UrlParserUtil.urlParser(url);
        for (String urls : urlList) {
            if (urls.equals("exp")){
            ParameterParser parameterParser =ParameterParserFactory.getParameterParser("cn.shuzilm.interf.pixcel.parser.ExpParser");
                responseStr = parameterParser.parseUrl(url);
            }else if (urls.equals("click")){
                ParameterParser parameterParser =ParameterParserFactory.getParameterParser("cn.shuzilm.interf.pixcel.parser.ClickParser");
                responseStr = parameterParser.parseUrl(url);
            }
        }

        return responseStr;
    }


}
