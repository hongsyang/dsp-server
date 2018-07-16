package cn.shuzilm.interf.parser;

import cn.shuzilm.bean.control.TagBean;
import cn.shuzilm.util.*;
import cn.shuzilm.util.JedisManager;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加和广告 DSP 请求
 *
 * @author wanght 20180604
 */
public class JiaheParser {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(JiaheParser.class);

    enum Field {
        work_grid_id,
        work_city_id,
        work_county_id,
        work_province_id,
        residence_grid_id,
        residence_city_id,
        residence_county_id,
        residence_province_id
    }

    ;
    private JedisManager jedisManager = null;
    private static Logger logger = Logger.getLogger("jiahe");
    private ArrayList<String> tokenList = null;

    public JiaheParser() {
        // 10.169.20.2,6379,123456yiguan,0
        String[] jedisConf = Constants.getInstance().getConf("REDIS_CONF").split(",");
        jedisManager = new JedisManager(jedisConf[0], Integer.parseInt(jedisConf[1]), jedisConf[3]);
        tokenList = new ArrayList<>();
        String[] tokenArray = Constants.getInstance().getConf("TOKEN").split(",");
        for (String token : tokenArray) {
            tokenList.add(token);
        }
    }

    public String parseData(String url, String body, String remoteIp) {
        try {
            //		/api/query?token=xxxxxxxxxxxxxxxx&type=1&uid= ECD797683BA588E8EF87D08991ADD5E3&tagid=10000,10001,10002&ts=14570 0000
            String responseStr = "";
            Map<String, String> map = urlRequest(url);
            RequestService requestService = new RequestServiceImpl();
            BidRequestBean bidRequestBean = requestService.parseRequest(body);
            if (bidRequestBean != null) {
                DUFlowBean duFlowBean = new DUFlowBean();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                String format = LocalDateTime.now().format(formatter);
                duFlowBean.setInfoId(format + bidRequestBean.getId());
                duFlowBean.setDid("数盟的标识did，不知道在哪取");
                duFlowBean.setDeviceId(bidRequestBean.getDevice().getDpidsha1());
                duFlowBean.setAdUid("广告id，听说张杰在做");
                duFlowBean.setAudienceuid("人群id，听说张杰在做");
                duFlowBean.setAdvertiserUid("广告主id，听说张杰在做");
                duFlowBean.setAgencyUid("代理商id，听说张杰在做");
                duFlowBean.setCreativeUid("创意id，听说张杰在做");
                duFlowBean.setProvince("省，不知道在哪取");
                duFlowBean.setCity("市，不知道在哪取");
                duFlowBean.setRequestId(bidRequestBean.getId());
                log.debug("\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}", duFlowBean.getInfoId(),
                        duFlowBean.getDid(), duFlowBean.getDeviceId(),
                        duFlowBean.getAdUid(),duFlowBean.getAdvertiserUid(),
                        duFlowBean.getAdvertiserUid(), duFlowBean.getAgencyUid(),
                        duFlowBean.getCreativeUid(), duFlowBean.getProvince(),
                        duFlowBean.getCity(), body);
            }
            if (!map.containsKey("token") || !map.containsKey("uid") || !map.containsKey("type")) {
                return packageResponse("400", "Missing required parameters", null);
            } else if (map.containsKey("token")) {
                String token = map.get("token").toLowerCase();
                if (!tokenList.contains(token)) {
                    return packageResponse("401", "Requested with invalid token", null);
                }
            }
//            0：cookie 1：IMEI   2：IDFA 3：MAC
            String type = map.get("type");
            String uid = map.get("uid");
            String tagId = map.get("tagid");
//| did             | string     |       |                                             |
//| grid_id         | bigint     |       |                                             |
//| city_id         | bigint     |       |                                             |
//| county_id       | bigint     |       |                                             |
//| lng             | double     |       |                                             |
//| lat             | double     |       |                                             |
//| type            | string     |       |                                             |
//| imei            | string     |       |                                             |
//| mac             | string     |       |                                             |
//| imei_md5        | string     |       |                                             |
//| mac_md5         | string     |       |                                             |
//| city_name       | string     |       |                                             |
//| county_name     | string     |       |                                             |
            Object obj = jedisManager.getMap(uid);
            if (obj != null) {
                Map<String, String> resultMap = (Map) obj;
                ArrayList<TagBean> beanList = new ArrayList<>();

                beanList.add(new TagBean(Field.residence_province_id.name(), resultMap.get(Field.residence_province_id.name()), ""));
                beanList.add(new TagBean(Field.residence_city_id.name(), resultMap.get(Field.residence_city_id.name()), ""));
                beanList.add(new TagBean(Field.residence_county_id.name(), resultMap.get(Field.residence_county_id.name()), ""));
                beanList.add(new TagBean(Field.residence_grid_id.name(), resultMap.get(Field.residence_grid_id.name()), ""));

                beanList.add(new TagBean(Field.work_province_id.name(), resultMap.get(Field.work_province_id.name()), ""));
                beanList.add(new TagBean(Field.work_city_id.name(), resultMap.get(Field.work_city_id.name()), ""));
                beanList.add(new TagBean(Field.work_county_id.name(), resultMap.get(Field.work_county_id.name()), ""));
                beanList.add(new TagBean(Field.work_grid_id.name(), resultMap.get(Field.work_grid_id.name()), ""));

                responseStr = packageResponse("0", "Success", beanList);
            } else {
                responseStr = packageResponse("0", "Tag not found", null);
            }
            //		responseStr = parseApp(dataValue,remoteIp,wdt,mqTool,gzip);

            return responseStr;
        } catch (Exception ex) {
            ex.printStackTrace();
            return packageResponse("500", "Internal server error ", null);
        }

    }

    private String packageResponse(String status, String message, List<TagBean> list) {
//        {
//            "code": 0,
//                "msg": "",
//                "result": {
//                      "10000": 0,
//                      "10001": 1
//                  }
//        }
        JSONObject json = new JSONObject();
        json.put("code", status);
        json.put("msg", message);
        JSONObject json2 = new JSONObject();
        if (list != null && list.size() > 0) {
            for (TagBean tag : list) {
                if (tag.getTagId() != null)
                    json2.put(tag.getTagName(), tag.getTagId());
            }
        }

        json.put("result", json2);
        return json.toString();

    }


    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> urlRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = truncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        //每个键值为一组 www.2cto.com
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url 请求参数部分
     */
    private static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim();

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }

        return strAllParam;
    }


}
