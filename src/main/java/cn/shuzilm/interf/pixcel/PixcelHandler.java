package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.ParameterParser;
import cn.shuzilm.interf.pixcel.parser.RequestParser;
import cn.shuzilm.util.Help;
import cn.shuzilm.util.HttpClientUtil;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 对应不同版本的SDK，netty需要做相应的修改： 1、队列名 2、日志路径 3、maven包名，用端口号做区分
 *
 * @author
 */
public class PixcelHandler extends SimpleChannelUpstreamHandler {

    RequestParser parser = null;
    private static AtomicInteger counter = new AtomicInteger();

    private static final Logger log = LoggerFactory.getLogger(PixcelHandler.class);


    private static final String PIXEL_CONFIG = "pixel.properties";

    private static AppConfigs configs = AppConfigs.getInstance(PIXEL_CONFIG);


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
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        try {
            counter.getAndAdd(1);
            if (messageEvent.getMessage() instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) messageEvent.getMessage();
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
                log.debug("url:{}，remoteIp:{}", url, remoteIp);
                //主业务逻辑  增加超时线程池
                String remote = "&remoteIp=" + remoteIp;
                List<String> urlParser = UrlParserUtil.urlParser(url);
                String redisStr = "";
                if (urlParser.size() > 0) {
                    redisStr = urlParser.get(0);
                    if (redisStr.contains("lingji") | redisStr.contains("adview") |
                            redisStr.contains("youyi") | redisStr.contains("tencent")
                            | redisStr.contains("baidu")) {
                        boolean b = RedisQueueManager.putElementToQueue(redisStr, url + remote, Priority.NORM_PRIORITY);
                        if (b) {

                        } else {
                            Help.sendAlert("发送到" + configs.getString("HOST") + "失败,PixcelHandler");
                            MDC.put("sift", "urlRedisError");
                            log.debug("url:{}，remoteIp:{}", url, remoteIp);
                        }
                    }

                }

                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

                if (url.contains("baiduclick")) {


                    String lpdAndClk = url.substring(url.indexOf("lpd") + 4);
                    MDC.put("sift", "baiduClick302");
                    log.debug("lpdAndClk:{}", lpdAndClk);
                    MDC.remove("sift");
                    String http = "http://";
                    String htppLpdUrl = "";
                    if (lpdAndClk != null) {
                        String[] split = lpdAndClk.split("&clk=");
                        if (split != null) {
                            String lpd = split[0];
                            if (lpd != null || "null".equals(lpd)) {
                                String[] split1 = lpd.split("\\?");
                                String replace = split1[0].replace("_", ".").replace("-", "/");
                                if (split1.length > 1) {
                                    replace = replace + "?" + split1[1];
                                }
                                htppLpdUrl = http + replace;
                            }

                            //百度302 转发
                            response.setStatus(HttpResponseStatus.FOUND);
                            response.setHeader("location", htppLpdUrl);

                            //第三方点击监测
                            String clk = split[1];
                            if (clk != null || "null".equals(clk)) {
                                String[] clkSplit = clk.split("\\?");
                                String clkreplace = clkSplit[0].replace("_", ".").replace("-", "/");
                                if (clkSplit.length > 1) {
                                    clkreplace = clkreplace + "?" + clkSplit[1];
                                }
                                String htppClkUrl = http + clkreplace;
                                //触发点击监测
                                HttpClientUtil.get(htppClkUrl);
                            }

                        }

                    }


                } else {
                    //不做处理直接返回
                    byte[] content = redisStr.getBytes("utf-8");
                    ChannelBuffer buffer = new DynamicChannelBuffer(2048);

                    response.setHeader("Content-Type", "text/html");
                    response.setHeader("Connection", HttpHeaders.Values.CLOSE);
                    response.setHeader("Content-Length", content.length);
                    response.setHeader("Accept-Ranges", "bytes");
                    buffer.writeBytes(content);
                    response.setContent(buffer);
                }


                // Write the response.
                ChannelFuture future2 = messageEvent.getChannel().write(response);
                if (close) {
                    future2.addListener(ChannelFutureListener.CLOSE);
                }

            }
        } catch (Exception e) {
            Help.sendAlert("发送到" + configs.getString("HOST") + "失败,PixcelHandler");
            MDC.put("sift", "pixcelException");
            log.debug("url:{}，remoteIp:{}", url, remoteIp, e);
            log.debug("异常信息e:{}", e);
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
