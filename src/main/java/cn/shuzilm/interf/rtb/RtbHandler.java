package cn.shuzilm.interf.rtb;

import bidserver.BidserverSsp;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import gdt.adx.GdtRtb;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 对应不同版本的SDK，netty需要做相应的修改： 1、队列名 2、日志路径 3、maven包名，用端口号做区分
 *
 * @author
 */
public class RtbHandler extends SimpleChannelUpstreamHandler {

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);


    private static final Logger log = LoggerFactory.getLogger(RtbHandler.class);
    //	private WriteDataToLog wdt;
    private RtbRequestParser parser = null;
    private static AtomicInteger counter = new AtomicInteger();

    private ExecutorService executor = null;

    private String remoteIp = null;
    private String dataStr = null;
    private String url = null;
    //log日志参数
    private String requestId = null;
    private Integer adxId = 0;
    private String appName = "";
    private String appPackageName = "";
    private Integer ipBlackListFlag = 1;
    private Integer bundleBlackListFlag = 1;
    private Integer deviceIdBlackListFlag = 1;
    private Integer timeOutFlag = 1;
    private Integer bidPriceFlag = 0;
    private String price = "-1";
    private Integer exceptionFlag = 1;
    //返回结果
    private String result = null;


    public RtbHandler(ExecutorService executor) {
        parser = new RtbRequestParser();
        this.executor = executor;
        System.out.println(Thread.currentThread().getName() + " rtb parser 初始化成功。。。");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent messageEvent) {
        long start = System.currentTimeMillis();
        //返回状态
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setHeader("Content-Type", "text/html");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Connection", HttpHeaders.Values.KEEP_ALIVE);

        // 请求状态
        boolean close = true;
        log.debug("线程名称：{}，counter：{}", Thread.currentThread().getName(), counter);
        counter.getAndAdd(1);
        try {
            if (messageEvent.getMessage() instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) messageEvent.getMessage();
                // 请求状态
                close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));
                // 获取对方的ip地址
                remoteIp = ctx.getChannel().getRemoteAddress().toString().split(":")[0].replace("/", "");
                //接收 GET 请求
                url = request.getUri();
                //接收 POST 请求 , 获取 SDK 回传数据
                dataStr = new String(request.getContent().array());

                BidserverSsp.BidRequest bidRequest = null;
                GdtRtb.BidRequest tencentBidRequest = null;

                if (url.contains("youyi")) {
                    bidRequest = BidserverSsp.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(bidRequest);
                } else if (url.contains("tencent")) {
                    tencentBidRequest = GdtRtb.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(tencentBidRequest);
                } else {
                    dataStr = URLDecoder.decode(dataStr, "utf-8");
                }


            }

            //增加超时线程池
            Future<Object> future = executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //主业务逻辑
                    return parser.parseData(url, dataStr, remoteIp);
                }
            });

            log.debug("超时时间设置：{}", configs.getInt("TIME_OUT"));
            try {
                result = (String) future.get(configs.getInt("TIME_OUT"), TimeUnit.MILLISECONDS);
                log.debug("线程返回：{}", result);
            } catch (TimeoutException e) {
                exceptionFlag = 0;
                // 超时情况
                long end = System.currentTimeMillis();
                MDC.put("sift", "timeOut");
                log.error("timeMs:{},url:{}", end - start, url);
                MDC.remove("sift");
                response.setStatus(HttpResponseStatus.NO_CONTENT);
                ChannelFuture future1 = messageEvent.getChannel().write(response);
                future1.addListener(ChannelFutureListener.CLOSE);
                future.cancel(true);// 中断执行此任务的线程
                return;
            }

            //正常情况 主业务逻辑
            byte[] content = null;
            String resultData = result;
            if ("".equals(resultData)) {
                response.setStatus(HttpResponseStatus.NO_CONTENT);
                content = resultData.getBytes("utf-8");
            } else if (resultData.contains("session_id")) {
                BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
                JsonFormat.merge(resultData, builder);
                BidserverSsp.BidResponse build = builder.build();
                content = build.toByteArray();
            } else {
                content = resultData.getBytes("utf-8");
            }


            response.setHeader("Content-Length", content.length);
            ChannelBuffer buffer = new DynamicChannelBuffer(2048);
            buffer.writeBytes(content);
            response.setContent(buffer);

            //正常返回
            ChannelFuture future2 = messageEvent.getChannel().write(response);
            if (close) {
                future2.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            exceptionFlag = 0;
            long end = System.currentTimeMillis();
            MDC.put("sift", "rtb-exception");
            log.debug("timeMs:{},Exception:{},url:{},body:{},remoteIp:{}", end - start, e.getMessage(), url, dataStr, remoteIp);
            log.debug("timeMs:{},Exception:{}", end - start, e);
            MDC.remove("sift");
            response.setStatus(HttpResponseStatus.NO_CONTENT);
            ChannelFuture future1 = messageEvent.getChannel().write(response);
            future1.addListener(ChannelFutureListener.CLOSE);


        } finally {
            long end = System.currentTimeMillis();
            MDC.put("sift", configs.getString("ADX_REQUEST"));
            log.debug("timeMs:{},url:{},body:{},remoteIp:{}", end - start, url, dataStr, remoteIp);
            MDC.remove("sift");
            //是否在50毫秒内
            Long timeOut = end - start;
            if (timeOut > 50) {
                timeOutFlag = 0;
            }
            if (url.contains("lingji")) {
                adxId = 1;
                BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
                requestId = bidRequestBean.getId();
                if (bidRequestBean.getApp() != null) {
                    appName = bidRequestBean.getApp().getName();
                    appPackageName = bidRequestBean.getApp().getBundle();
                }
                if (result.contains("ipBlackList")) {
                    ipBlackListFlag = 0;
                }
                if (result.contains("bundleBlackList")) {
                    bundleBlackListFlag = 0;
                }
                if (result.contains("deviceIdBlackList")) {
                    deviceIdBlackListFlag = 0;
                }
                if (result.contains("price\":")) {
                    bidPriceFlag = 1;
                    String substring = result.substring(result.indexOf("price\":"));
                    price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf(",\""));
                }
            } else if (url.contains("adview")) {
                adxId = 2;
                BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
                requestId = bidRequestBean.getId();
                if (bidRequestBean.getApp() != null) {
                    appName = bidRequestBean.getApp().getName();
                    appPackageName = bidRequestBean.getApp().getBundle();
                }
                if (result.contains("ipBlackList")) {
                    ipBlackListFlag = 0;
                }
                if (result.contains("bundleBlackList")) {
                    bundleBlackListFlag = 0;
                }
                if (result.contains("deviceIdBlackList")) {
                    deviceIdBlackListFlag = 0;
                }
                if (result.contains("price\":")) {
                    bidPriceFlag = 1;
                    String substring = result.substring(result.indexOf("price\":"));
                    price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf(",\""));
                }
            } else if (url.contains("youyi")) {
                adxId = 3;
                YouYiBidRequest bidRequestBean = JSON.parseObject(dataStr, YouYiBidRequest.class);
                requestId = bidRequestBean.getSession_id();
                if (bidRequestBean.getMobile() != null) {
                    appName = bidRequestBean.getMobile().getApp_name();
                    appPackageName = bidRequestBean.getMobile().getApp_bundle();
                }
                if (result.contains("ipBlackList")) {
                    ipBlackListFlag = 0;
                }
                if (result.contains("bundleBlackList")) {
                    bundleBlackListFlag = 0;
                }
                if (result.contains("deviceIdBlackList")) {
                    deviceIdBlackListFlag = 0;
                }
                if (result.contains("price\":")) {
                    bidPriceFlag = 1;
                    String substring = result.substring(result.indexOf("price\":"));
                    price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf(",\""));
                }
            } else if (url.contains("tencent")) {
                adxId = 4;
                JSONObject jsonObject = JSON.parseObject(dataStr);
                requestId = jsonObject.getString("id");
                if (jsonObject.getJSONObject("app") != null) {
                    JSONObject app = jsonObject.getJSONObject("app");
                    appPackageName = app.getString("app_bundle_id");
                }
            }
            MDC.put("phoenix", "rtb-houkp");
            log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                            "\t{}\t{}\t{}\t{}\t{}\t{}",
                    LocalDateTime.now().toString(), new Date().getTime(),
                    LocalDate.now().toString(), LocalTime.now().getHour(),
                    LocalTime.now().getMinute(), requestId,
                    adxId, appName,
                    appPackageName, ipBlackListFlag,
                    bundleBlackListFlag,deviceIdBlackListFlag,
                    timeOutFlag, bidPriceFlag,
                    price, exceptionFlag
            );
            MDC.remove("phoenix");
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        System.out.println(e.toString());

        // super.exceptionCaught(ctx, e);//不打印堆栈日志
    }

    /**
     * 解析进来的请求数据
     *
     * @throws UnsupportedEncodingException
     */
    public String parseRequest(String url, String dataStr, String remoteIp) throws Exception {
        /**********		POST主业务逻辑		***************/
        String resultData = parser.parseData(url, dataStr, remoteIp);//SDK 2.0.1

        byte[] content = null;
        content = resultData.getBytes("utf-8");
        return resultData;
    }


}

