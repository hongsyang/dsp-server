package cn.shuzilm.interf;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;


/**
 *@Author： Wang Haiting
 *@Version： May 6, 20132:48:04 PM
 **/
public class RtbServer {
	
	/** 保存所有的appKey*/
	public static HashSet<String> appKeySet;
	
	public static HashMap<String, String> appKeyIdMap;
	
	/** 创建数据库连接*/
//	public static MySqlConnection mySqlConnection ;
	public static Connection conn ;
	
	public static void main(String[] args) {

		appKeySet = new HashSet<String>();
		appKeyIdMap = new HashMap<String, String>();
//		mySqlConnection = new MySqlConnection("192.168.0.112", "distinguish", "root", "root");
//		conn = mySqlConnection.getConn();
		
		RtbServer server = new RtbServer();
		server.start(8080);
	}

	public void start(int port) {
		// 配置服务器-使用java线程池作为解释线程
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newFixedThreadPool(50), Executors.newCachedThreadPool(),50));
		// 设置 pipeline factory.
		bootstrap.setOption("child.tcpNoDelay", true); //注意child前缀
		bootstrap.setOption("child.keepAlive", true); //注意child前缀
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("child.linger", 60);
		bootstrap.setOption("child.TIMEOUT", 1);
//		bootstrap.setOption("sendBufferSize", 1048576);
//		bootstrap.setOption("writeBufferHighWaterMark", 10 * 64 * 1024);
//		
//		bootstrap.setOption("receiveBufferSize", 1048576);
		bootstrap.setPipelineFactory(new ServerPipelineFactory());
		// 绑定端口
		bootstrap.bind(new InetSocketAddress(port));
		System.out.println("admin start on "+port);
	}

	private class ServerPipelineFactory implements
			ChannelPipelineFactory {
		public ChannelPipeline getPipeline() throws Exception {
			// Create a default pipeline implementation.
			ChannelPipeline pipeline = Channels.pipeline();

//			pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
//			pipeline.addLast("decoder", new HttpRequestDecoder());
//		    pipeline.addLast("encoder", new HttpResponseEncoder());
//			//http处理handler
//			pipeline.addLast("handler", new PicHandler());
			pipeline.addLast("decoder", new HttpRequestDecoder());
			pipeline.addLast("encoder", new HttpResponseEncoder());
//		         pipeline.addLast("streamer", new ChunkedWriteHandler());
//            pipeline.addLast("aggregator", new HttpChunkAggregator(20480000));//设置块的最大字节数
			//http处理handler
			pipeline.addLast("handler", new RtbHandler());

			return pipeline;
		}
	}
}