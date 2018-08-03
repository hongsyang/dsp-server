package cn.shuzilm.interf.pixcel;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;

/**
* @Description:    PixcelServer 用于监听曝光量和点击量
* @Author:         houkp
* @CreateDate:     2018/7/19 15:18
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 15:18
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class PixcelServer{


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

        PixcelServer server = new PixcelServer();
        server.start(8880);
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
        bootstrap.setOption("sendBufferSize", 1048576);
        bootstrap.setOption("writeBufferHighWaterMark", 10 * 64 * 1024);

        bootstrap.setOption("receiveBufferSize", 1048576);
        bootstrap.setPipelineFactory(new PixcelServer.ServerPipelineFactory());
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
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("encoder", new HttpResponseEncoder());
//		         pipeline.addLast("streamer", new ChunkedWriteHandler());
            pipeline.addLast("aggregator", new HttpChunkAggregator(20480000));//设置块的最大字节数
            //http处理handler
            pipeline.addLast("handler", new PixcelHandler());

            return pipeline;
        }
    }
}
