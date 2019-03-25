package cn.shuzilm.util;

import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.AppConfigs;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nutz.ssdb4j.impl.SimpleClient;
import org.nutz.ssdb4j.spi.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;

public class SSDBUtil {
    private static final Logger log = LoggerFactory.getLogger(SSDBUtil.class);

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static SimpleClient simpleClient = new SimpleClient(configs.getString("SSBD_HOST"), configs.getInt("SSBD_PORT"), 10000);

    public static void main(String[] args) throws InterruptedException {
        DUFlowBean duFlowBean = new DUFlowBean();
        duFlowBean.setRequestId("1111111");
        String duFlowBeanJson = JSON.toJSONString(duFlowBean);
        long l = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            Response set = simpleClient.setx(duFlowBean.getRequestId(), duFlowBeanJson, 10);
            System.out.println("第一次");
            if (set.ok()) {
                System.out.println(set.ok());
                break;
            }
        }
        long e = System.currentTimeMillis();
        log.debug("上传到ssdb的时间:{}", e - l);
        for (int i = 0; i < 60; i++) {
            Response houkp = simpleClient.get(duFlowBean.getRequestId());
            List<String> list = houkp.listString();
            System.out.println(list.size());
            System.out.println(houkp.ok());
            System.out.println(houkp.listString());
            Thread.sleep(2000L);
        }
    }


    /**
     * 把生成的内部流转DUFlowBean上传到ssdb服务器 设置60分钟失效
     *
     * @param targetDuFlowBean
     */
    public static void pushSSDB(DUFlowBean targetDuFlowBean) {
        log.debug("ssdb连接时间计数");
        String duFlowBeanJson = JSON.toJSONString(targetDuFlowBean);
        try {
            if (simpleClient != null) {
                MDC.put("sift", "ssdb");
                for (int i = 0; i < 3; i++) {
                    //设置60分钟失效
                    Response response = simpleClient.setx(targetDuFlowBean.getRequestId(), duFlowBeanJson, 60 * 60);
                    if (response.ok()) {
                        log.debug("推送到ssdb服务器是否成功;{},duFlowBeanJson:{}", response.ok(), duFlowBeanJson);
                        MDC.remove("sift");
                        break;
                    } else {
                        MDC.put("sift", "ssdb-exception");
                        log.debug("duFlowBeanJson：{}", duFlowBeanJson);
                        MDC.remove("sift");
                    }
                }

            } else {
                log.debug("SSDB为空：{}", simpleClient);
                MDC.put("sift", "ssdb-exception");
                log.debug("duFlowBeanJson：{}", duFlowBeanJson);
                MDC.remove("sift");

            }
        } catch (Exception e) {
            MDC.put("sift", "rtb-exception");
            log.error("SSDB为空：{}", e);
            MDC.remove("sift");
        }
    }

    /**
     * 根据请求id获取DUFlowBean 对象
     *
     * @param requestId
     * @return
     */
    public static DUFlowBean getDUFlowBean(String requestId) {
        Response response = simpleClient.get(requestId);
        if (response.listString().size() > 0) {
            String duFlowBeanJson = response.listString().get(0);
            MDC.put("sift", "pixcel-ssdb");
            log.debug("duFlowBeanJson：{}", duFlowBeanJson);
            MDC.remove("sift");
            DUFlowBean duFlowBean = JSONObject.parseObject(duFlowBeanJson, DUFlowBean.class);
            return duFlowBean;
        }else {
            return null;
        }
    }
}
