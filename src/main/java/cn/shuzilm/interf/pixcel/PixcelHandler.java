package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.interf.pixcel.parser.ParameterParser;
import cn.shuzilm.interf.pixcel.parser.ParameterParserFactory;
import cn.shuzilm.interf.pixcel.parser.RequestParser;
import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import cn.shuzilm.util.UrlParserUtil;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 对应不同版本的SDK，netty需要做相应的修改： 1、队列名 2、日志路径 3、maven包名，用端口号做区分
 *
 * @author
 */
public class PixcelHandler extends SimpleChannelUpstreamHandler {
    //	private WriteDataToLog wdt;
    RequestParser parser = null;
    private static AtomicInteger counter = new AtomicInteger();

    private static final Logger log = LoggerFactory.getLogger(RequestParser.class);

    private static ConcurrentHashMap<String, Object> requestParser;

    private static ParameterParser parameterParser;
    private ExecutorService executor;

    private String remoteIp = null;
    private String dataStr = null;
    private String url = null;
    //返回结果
    private String result = null;


    public PixcelHandler(ExecutorService executor, ConcurrentHashMap requestParser) {
        parser = new RequestParser();
        this.executor = executor;
        this.requestParser = requestParser;
        System.out.println(Thread.currentThread().getName() + " rtb parser 初始化成功。。。");
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        try {
            counter.getAndAdd(1);
            if (e.getMessage() instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) e.getMessage();
//				System.out.println("------------\r\n" + request.getUri());
                boolean close = HttpHeaders.Values.CLOSE
                        .equalsIgnoreCase(request
                                .getHeader(HttpHeaders.Names.CONNECTION))
                        || request.getProtocolVersion().equals(
                        HttpVersion.HTTP_1_0)
                        && !HttpHeaders.Values.KEEP_ALIVE
                        .equalsIgnoreCase(request
                                .getHeader(HttpHeaders.Names.CONNECTION));
                // 获取对方的ip地址
                remoteIp = ctx.getChannel().getRemoteAddress()
                        .toString().split(":")[0].replace("/", "");
                //接收 GET 请求
                url = request.getUri();
                MDC.put("sift", "url");
                log.debug("url:{}", url);
                //主业务逻辑  增加超时线程池
                List<String> urlParser = UrlParserUtil.urlParser(url);
                String redis = urlParser.get(0);
//                JedisQueueManager.putElementToQueue(redis,url, Priority.NORM_PRIORITY);
                Object o = requestParser.get(urlParser.get(0));
                if (o != null) {
                    parameterParser = ParameterParserFactory.getParameterParser(o.toString());
//
                    Future<Object> future = executor.submit(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
//                            主业务逻辑
                            return parameterParser.parseUrl(url);
                        }
                    });
                } else {
                    url = "没有对应的解析器";
                }

                //不接受内容，直接处理
                byte[] content = url.getBytes("utf-8");
                ChannelBuffer buffer = new DynamicChannelBuffer(2048);

                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                response.setHeader("Content-Type", "text/html");
                response.setHeader("Connection", HttpHeaders.Values.CLOSE);
                response.setHeader("Content-Length", content.length);
                response.setHeader("Accept-Ranges", "bytes");
                buffer.writeBytes(content);
                response.setContent(buffer);

                // Write the response.
                ChannelFuture future2 = e.getChannel().write(response);
                if (close) {
                    future2.addListener(ChannelFutureListener.CLOSE);
                }

            }
        } catch (Exception e2) {
            e2.printStackTrace();
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
    public byte[] parseRequest(String url, String dataStr, String remoteIp) throws UnsupportedEncodingException {
        /**********		GET主业务逻辑		***************/
        String resultData = parser.parseData(url, dataStr, remoteIp);//SDK 2.0.1
        byte[] content = {0};//c初始值
        if (StringUtils.isNotBlank(resultData)) {
            content = resultData.getBytes("utf-8");
        }
        return content;
    }


}