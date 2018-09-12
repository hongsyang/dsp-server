package cn.shuzilm.interf.rtb;

import cn.shuzilm.interf.rtb.parser.RtbRequestParser;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
        System.out.println(Thread.currentThread().getName() + " parser 初始化成功。。。");
    }

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e){
		try {
//            System.out.println(Thread.currentThread().getName() + "\t" + counter);
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

				String remoteIp = ctx.getChannel().getRemoteAddress()
						.toString().split(":")[0].replace("/", "");
//				System.out.println("remoteIp : "+remoteIp);
				//接收 POST 请求 , 获取 SDK 回传数据
				String dataStr = URLDecoder.decode(new String(request.getContent().array(),"utf-8"));
//				String dataStr = new String(request.getContent().array(),"utf-8");
//				System.out.println("接收到的原始数据 --- "+dataStr);
//				dataStr = new String(EncryptionData.decrypt(EKEY, dataStr)
//						.getBytes(), "UTF-8");
				//接收 GET 请求
				String url = request.getUri();
//				System.out.println("uri : "+url);

				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
				ChannelBuffer buffer = new DynamicChannelBuffer(2048);

				//主业务逻辑
				byte[] content = parseRequest(url,dataStr, remoteIp);

				response.setHeader("Content-Type", "text/html");
				response.setHeader("Content-Length", content.length);
				response.setHeader("Accept-Ranges", "bytes");
                response.setHeader("Connection", HttpHeaders.Values.KEEP_ALIVE);
				buffer.writeBytes(content);
				//设置返回状态
				response.setStatus(HttpResponseStatus.ACCEPTED);
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
			log.error("",e2);
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
	 * @throws UnsupportedEncodingException
	 */
	public byte[] parseRequest(String url, String dataStr, String remoteIp) throws UnsupportedEncodingException {
		/**********		POST主业务逻辑		***************/
		String resultData = parser.parseData(url, dataStr,remoteIp);//SDK 2.0.1

		byte[] content = null;
		content = resultData.getBytes("utf-8");
		return content;
	}



}