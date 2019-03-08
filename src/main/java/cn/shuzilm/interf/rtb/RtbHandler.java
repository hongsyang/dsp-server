package cn.shuzilm.interf.rtb;


import bidserver.BidserverSsp;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.tencent.request.TencentBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import com.alibaba.fastjson.JSON;
import com.googlecode.protobuf.format.JsonFormat;
import gdt.adx.GdtRtb;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: RtbHandler
 * @Author: houkp
 * @CreateDate: 2019/3/7 17:34
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/7 17:34
 * @UpdateRemark: netty 4.1.24
 * @Version: 1.0
 */
@ChannelHandler.Sharable//可以被共享使用
public class RtbHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private static final Logger log = LoggerFactory.getLogger(RtbHandler.class);

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    //各家adx解析器
    private static ConcurrentHashMap<String, Object> requestParser = null;

    //各家adx 服务器ip和连接次数
    private static ConcurrentHashMap<String, Integer> remoteIpGroup = new ConcurrentHashMap();
    //初始连接次数
    private static Integer remoteIpLinkNum = 1;

    private static ExecutorService executor = null;


    private static AtomicInteger atomicInteger = new AtomicInteger();

    private static FullHttpRequest httpRequest;

    private static RtbRequestParser parser = new RtbRequestParser();

    private static String remoteIp = null;
    private static String dataStr = "";
    private static String url = null;

    //log日志参数
    private String requestId = null;
    private Integer adxId = 0;
    private String appName = "";
    private String appPackageName = "";
    private Integer ipBlackListFlag = null;
    private Integer bundleBlackListFlag = null;
    private Integer deviceIdBlackListFlag = null;
    private Integer timeOutFlag = 1;
    private Integer bidPriceFlag = 0;
    private String price = "-1";
    private Integer exceptionFlag = 1;


    private String result = "";

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            log.debug("当前通道连接数为:{},连接的服务器ip:{}", atomicInteger.get(), remoteIpGroup);
        }, 0, 1, TimeUnit.MINUTES);
    }

    public RtbHandler(ExecutorService executor, ConcurrentHashMap<String, Object> requestParser) {
        this.executor = executor;
        this.requestParser = requestParser;
//        log.debug("executor:{},requestParser:{}", executor, requestParser);

    }


    /**
     * 业务处理
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {

        long start = System.currentTimeMillis();
        try {
            //开始时间
            //判断是否是 http请求
            if (!(msg instanceof FullHttpRequest)) {
                result = "未知请求!";
                send(ctx, result, url, 0);
                return;
            }
            //解析参数
            httpRequest = msg;
            this.remoteIp = ctx.channel().remoteAddress().toString().split(":")[0].replace("/", "");
            this.url = httpRequest.uri().replace("/", "");          //获取路径
            this.dataStr = getBody(httpRequest, url);     //获取参数
//            log.debug("dataStr ：{}", dataStr);
            HttpMethod method = httpRequest.method();//获取请求方法

            //获取get参数
//            if (HttpMethod.GET.equals(method)) {
//                //接受到的消息，做业务逻辑处理...
//                log.debug("GET请求 ：{}", url);
//                result=dataStr;
//                send(ctx, result, HttpResponseStatus.OK);
//                return;
//            }

            //获取post参数
            if (HttpMethod.POST.equals(method)) {
                //接受到的消息，做业务逻辑处理...
//                log.debug("POST请求 ：{}", dataStr);
                //增加超时线程池
                Future<Object> future = executor.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        //主业务逻辑
                        return result = parser.parseData(url, dataStr, remoteIp, requestParser);
                    }
                });
                //设置超时时间
                try {
                    result = (String) future.get(configs.getInt("TIME_OUT"), TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    timeOutFlag = 0;
                    long end = System.currentTimeMillis();
                    MDC.put("sift", "timeOut");
                    log.error("超时timeMs:{},url:{}", end - start, url);
                    MDC.remove("sift");
                    send(ctx, result, url, timeOutFlag);
                    future.cancel(true);// 中断执行此任务的线程
                    return;
                }

                //正常状态 返回结果
                send(ctx, result, url, 1);
            }

        } catch (Exception e) {
            exceptionFlag = 0;
            long end = System.currentTimeMillis();
            MDC.put("sift", "rtb-exception");
            log.debug("timeMs:{},Exception:{},url:{},body:{},remoteIp:{}", end - start, e.getMessage(), url, dataStr, remoteIp);
            log.error("timeMs:{},Exception:{}", end - start, e);
            send(ctx, result, url, exceptionFlag);

        } finally {
            try {
                long end = System.currentTimeMillis();
                MDC.put("sift", configs.getString("ADX_REQUEST"));
                log.debug("timeMs:{},url:{},body:{},remoteIp:{}", end - start, url, dataStr, remoteIp);
                MDC.remove("sift");


                //ip， 设备 ，媒体 黑名单
                ipBlackListFlag = 1;
                bundleBlackListFlag = 1;
                deviceIdBlackListFlag = 1;
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
                        bundleBlackListFlag, deviceIdBlackListFlag,
                        timeOutFlag, bidPriceFlag,
                        price, exceptionFlag
                );
                MDC.remove("phoenix");
            } catch (Exception e1) {
                MDC.put("sift", "rtb-exception");
                log.error("最后的异常Exception:{}", e1);
                MDC.remove("sift");
            }


        }

    }


    /**
     * 获取body参数
     *
     * @param request
     * @return
     */
    private String getBody(FullHttpRequest request, String url) throws Exception {
        //悠易请求
        BidserverSsp.BidRequest bidRequest = null;
        //广点通请求
        GdtRtb.BidRequest tencentBidRequest = null;

        //解决 IllegalReferenceCountException
        ByteBuf buf = request.content();
        buf.retain();
        if (request != null) {
            if (url.contains("youyi")) {
                bidRequest = BidserverSsp.BidRequest.parseFrom(request.content().array());
                dataStr = JsonFormat.printToString(bidRequest);
            } else if (url.contains("tencent")) {
                tencentBidRequest = GdtRtb.BidRequest.parseFrom(request.content().array());
                dataStr = JsonFormat.printToString(tencentBidRequest);
            } else {
                dataStr = buf.toString(CharsetUtil.UTF_8);
            }
        }
        return dataStr;
    }


    /**
     * 发送的返回值
     *
     * @param ctx    返回
     * @param result 消息
     * @param url    adx厂商
     */
    private void send(ChannelHandlerContext ctx, String result, String url, Integer exceptionStatus) {
        //设置返回头 默认状态为200
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.ACCEPT_RANGES, "bytes");
        //保持连接
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        //悠易返回结果
        BidserverSsp.BidResponse.Builder youyiBuilder = BidserverSsp.BidResponse.newBuilder();
        //广点通返回结果
        GdtRtb.BidResponse.Builder tencentbuilder = GdtRtb.BidResponse.newBuilder();

        //返回接口
        byte[] content = null;
        try {
            //大于0位正常状态
            if (url.contains("youyi")) {
                if (result.contains("204session_id")) {
                    String substring = result.substring(result.indexOf("204session_id") + 14);
                    youyiBuilder.setSessionId(substring);
//                    builder.setAds(0, BidserverSsp.BidResponse.Ad.newBuilder().build() );
                    content = youyiBuilder.build().toByteArray();
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
                    response.replace(Unpooled.copiedBuffer(content));
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    JsonFormat.merge(result, youyiBuilder);
                    BidserverSsp.BidResponse build = youyiBuilder.build();
                    content = build.toByteArray();
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
                    response.replace(Unpooled.copiedBuffer(content));
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            } else if (url.contains("tencent")) {
                JsonFormat.merge(result, tencentbuilder);
                GdtRtb.BidResponse build = tencentbuilder.build();
                content = build.toByteArray();
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
                response.replace(Unpooled.copiedBuffer(content));
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                if ("".equals(result) ||
                        "ipBlackList".equals(result) ||
                        "bundleBlackList".equals(result) ||
                        "deviceIdBlackList".equals(result) ||
                        exceptionStatus > 0
                        ) {
                    response.setStatus(HttpResponseStatus.NO_CONTENT);
                    result = "";
                }
                content = result.getBytes("utf-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
                response.replace(Unpooled.copiedBuffer(content));
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }


            //关闭通道
        } catch (Exception e) {
            MDC.put("sift", "send-message-exception");
            log.error("send-message-exception:{}", e);
        }
    }


    /**
     * 客户端连接
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            this.remoteIp = ctx.channel().remoteAddress().toString().split(":")[0].replace("/", "");
            atomicInteger.incrementAndGet();
//            log.debug("客户端ip:{}, Channel连接", remoteIp);
            if (remoteIpGroup.get(remoteIp) != null) {
                Integer linkNum = remoteIpGroup.get(remoteIp);
                linkNum++;//连接次数 + 1
                remoteIpGroup.put(remoteIp, linkNum);
            } else {
                remoteIpGroup.put(remoteIp, remoteIpLinkNum);
            }
        } catch (Exception e) {
            MDC.put("sift", "dsp-netty-exception");
            log.error("dsp-netty-exception", e);
            MDC.remove("sift");
        }

    }

    /**
     * 客户端断开
     *
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        try {
            this.remoteIp = ctx.channel().remoteAddress().toString().split(":")[0].replace("/", "");
            atomicInteger.decrementAndGet();
//            log.debug("客户端ip:{} ,Channel断开", remoteIp);
            if (remoteIpGroup.get(remoteIp) != null) {
                Integer linkNum = remoteIpGroup.get(remoteIp);
                linkNum--;//连接次数 - 1
                remoteIpGroup.put(remoteIp, linkNum);
            }
        } catch (Exception e) {
            MDC.put("sift", "dsp-netty-exception");
            log.error("dsp-netty-exception", e);
        }
    }


    /**
     * 异常捕获
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MDC.put("sift", "dsp-netty-exception");
        log.error("exceptionCaught:{}", cause.getStackTrace());
    }
}
