package cn.shuzilm;

import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.util.UrlParserUtil;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URLDecoder;
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
public class TestAdViewClickParameterParserImpl  {

    private static final Logger log = LoggerFactory.getLogger(TestAdViewClickParameterParserImpl.class);

    private static final String PIXEL_CONFIG = "pixel.properties";

    private static AppConfigs configs = AppConfigs.getInstance(PIXEL_CONFIG);


    public static String parseUrlStr(String url) {
        MDC.put("sift", "AdViewClick");
        log.debug("AdViewClick点击的url值:{}", url);
        Map<String, String> urlRequest = UrlParserUtil.urlRequest(url);
        log.debug("AdViewClick点击之后的url值:{}", urlRequest);
        DUFlowBean element = new DUFlowBean();

        String requestId = urlRequest.get("id");
        element.setInfoId(requestId + UUID.randomUUID());
        element.setRequestId(requestId);
        element.setBidid(urlRequest.get("bidid"));

        String impid = urlRequest.get("impid");
        List<Impression> list = new ArrayList();
        Impression impression = new Impression();
        element.setImpression(list);
        impression.setId(impid);
        list.add(impression);


        String act = urlRequest.get("act");
        element.setWinNoticeTime(Long.valueOf(act));

        String adx = urlRequest.get("adx");
        element.setAdxId(adx);

        String did = urlRequest.get("did");
        element.setDid(did);

        String device = urlRequest.get("device");
        element.setDeviceId(device);

        String app = urlRequest.get("app").equals("null") ? "" : urlRequest.get("app");
        element.setAppName(URLDecoder.decode(app));
        String appn = urlRequest.get("appn").equals("null") ? "" : urlRequest.get("appn");
        element.setAppPackageName(appn);
        String appv = urlRequest.get("appv").equals("null") ? "" : urlRequest.get("appv");
        element.setAppVersion(appv);
        String ddem = urlRequest.get("ddem").equals("null") ? "" : urlRequest.get("ddem");
        element.setAudienceuid(ddem);
        String dcuid = urlRequest.get("dcuid").equals("null") ? "" : urlRequest.get("dcuid");
        element.setCreativeUid(dcuid);
        String dpro = urlRequest.get("dpro").equals("null") ? "" : urlRequest.get("dpro");
        element.setProvince(dpro);
        String dcit = urlRequest.get("dcit").equals("null") ? "" : urlRequest.get("dcit");
        element.setCity(dcit);
        String dcou = urlRequest.get("dcou").equals("null") ? "" : urlRequest.get("dcou");
        element.setCountry(dcou);
        String dade = urlRequest.get("dade").equals("null") ? "" : urlRequest.get("dade");
        element.setAdvertiserUid(dade);
        String dage = urlRequest.get("dage").equals("null") ? "" : urlRequest.get("dage");
        element.setAgencyUid(dage);
        String daduid = urlRequest.get("daduid").equals("null") ? "" : urlRequest.get("daduid");
        element.setAdUid(daduid);
        String pmp = urlRequest.get("pmp").equals("null") ? "" : urlRequest.get("pmp");
        element.setDealid(pmp);
        String userip = urlRequest.get("userip").equals("null") ? "" : urlRequest.get("userip");
        element.setIpAddr(userip);
        element.setAdxSource("AdView");


            //pixel服务器发送到Phoenix

            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                    element.getDid(), element.getBidid(),
                    element.getRequestId(), element.getDeviceId(),
                    element.getAdUid(), element.getAudienceuid(),
                    element.getAgencyUid(), element.getAdvertiserUid(),
                    element.getCreativeUid(), element.getProvince(),
                    element.getCity(), element.getActualPricePremium(),
                    element.getBiddingPrice(), element.getActualPrice(),
                    element.getAgencyProfit(), element.getOurProfit(),
                    element.getAdxId(), element.getAppName(),
                    element.getAppPackageName(), element.getAppVersion(),
                    element.getRequestId(), element.getImpression().get(0).getId(),
                    element.getDealid(), element.getAppId(), element.getBidid(),element.getIpAddr(),urlRequest.get("remoteIp"));
            MDC.remove("phoenix");
        String duFlowBeanJson = JSON.toJSONString(element);
        log.debug("duFlowBeanJson:{}", duFlowBeanJson);
        return requestId;
    }

}
