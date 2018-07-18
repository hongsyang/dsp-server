package cn.shuzilm.interf.parser;

import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.adview.response.Bid;
import cn.shuzilm.bean.adview.response.BidResponseBean;
import cn.shuzilm.bean.adview.response.SeatBid;
import cn.shuzilm.bean.control.TagBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.interf.RequestService;
import cn.shuzilm.interf.RequestServiceImpl;
import cn.shuzilm.util.*;
import cn.shuzilm.util.JedisManager;
import com.alibaba.fastjson.JSON;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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


    enum Field {
        work_grid_id,
        work_city_id,
        work_county_id,
        work_province_id,
        residence_grid_id,
        residence_city_id,
        residence_county_id,
        residence_province_id;
    }

    ;
    private JedisManager jedisManager = null;
    private ArrayList<String> tokenList = null;

    private static final Logger log = LoggerFactory.getLogger("ceshi");

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

    /**
     * 解析	Bid Request 报文
     * 并返回结果 	Bid Response
     *
     * @param url
     * @param body
     * @param remoteIp
     * @return
     */
    public String parseData(String url, String body, String remoteIp) {
        try {
            log.debug("Application started");
            MDC.put("sift","interface");
            log.debug("interface says hello");
            MDC.put("sift","rtb");
            log.debug("rtb says hello");
//            MDC.put("interface",null);
            String responseStr = "";
            Map<String, String> map = urlRequest(url);
            //请求报文BidRequest解析
            RequestService requestService = new RequestServiceImpl();
            BidRequestBean bidRequestBean = requestService.parseRequest(body);

            //TODO 业务逻辑等待开发

            //TODO  输出的标准log  start  未开始
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String format = LocalDateTime.now().format(formatter);//时间戳

            if (bidRequestBean != null) {
                DUFlowBean duFlowBean = new DUFlowBean();
                duFlowBean.setInfoId(bidRequestBean.getId() + format);
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
                        duFlowBean.getAdUid(), duFlowBean.getAdvertiserUid(),
                        duFlowBean.getAdvertiserUid(), duFlowBean.getAgencyUid(),
                        duFlowBean.getCreativeUid(), duFlowBean.getProvince(),
                        duFlowBean.getCity(), body);
            }
            //TODO  输出的标准log  end

         /*   if(!map.containsKey("token") || !map.containsKey("uid") || !map.containsKey("type")){
                return packageResponse("400","Missing required parameters",null);
            }else if(map.containsKey("token")){
                String token = map.get("token").toLowerCase();
                if(!tokenList.contains(token)){
                    return packageResponse("401","Requested with invalid token",null);
                }
            }*/
            //请求报文BidResponse返回

            BidResponseBean bidResponseBean = new BidResponseBean();
            bidResponseBean.setId(bidRequestBean.getId());
            bidResponseBean.setBidid("Bidid" + format);//BidResponse 的唯一标识,由 DSP生成
            List<SeatBid> seatBidList = new ArrayList<SeatBid>();//注意第一层数组  DSP出价
            List<Bid> bidList = new ArrayList<Bid>();//注意第二层数组 针对单次曝光的出价

            SeatBid seatBid = new SeatBid();
            seatBid.setSeat("seat" + format);//SeatBid 的标识,由 DSP 生成

            Bid bid = new Bid();
            bid.setAdid("adid" + format);//广告id，对应duFlowBean的AdUid；
            List<Impression> imp = bidRequestBean.getImp();
            Impression impression = imp.get(0);
            bid.setImpid(impression.getId());//从bidRequestBean里面取
            bid.setWurl("http://dsp.example.com/winnotice?price=" + "60000");//赢价通知，由 AdView 服务器 发出  编码格式的 CPM 价格*10000，如价格为 CPM 价格 0.6 元，则取值0.6*10000=6000。
            List<String> urls = new ArrayList<>();
            urls.add("http://dsp.example1.com");
            urls.add("http://dsp.example2.com");
            urls.add("http://dsp.example3.com");
            urls.add("http://dsp.example4.com");
            Map nurlMap = new HashMap();
            nurlMap.put(0, urls);
            bid.setNurl(nurlMap);//带延迟的展示汇报，由客户端发送
            bid.setAdmt(4);//广告类型
            bid.setPrice(6000);//CPM 出价，数值为 CPM 实际价格*10000，如出价为 0.6 元，
            bid.setCurl(urls);//点击监控地址，客户端逐个发送通知
            bid.setCrid("crid" + format);//广告物料 ID
            String adm = "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />";
            bid.setAdm(adm);// 广告物料数据
            bid.setAdh(50);//广告物料高度
            bid.setAdw(320);//广告物料宽度
            bid.setAdct(1);// 广告点击行为类型，参考附录 9
            bid.setCid("cid" + format);//广告创意 ID，可用于去重
            //添加到list中
            bidList.add(bid);
            seatBid.setBid(bidList);
            seatBidList.add(seatBid);
            bidResponseBean.setSeatBid(seatBidList);
            Object o = JSON.toJSON(bidResponseBean);
            responseStr = o.toString();
//            System.out.println(responseStr);

            //下面的代码不知道是干什么的，先注释起来。houkp
/*            String type = map.get("type");
            String uid = map.get("uid");
            String tagId = map.get("tagid");
            Object obj = jedisManager.getMap(uid);
            if(obj != null){
                Map<String,String> resultMap = (Map)obj;
                ArrayList<TagBean> beanList = new ArrayList<>();

                beanList.add(new TagBean(Field.residence_province_id.name(),resultMap.get(Field.residence_province_id.name()),""));
                beanList.add(new TagBean(Field.residence_city_id.name(),resultMap.get(Field.residence_city_id.name()),""));
                beanList.add(new TagBean(Field.residence_county_id.name(),resultMap.get(Field.residence_county_id.name()),""));
                beanList.add(new TagBean(Field.residence_grid_id.name(),resultMap.get(Field.residence_grid_id.name()),""));

                beanList.add(new TagBean(Field.work_province_id.name(),resultMap.get(Field.work_province_id.name()),""));
                beanList.add(new TagBean(Field.work_city_id.name(),resultMap.get(Field.work_city_id.name()),""));
                beanList.add(new TagBean(Field.work_county_id.name(),resultMap.get(Field.work_county_id.name()),""));
                beanList.add(new TagBean(Field.work_grid_id.name(),resultMap.get(Field.work_grid_id.name()),""));

                responseStr = packageResponse("0","Success",beanList);
            }else{
                responseStr = packageResponse("0","Tag not found",null);
            }
            //		responseStr = parseApp(dataValue,remoteIp,wdt,mqTool,gzip);*/

            return responseStr;
        } catch (Exception ex) {
            ex.printStackTrace();
            return packageResponse("500", "Internal server error ", null);
        }

    }

    private String packageResponse(String status, String message, List<TagBean> list) {
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
