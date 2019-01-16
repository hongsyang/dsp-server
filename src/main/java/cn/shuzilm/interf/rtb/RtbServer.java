package cn.shuzilm.interf.rtb;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.util.IpBlacklistUtil;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @Author： Wang Haiting
 * @Version： May 6, 20132:48:04 PM
 **/
public class RtbServer {
    private static RuleMatching ruleMatching;

    private static JedisManager jedisManager;

    private static IpBlacklistUtil ipBlacklist;


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static final Logger log = LoggerFactory.getLogger(RtbServer.class);

    //超时线程池
    private ExecutorService executor = Executors.newCachedThreadPool();


    public static void main(String[] args) {
        try {
            configs = AppConfigs.getInstance(FILTER_CONFIG);
            ipBlacklist = IpBlacklistUtil.getInstance();
            jedisManager = JedisManager.getInstance();
            ruleMatching = RuleMatching.getInstance();
            RtbServer server = new RtbServer();
            server.start(configs.getInt("RTB_PORT"));

        } catch (Exception e) {
            log.error("", e);
        }

    }


    public void start(int port) {
        // 配置服务器-使用java线程池作为解释线程
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newFixedThreadPool(3000), Executors.newCachedThreadPool(), 3000));
        // 设置 pipeline factory.
        bootstrap.setOption("child.tcpNoDelay", true); //注意child前缀
        bootstrap.setOption("child.keepAlive", true); //注意child前缀
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("child.linger", 60);
        bootstrap.setOption("child.TIMEOUT", 1);
        bootstrap.setOption("sendBufferSize", 1048576);
        bootstrap.setOption("writeBufferHighWaterMark", 10 * 64 * 1024);

        bootstrap.setOption("receiveBufferSize", 1048576);
        bootstrap.setPipelineFactory(new ServerPipelineFactory());
        // 绑定端口
        bootstrap.bind(new InetSocketAddress(port));
        System.out.println("admin start on " + port);
    }

    private class ServerPipelineFactory implements ChannelPipelineFactory {
        public ChannelPipeline getPipeline() {
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
            pipeline.addLast("aggregator", new HttpChunkAggregator(20480000));//设置块的最大字节数
            //http处理handler
            pipeline.addLast("handler", new RtbHandler(executor));

            return pipeline;
        }
    }
}