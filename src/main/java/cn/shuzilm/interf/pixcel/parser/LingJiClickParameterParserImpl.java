package cn.shuzilm.interf.pixcel.parser;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.RedisUtil;
import cn.shuzilm.util.SSDBUtil;
import cn.shuzilm.util.UrlParserUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @Description: ClickParser 点击解析
 * @Author: houkp
 * @CreateDate: 2018/7/19 18:44
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 18:44
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiClickParameterParserImpl implements ParameterParser {

    private static final Logger log = LoggerFactory.getLogger(LingJiClickParameterParserImpl.class);

    private static final String PIXEL_CONFIG = "pixel.properties";

    private static AppConfigs configs = AppConfigs.getInstance(PIXEL_CONFIG);

    public static String parseUrlStr(String url) {
        MDC.put("sift", "LingJiClick");
        log.debug("LingJiClick点击的url值:{}", url);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        log.debug("LingJiClick点击转换之后的url值:{}", urlRequest);
        DUFlowBean element = new DUFlowBean();

        String requestId = urlRequest.get("id");
        try {

            if (SSDBUtil.getDUFlowBean(requestId) != null) {
                element = SSDBUtil.getDUFlowBean(requestId);
            } else {
                element.setInfoId(requestId + UUID.randomUUID());//2019年03月27号 现阶段不用
                element.setRequestId(requestId);//请求id
                element.setBidid(urlRequest.get("bidid"));//去重id
                //点击 不计算价格

                String act = urlRequest.get("act");
                element.setWinNoticeTime(Long.valueOf(act));//竞价的时候，带过来的点击时间
                String did = urlRequest.get("device");//数盟设备id
                element.setDid(did);
                String device = urlRequest.get("device");//设备id
                element.setDeviceId(device);
                String appn = urlRequest.get("appn").equals("null") ? "" : urlRequest.get("appn");//App包名
                element.setAppPackageName(appn);
                //点击不获取 溢价系数

                String ddem = urlRequest.get("ddem").equals("null") ? "" : urlRequest.get("ddem");//人群ID
                element.setAudienceuid(ddem);
                String dcuid = urlRequest.get("dcuid").equals("null") ? "" : urlRequest.get("dcuid");//创意id
                element.setCreativeUid(dcuid);
                String dbidp = urlRequest.get("dbidp").equals("null") ? "" : urlRequest.get("dbidp");//广告主出价
                element.setBiddingPrice(Double.valueOf(dbidp));
                String dade = urlRequest.get("dade").equals("null") ? "" : urlRequest.get("dade");//广告主ID
                element.setAdvertiserUid(dade);
                String dage = urlRequest.get("dage").equals("null") ? "" : urlRequest.get("dage");//代理商ID
                element.setAgencyUid(dage);
                String daduid = urlRequest.get("daduid").equals("null") ? "" : urlRequest.get("daduid");//广告ID
                element.setAdUid(daduid);
                String dmat = urlRequest.get("dmat").equals("null") ? "" : urlRequest.get("dmat");//素材id
                element.setMaterialId(dmat);
                String userip = urlRequest.get("userip").equals("null") ? "" : urlRequest.get("userip");//用户ip
                element.setIpAddr(userip);
                element.setAdxId("1");
                element.setAdxSource("LiJing");
                //--------------------------------------------------不一定传过来，从redis中获取，如果没有就不要了

                if (RedisUtil.getDUFlowBean(requestId) != null) {
                    DUFlowBean duFlowBean = RedisUtil.getDUFlowBean(requestId);
                    List<Impression> list = new ArrayList();
                    Impression impression = new Impression();
                    impression.setId(duFlowBean.getImpression().get(0).getId());
                    list.add(impression);
                    element.setImpression(list);
                    String app = duFlowBean.getAppName().equals("null") ? "" : urlRequest.get("app");
                    element.setAppName(app);
                    String appv = duFlowBean.getAppVersion().equals("null") ? "" : urlRequest.get("appv");
                    element.setAppVersion(appv);
                    String dpro = duFlowBean.getProvince().equals("null") ? "" : urlRequest.get("dpro");
                    element.setProvince(dpro);
                    String dcit = duFlowBean.getCity().equals("null") ? "" : urlRequest.get("dcit");
                    element.setCity(dcit);
                    String dcou = duFlowBean.getCountry().equals("null") ? "" : urlRequest.get("dcou");
                    element.setCountry(dcou);
                    String pmp = duFlowBean.getDealid().equals("null") ? "" : urlRequest.get("pmp");
                    element.setDealid(pmp);

                }else {
                    List<Impression> list = new ArrayList();
                    Impression impression = new Impression();
                    impression.setId("");
                    list.add(impression);
                    element.setImpression(list);
                    element.setAppName("");
                    element.setAppVersion("");
                    element.setProvince("");
                    element.setCity("");
                    element.setCountry("");
                    element.setDealid("");
                }


            }
            log.debug("LingJiClick点击的requestid:{},element值:{}", requestId, element);
            AdPixelBean bean = new AdPixelBean();
            if (element != null) {
                bean.setAdUid(element.getAdUid());
            }
            bean.setHost(configs.getString("HOST"));
            bean.setClickNums(1);
            bean.setClickTime(new Date().getTime());
            bean.setType(1);
            //pixel服务器发送到主控模块
            log.debug("pixel服务器发送到主控模块的LingJiClickBean：{}", bean);
            PixelFlowControl.getInstance().sendStatus(bean);

            //pixel服务器发送到Phoenix
            MDC.put("sift", "LingJiClick");
            log.debug("发送到Phoenix的DUFlowBean:{}", element);
            MDC.put("phoenix", "Click");
            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                    element.getInfoId(), new Date().getHours(),
                    new Date().getTime(), LocalDateTime.now().toString(),
                    element.getDid(), element.getDeviceId(),
                    element.getAdUid(), element.getAudienceuid(),
                    element.getAgencyUid(), element.getAdvertiserUid(),
                    element.getCreativeUid(), element.getProvince(),
                    element.getCity(), element.getActualPricePremium(),
                    element.getBiddingPrice(), element.getActualPrice(),
                    element.getAgencyProfit(), element.getOurProfit(),
                    element.getAdxId(), element.getAppName(),
                    element.getAppPackageName(), element.getAppVersion(),
                    element.getRequestId(), element.getImpression().get(0).getId(),
                    element.getDealid(), element.getAppId(),
                    element.getBidid(), element.getIpAddr(), urlRequest.get("remoteIp"),
                    element.getMaterialId());

            MDC.remove("phoenix");
            boolean lingJiClick = RedisQueueManager.putElementToQueue("CLICK", element, Priority.MAX_PRIORITY);
            if (lingJiClick) {
                log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiClick);
            } else {
                log.debug("发送elemen :{}到Phoenix是否成功：{}", element, lingJiClick);
                throw new RuntimeException();
            }
        } catch (Exception e) {
            Help.sendAlert("发送到" + configs.getString("HOST") + "失败,LingJiClick");
            MDC.put("sift", "exception");
            boolean click_error = RedisQueueManager.putElementToQueue("CLICK_ERROR", element, Priority.MAX_PRIORITY);
            log.debug("发送element：{}到CLICK_ERROR队列：{}", element, click_error);
            log.debug("element:{}", JSON.toJSONString(element));
            log.error("异常信息:{}", e);
            MDC.remove("sift");
        }

        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return requestId;
    }

    @Override
    public String parseUrl(String url) {
        return null;
    }
}
