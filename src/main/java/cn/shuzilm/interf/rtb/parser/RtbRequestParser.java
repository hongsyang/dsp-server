package cn.shuzilm.interf.rtb.parser;

import cn.shuzilm.interf.pixcel.parser.ParameterParser;
import cn.shuzilm.interf.pixcel.parser.ParameterParserFactory;
import cn.shuzilm.interf.pixcel.parser.RequestParser;
import cn.shuzilm.util.UrlParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        for (String urls : urlList) {
            if (urls.equals("adview")) {
                RequestService requestService = RequestServiceFactory.getRequestService("cn.shuzilm.interf.rtb.parser.KuaiyouParser");
                responseStr = requestService.parseRequest(dataStr);
            } else if (urls.equals("lingji")) {
                RequestService requestService = RequestServiceFactory.getRequestService("cn.shuzilm.interf.rtb.parser.LingjiParser");
                responseStr = requestService.parseRequest(dataStr);
            }
        }

        return responseStr;
    }
}
