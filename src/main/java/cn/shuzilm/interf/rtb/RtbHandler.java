package cn.shuzilm.interf.rtb;


import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
@ChannelHandler.Sharable
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

    private String result = "";

    public RtbHandler(ExecutorService executor, ConcurrentHashMap<String, Object> requestParser) {
        this.executor = executor;
        this.requestParser = requestParser;
        log.debug("executor:{},requestParser:{}", executor, requestParser);
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            log.debug("当前连接数为:{}", atomicInteger.get());
//        }, 0, 3, TimeUnit.SECONDS);
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
        try {
            long start = new Date().getTime();
//            log.debug("msg:{}", msg);
            if (!(msg instanceof FullHttpRequest)) {
                result = "未知请求!";
                send(ctx, result, HttpResponseStatus.OK);
                return;
            }
            httpRequest = msg;

            this.remoteIp = ctx.channel().remoteAddress().toString().split(":")[0].replace("/", "");
            this.url = httpRequest.uri().replace("/", "");          //获取路径
            this.dataStr = getBody(httpRequest, url);     //获取参数
            HttpMethod method = httpRequest.method();//获取请求方法
            if (HttpMethod.GET.equals(method)) {
                //接受到的消息，做业务逻辑处理...
                log.debug("GET请求 ：{}", url);
                send(ctx, result, HttpResponseStatus.OK);
                return;
            }

            //获取post参数
            if (HttpMethod.POST.equals(method)) {
                //接受到的消息，做业务逻辑处理...
                log.debug("POST请求 ：{}", dataStr);
                //返回结果
                result = parser.parseData(url, dataStr, remoteIp, requestParser);
                send(ctx, result, HttpResponseStatus.OK);
            }


        } catch (Exception e) {
            MDC.put("sift", "rtb-exception");
            try {
                send(ctx, result, HttpResponseStatus.NO_CONTENT);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            log.error("", e);
        }

    }


    /**
     * 获取body参数
     *
     * @param request
     * @return
     */
    private String getBody(FullHttpRequest request, String url) throws Exception {
        if (request != null) {
            ByteBuf buf = request.content();
            //解决 IllegalReferenceCountException
            buf.retain();
            dataStr = buf.toString(CharsetUtil.UTF_8);
        }
        return dataStr;
    }


    /**
     * 发送的返回值
     *
     * @param ctx    返回
     * @param result 消息
     * @param status 状态
     */
    private void send(ChannelHandlerContext ctx, String result, HttpResponseStatus status) throws Exception {
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.ACCEPT_RANGES, "bytes");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

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
            log.debug("客户端ip:{} 连接", remoteIp);
            if (remoteIpGroup.get(remoteIp) != null) {
                Integer linkNum = remoteIpGroup.get(remoteIp);
                linkNum++;//连接次数 + 1
                remoteIpGroup.put(remoteIp, linkNum);
            } else {
                remoteIpGroup.put(remoteIp, remoteIpLinkNum);
            }
        } catch (Exception e) {
            MDC.put("sift", "dsp-netty-exception");
            log.error("", e);
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
            log.debug("客户端ip:{} 断开", remoteIp);
            if (remoteIpGroup.get(remoteIp) != null) {
                Integer linkNum = remoteIpGroup.get(remoteIp);
                linkNum--;//连接次数 - 1
                remoteIpGroup.put(remoteIp, linkNum);
            }
        } catch (Exception e) {
            MDC.put("sift", "dsp-netty-exception");
            log.error("", e);
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
        log.error("exceptionCaught:{}", cause);
    }
}
