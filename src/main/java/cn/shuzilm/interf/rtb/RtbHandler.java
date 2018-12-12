package cn.shuzilm.interf.rtb;

import bidserver.BidserverSsp;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import com.googlecode.protobuf.format.JsonFormat;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
    //返回结果
    private String result = null;


    public RtbHandler(ExecutorService executor) {
        parser = new RtbRequestParser();
        this.executor = executor;
        System.out.println(Thread.currentThread().getName() + " rtb parser 初始化成功。。。");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
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
        if (e.getMessage() instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) e.getMessage();
            // 请求状态
            close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));
            // 获取对方的ip地址
            remoteIp = ctx.getChannel().getRemoteAddress().toString().split(":")[0].replace("/", "");
            //接收 GET 请求
            url = request.getUri();
            //接收 POST 请求 , 获取 SDK 回传数据
            dataStr = new String(request.getContent().array());
            try {
                BidserverSsp.BidRequest bidRequest = null;
                if (url.contains("youyi")) {
                    bidRequest = BidserverSsp.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(bidRequest);
                } else {
                    dataStr = URLDecoder.decode(dataStr, "utf-8");
                }
            } catch (Exception e1) {
                log.debug(" 异常：{}，接收 POST 请求", e1, dataStr);
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
        } catch (InterruptedException e1) {
            log.error("线程中断出错：{}", e1);
            future.cancel(true);// 中断执行此任务的线程
        } catch (ExecutionException e1) {
            log.error("线程服务出错{}", e1);
            future.cancel(true);// 中断执行此任务的线程
        } catch (TimeoutException e1) {
            // 超时情况
            long end = System.currentTimeMillis();
            MDC.put("sift", "timeOut");
            log.error("timeMs:{},url:{}", end - start, url);
            MDC.remove("sift");
            response.setStatus(HttpResponseStatus.NO_CONTENT);
            ChannelFuture future1 = e.getChannel().write(response);
            future1.addListener(ChannelFutureListener.CLOSE);
            System.out.println(Thread.currentThread().getName()+"是否执行完毕"+future.isDone());
            future.cancel(true);// 中断执行此任务的线程
            System.out.println(Thread.currentThread().getName()+"是否执行完毕"+future.isDone());
            return;
        }

        //正常情况 主业务逻辑
        byte[] content = null;
        String resultData = result;
        try {
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
        } catch (Exception e2) {
            log.error("转换异常:{},resultData:{}", e2, resultData);
        }

        response.setHeader("Content-Length", content.length);
        ChannelBuffer buffer = new DynamicChannelBuffer(2048);
        buffer.writeBytes(content);
        response.setContent(buffer);

        //正常返回
        ChannelFuture future2 = e.getChannel().write(response);
        if (close) {
            future2.addListener(ChannelFutureListener.CLOSE);
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

