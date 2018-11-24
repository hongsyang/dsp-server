package cn.shuzilm.interf.rtb;

import bidserver.BidserverSsp;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 对应不同版本的SDK，netty需要做相应的修改： 1、队列名 2、日志路径 3、maven包名，用端口号做区分
 *
 * @author
 */
public class RtbHandler extends SimpleChannelUpstreamHandler {

    private static final Logger log = LoggerFactory.getLogger(RtbHandler.class);
    //	private WriteDataToLog wdt;
    RtbRequestParser parser = null;
    private static AtomicInteger counter = new AtomicInteger();

    public RtbHandler() {
//		wdt = new WriteDataToLog();
        parser = new RtbRequestParser();
        System.out.println(Thread.currentThread().getName() + " rtb parser 初始化成功。。。");
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        try {
//            System.out.println(Thread.currentThread().getName() + "\t" + counter);
            counter.getAndAdd(1);
            if (e.getMessage() instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) e.getMessage();
                boolean close = HttpHeaders.Values.CLOSE
                        .equalsIgnoreCase(request
                                .getHeader(HttpHeaders.Names.CONNECTION))
                        || request.getProtocolVersion().equals(
                        HttpVersion.HTTP_1_0)
                        && !HttpHeaders.Values.KEEP_ALIVE
                        .equalsIgnoreCase(request
                                .getHeader(HttpHeaders.Names.CONNECTION));
//				System.out.println("------------\r\n" + request.getUri());
                // 获取对方的ip地址

                String remoteIp = ctx.getChannel().getRemoteAddress()
                        .toString().split(":")[0].replace("/", "");
//				System.out.println("remoteIp : "+remoteIp);
                //接收 POST 请求 , 获取 SDK 回传数据
//                log.debug(request.getContent().toString());
                String dataStr = null;
                String url = request.getUri();
                if (url.contains("youyi")) {
                    BidserverSsp.BidRequest bidRequest = BidserverSsp.BidRequest.parseFrom(request.getContent().array());
                    dataStr = JsonFormat.printToString(bidRequest);
                } else {
                    dataStr = URLDecoder.decode(new String(request.getContent().array(), "utf-8"));
                }
//				String dataStr = new String(request.getContent().array(),"utf-8");
//				System.out.println("接收到的原始数据 --- "+dataStr);
//				dataStr = new String(EncryptionData.decrypt(EKEY, dataStr)
//						.getBytes(), "UTF-8");
                //接收 GET 请求
//				System.out.println("uri : "+url);

                HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
                ChannelBuffer buffer = new DynamicChannelBuffer(2048);
                //主业务逻辑
                byte[] content = null;
                String resultData = parseRequest(url, dataStr, remoteIp);

                if ("".equals(resultData)) {
                    response.setStatus(HttpResponseStatus.NO_CONTENT);
                    content=  resultData.getBytes("utf-8");
                } else if (resultData.contains("session_id")) {
                    BidserverSsp.BidResponse.Builder builder = BidserverSsp.BidResponse.newBuilder();
                    JsonFormat.merge(resultData, builder);
                    BidserverSsp.BidResponse build = builder.build();
                    content = build.toByteArray();
                }else {
                    content = resultData.getBytes("utf-8");
                }
                response.setHeader("Content-Type", "text/html");
                response.setHeader("Connection", HttpHeaders.Values.KEEP_ALIVE);
                response.setHeader("Content-Length", content.length);
                response.setHeader("Accept-Ranges", "bytes");

                buffer.writeBytes(content);
                //设置返回状态
                response.setContent(buffer);

                // Write the response.
                ChannelFuture future2 = e.getChannel().write(response);
                if (close) {
                    future2.addListener(ChannelFutureListener.CLOSE);
                }

//				Channel ch = e.getChannel();
                // ch.write(response);
//				ch.disconnect();
//				ch.close();
            }
        } catch (Exception e2) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            ChannelFuture future2 = e.getChannel().write(response);
            future2.addListener(ChannelFutureListener.CLOSE);
            log.error("", e2);
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
    public String parseRequest(String url, String dataStr, String remoteIp) throws Exception {
        /**********		POST主业务逻辑		***************/
        String resultData = parser.parseData(url, dataStr, remoteIp);//SDK 2.0.1

//		byte[] content = null;
//		content = resultData.getBytes("utf-8");
        return resultData;
    }


}

