package cn.shuzilm.interf.rtb;

import bidserver.BidserverSsp;
import cn.shuzilm.BaiduRealtimeBidding;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.tencent.request.TencentBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import cn.shuzilm.util.HttpClientUtil;
import com.alibaba.fastjson.JSON;
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
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
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
    private Integer ipBlackListFlag = null;
    private Integer bundleBlackListFlag = null;
    private Integer deviceIdBlackListFlag = null;
    private Integer AdTagBlackListFlag = null;
    private Integer filterRuleBidRequestFlag = null;
    private Integer timeOutFlag = 1;
    private Integer bidPriceFlag = 0;
    private String price = "-1";
    private Integer exceptionFlag = 1;
    //本机地址
    private String hostAddress;
    //返回结果
    private String result = null;

    //初始统计次数
    private static Integer countNum = 1;

    //统计每家adx的请求数
    private static ConcurrentHashMap<String, Integer> countMap = new ConcurrentHashMap();

    //统计每家adx的出手数
    private static ConcurrentHashMap<String, Integer> bidCountMap = new ConcurrentHashMap();

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {

            MDC.put("sift", "https-count"); 
            log.debug("请求 的数据:{}", JSON.toJSONString(countMap));
            log.debug("出手 的数据:{}", JSON.toJSONString(bidCountMap));
            try {
                HttpClientUtil.httpPostWithJson("http://172.17.129.131:8090/postRequest", JSON.toJSONString(countMap));
                HttpClientUtil.httpPostWithJson("http://172.17.129.131:8090/postBid", JSON.toJSONString(bidCountMap));
            } catch (Exception e) {
                MDC.put("sift", "rtb-exception");
                log.error("发送统计请求数和出手数异常Exception:{}", e);
                MDC.remove("sift");
            }
            countMap.clear();
            bidCountMap.clear();
            MDC.remove("sift");
        }, 0, configs.getInt("COUNT_TIME"), TimeUnit.MINUTES);
    }

    public RtbHandler(ExecutorService executor, ConcurrentHashMap countMap) {
        InetAddress ia = null;
        try {
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        parser = new RtbRequestParser();
        this.executor = executor;
        this.countMap = countMap;
        this.hostAddress = ia.getHostAddress();
        log.debug("ExecutorService: {}", executor);
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
                BaiduRealtimeBidding.BidRequest baiduBidRequest = null;


                if (url.contains("youyi")) {
                    bidRequest = BidserverSsp.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(bidRequest);
                } else if (url.contains("tencent")) {
                    tencentBidRequest = GdtRtb.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(tencentBidRequest);
                    //增加百度解析
                } else if (url.contains("baidu")) {
                    baiduBidRequest = BaiduRealtimeBidding.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(baiduBidRequest);
                } else {
                    dataStr = URLDecoder.decode(dataStr, "utf-8");
                }


            }
            //返回接口
            byte[] content = null;


            //增加超时线程池
            Future<Object> future = executor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    //主业务逻辑
                    return parser.parseData(url, dataStr, remoteIp, executor, countMap);
                }
            });

            log.debug("超时时间设置：{}", configs.getInt("TIME_OUT"));
            try {
                if (url.contains("youyi")) {
                    result = (String) future.get(configs.getInt("YOUYI_TIME_OUT"), TimeUnit.MILLISECONDS);
                } else if (url.contains("tencent")) {
                    result = (String) future.get(configs.getInt("TENCENT_TIME_OUT"), TimeUnit.MILLISECONDS);
                } else if (url.contains("baidu")) {
                    result = (String) future.get(configs.getInt("BAIDU_TIME_OUT"), TimeUnit.MILLISECONDS);
                } else if (url.contains("lingji")) {
                    result = (String) future.get(configs.getInt("LINGJI_TIME_OUT"), TimeUnit.MILLISECONDS);
                } else if (url.contains("adview")) {
                    result = (String) future.get(configs.getInt("ADVIEW_TIME_OUT"), TimeUnit.MILLISECONDS);
                } else {
                    result = (String) future.get(configs.getInt("TIME_OUT"), TimeUnit.MILLISECONDS);
                }

            } catch (TimeoutException e) {
                timeOutFlag = 0;
                exceptionFlag = 0;
                // 超时情况
                long end = System.currentTimeMillis();
                MDC.put("sift", "timeOut");
                log.error("超时timeMs:{},url:{}", end - start, url);
                MDC.remove("sift");
                String resultData = "";
                if (result != null) {
                    resultData = result;
                }
                if (resultData.contains("204session_id")) {
                    BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
                    String substring = resultData.substring(resultData.indexOf("204session_id") + 14);
                    builder.setSessionId(substring);
//                    builder.setAds(0, BidserverSsp.BidResponse.Ad.newBuilder().build() );
                    content = builder.build().toByteArray();
                    response.setHeader("Content-Length", content.length);
                    ChannelBuffer buffer = new DynamicChannelBuffer(2048);
                    buffer.writeBytes(content);
                    response.setContent(buffer);
                } else {
                    response.setStatus(HttpResponseStatus.NO_CONTENT);
                    ChannelBuffer buffer = new DynamicChannelBuffer(2048);
                    content = "".getBytes("utf-8");
                    response.setHeader("Content-Length", content.length);
                    buffer.writeBytes(content);
                    response.setContent(buffer);
                }
                ChannelFuture future1 = messageEvent.getChannel().write(response);
                future1.addListener(ChannelFutureListener.CLOSE);
                future.cancel(true);// 中断执行此任务的线程
                return;
            }

            //正常情况 主业务逻辑
            String resultData = null;
            if (result != null) {
                resultData = result;
            } else {
                resultData = "";
            }
            if (resultData.contains("204session_id")) {
                BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
                //修改状态码为200
//                response.setStatus(HttpResponseStatus.NO_CONTENT);
                String substring = resultData.substring(resultData.indexOf("204session_id") + 14);
                builder.setSessionId(substring);
//                builder.setAds(0, BidserverSsp.BidResponse.Ad.newBuilder().build() );
                content = builder.build().toByteArray();
            } else if ("".equals(resultData) || "ipBlackList".equals(resultData)
                    || "bundleBlackList".equals(resultData)
                    || "deviceIdBlackList".equals(resultData)
                    || "AdTagBlackList".equals(resultData)) {
                response.setStatus(HttpResponseStatus.NO_CONTENT);
                content = "".getBytes("utf-8");
            } else if (resultData.contains("session_id")) {
                BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
                JsonFormat.merge(resultData, builder);
                BidserverSsp.BidResponse build = builder.build();
                content = build.toByteArray();
            } else if (resultData.contains("seat_bids")) {
                GdtRtb.BidResponse.Builder builder = GdtRtb.BidResponse.newBuilder();
                JsonFormat.merge(resultData, builder);
                GdtRtb.BidResponse build = builder.build();
                content = build.toByteArray();
            } else if (resultData.contains("sequence_id")) {
                log.debug("resultData：{}", resultData);
                BaiduRealtimeBidding.BidResponse.Builder builder = BaiduRealtimeBidding.BidResponse.newBuilder();
                JsonFormat.merge(resultData, builder);
                BaiduRealtimeBidding.BidResponse build = builder.build();
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
            log.error("timeMs:{},Exception:{}", end - start, e);
            MDC.remove("sift");
            byte[] content = null;
            response.setStatus(HttpResponseStatus.NO_CONTENT);
            ChannelBuffer buffer = new DynamicChannelBuffer(2048);
            try {
                content = "".getBytes("utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            buffer.writeBytes(content);
            response.setContent(buffer);
            ChannelFuture future1 = messageEvent.getChannel().write(response);
            future1.addListener(ChannelFutureListener.CLOSE);


        } finally {

            try {
                long end = System.currentTimeMillis();
                MDC.put("sift", configs.getString("ADX_REQUEST"));
                log.debug("timeMs:{},url:{},body:{},remoteIp:{}", end - start, url, dataStr, remoteIp);
                MDC.remove("sift");
                //ip黑名单bug修改
                ipBlackListFlag = 1;
                bundleBlackListFlag = 1;
                deviceIdBlackListFlag = 1;
                filterRuleBidRequestFlag = 1;
                AdTagBlackListFlag = 1;
                //不知道超时会不会增加
                if (url.contains("lingji")) {
                    adxId = 1;
                    BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
                    requestId = bidRequestBean.getId();
                    if (bidRequestBean.getApp() != null) {
                        appName = bidRequestBean.getApp().getName();
                        appPackageName = bidRequestBean.getApp().getBundle();
                    }
                    if (result != null) {
                        if (result.contains("ipBlackList")) {
                            ipBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("bundleBlackList")) {
                            bundleBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("deviceIdBlackList")) {
                            deviceIdBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("AdTagBlackLis")) {
                            AdTagBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("price\":")) {
                            bidPriceFlag = 1;
                            String substring = result.substring(result.indexOf("price\":"));
                            price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf("}"));
                        }
                    }
                } else if (url.contains("adview")) {
                    adxId = 2;
                    BidRequestBean bidRequestBean = JSON.parseObject(dataStr, BidRequestBean.class);
                    requestId = bidRequestBean.getId();
                    if (bidRequestBean.getApp() != null) {
                        appName = bidRequestBean.getApp().getName();
                        appPackageName = bidRequestBean.getApp().getBundle();
                    }
                    if (result != null) {
                        if (result.contains("ipBlackList")) {
                            ipBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("bundleBlackList")) {
                            bundleBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("deviceIdBlackList")) {
                            deviceIdBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("AdTagBlackList")) {
                            AdTagBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("price\":")) {
                            bidPriceFlag = 1;
                            String substring = result.substring(result.indexOf("price\":"));
                            price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf(",\""));
                        }
                    }
                } else if (url.contains("youyi")) {
                    adxId = 3;
                    YouYiBidRequest bidRequestBean = JSON.parseObject(dataStr, YouYiBidRequest.class);
                    requestId = bidRequestBean.getSession_id();
                    if (bidRequestBean.getMobile() != null) {
                        appName = bidRequestBean.getMobile().getApp_name();
                        appPackageName = bidRequestBean.getMobile().getApp_bundle();
                    }
                    if (result != null) {
                        if (result.contains("ipBlackList")) {
                            ipBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("bundleBlackList")) {
                            bundleBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("deviceIdBlackList")) {
                            deviceIdBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("AdTagBlackList")) {
                            AdTagBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("price\":")) {
                            bidPriceFlag = 1;
                            String substring = result.substring(result.indexOf("price\":"));
                            price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf(",\""));
                        }
                    }
                } else if (url.contains("tencent")) {
                    adxId = 4;
                    TencentBidRequest bidRequestBean = JSON.parseObject(dataStr, TencentBidRequest.class);
                    requestId = bidRequestBean.getId();
                    if (bidRequestBean.getApp() != null) {
                        appPackageName = bidRequestBean.getApp().getApp_bundle_id();
                    }
                    if (result != null) {
                        if (result.contains("ipBlackList")) {
                            ipBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("bundleBlackList")) {
                            bundleBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("deviceIdBlackList")) {
                            deviceIdBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("AdTagBlackList")) {
                            AdTagBlackListFlag = 0;
                            filterRuleBidRequestFlag = 0;
                        }
                        if (result.contains("price\":")) {
                            bidPriceFlag = 1;
                            String substring = result.substring(result.indexOf("price\":"));
                            price = substring.substring(substring.indexOf("\":") + 2, substring.indexOf(",\""));
                        }
                    }

                }

                MDC.put("phoenix", "rtb-houkp");
                log.debug("{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}\t{}" +
                                "\t{}\t{}\t{}\t{}\t{}\t{}\t{}",
                        LocalDateTime.now().toString(), new Date().getTime(),
                        LocalDate.now().toString(), LocalTime.now().getHour(),
                        LocalTime.now().getMinute(), requestId,
                        adxId, appName,
                        appPackageName, ipBlackListFlag,
                        bundleBlackListFlag, deviceIdBlackListFlag,
                        timeOutFlag, bidPriceFlag,
                        price, exceptionFlag, String.valueOf(filterRuleBidRequestFlag) + "," + AdTagBlackListFlag
                );
                //请求统计次数
                String countMapKey = hostAddress + "$" + adxId + "$" + appName + "$" + appPackageName;
                if (countMap.get(countMapKey) != null) {
                    Integer linkNum = countMap.get(countMapKey);
                    linkNum++;//连接次数 + 1
                    countMap.put(countMapKey, linkNum);
                } else {
                    countMap.put(countMapKey, countNum);
                }
                //出手统计次数
                String bidCountMapKey = hostAddress + "$" + adxId + "$" + appName + "$" + appPackageName;
                if (bidPriceFlag == 1 && timeOutFlag == 1 && exceptionFlag == 1) {
                    if (bidCountMap.get(bidCountMapKey) != null) {
                        Integer linkNum = bidCountMap.get(bidCountMapKey);
                        linkNum++;//连接次数 + 1
                        bidCountMap.put(bidCountMapKey, linkNum);
                    } else {
                        bidCountMap.put(bidCountMapKey, countNum);
                    }
                }

                MDC.remove("phoenix");


            } catch (Exception e1) {
                MDC.put("sift", "rtb-exception");
                log.error("最后的异常Exception:{}", e1);
                MDC.remove("sift");
            }
        }


    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        System.out.println(e.toString());

        // super.exceptionCaught(ctx, e);//不打印堆栈日志
    }

//    /**
//     * 解析进来的请求数据
//     *
//     * @throws UnsupportedEncodingException
//     */
//    public String parseRequest(String url, String dataStr, String remoteIp) throws Exception {
//        /**********		POST主业务逻辑		***************/
//        String resultData = parser.parseData(url, dataStr, remoteIp, executor,countMap);//SDK 2.0.1
//
//        byte[] content = null;
//        content = resultData.getBytes("utf-8");
//        return resultData;
//    }


}


