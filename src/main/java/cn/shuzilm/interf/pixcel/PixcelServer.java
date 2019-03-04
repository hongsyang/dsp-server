package cn.shuzilm.interf.pixcel;

import cn.shuzilm.backend.timing.pixel.PixelCronDispatch;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.interf.pixcel.parser.ParameterParser;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.reflections.Reflections;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: PixcelServer 用于监听曝光量和点击量
 * @Author: houkp
 * @CreateDate: 2018/7/19 15:18
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/19 15:18
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class PixcelServer {


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);
    //业务线程池
    private static ExecutorService executor1 = Executors.newFixedThreadPool(configs.getInt("CLICK_THREADS"));
    private static ExecutorService executor2 = Executors.newFixedThreadPool(configs.getInt("LINGJI_THREADS"));
    private static ExecutorService executor3 = Executors.newFixedThreadPool(configs.getInt("YOUYI_THREADS"));
    private static ExecutorService executor4 = Executors.newFixedThreadPool(configs.getInt("ADVIEW_THREADS"));
    private static ExecutorService executor5 = Executors.newFixedThreadPool(configs.getInt("TENCENT_THREADS"));

    //扫描包
    private static Reflections reflections = new Reflections("cn.shuzilm.interf.pixcel.parser");
    //加载所有的实现接口的类
    private static Set<Class<? extends ParameterParser>> subTypesOf;
    //创建requestParser 解析的map
    private static ConcurrentHashMap<String, Object> requestParser = null;

    public static void main(String[] args) {
        subTypesOf = reflections.getSubTypesOf(ParameterParser.class);
        requestParser = createMap(subTypesOf);
        PixelCronDispatch.startPixelDispatch();
        PixcelServer server = new PixcelServer();
        System.out.println("服务开始---------------------");
        server.start(configs.getInt("PIXCEL_PORT"));
        //从redis中取数据

        ClickRedisTask clickRedisTask = new ClickRedisTask();
        LingJiRedisTask lingJiRedisTask = new LingJiRedisTask();
        AdviewRedisTask adviewRedisTask = new AdviewRedisTask();
        TencentRedisTask tencentRedisTask = new TencentRedisTask();
        YouYiRedisTask youYIRedisTask = new YouYiRedisTask();
        for (int i = 0; i < configs.getInt("EXECUTOR_THREADS"); i++) {
            executor1.execute(clickRedisTask);
            executor2.execute(lingJiRedisTask);
            executor3.execute(youYIRedisTask);
            executor4.execute(adviewRedisTask);
            executor5.execute(tencentRedisTask);
        }
    }


    /**
     * 创建requestParser 解析的map
     *
     * @param subTypesOf
     * @return
     */
    private static ConcurrentHashMap<String, Object> createMap(Set subTypesOf) {
        ConcurrentHashMap map = new ConcurrentHashMap();
        String oldChar = "class cn.shuzilm.interf.pixcel.parser.";
        for (Object o : subTypesOf) {
            map.put(o.toString().replace(oldChar, "").toLowerCase().replace("parameterparserimpl", ""), o.toString().replace("class ", ""));
        }
        System.out.println("map:" + map.toString());
        return map;
    }


    public void start(int port) {
        // 配置服务器-使用java线程池作为解释线程
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newFixedThreadPool(configs.getInt("BOSS_THREADS")), Executors.newCachedThreadPool(), configs.getInt("WORK_THREADS")));
        // 设置 pipeline factory.
        bootstrap.setOption("child.tcpNoDelay", true); //注意child前缀
        bootstrap.setOption("child.keepAlive", false); //注意child前缀
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("child.linger", 60);
        bootstrap.setOption("child.TIMEOUT", 1);
        bootstrap.setOption("sendBufferSize", 1048576);
        bootstrap.setOption("writeBufferHighWaterMark", 10 * 64 * 1024);
        bootstrap.setOption("receiveBufferSize", 1048576);
        bootstrap.setPipelineFactory(new PixcelServer.ServerPipelineFactory());
        // 绑定端口
        bootstrap.bind(new InetSocketAddress(port));
        System.out.println("admin start on " + port);
    }

    private class ServerPipelineFactory implements
            ChannelPipelineFactory {
        public ChannelPipeline getPipeline() throws Exception {
            // Create a default pipeline implementation.
            ChannelPipeline pipeline = Channels.pipeline();

//			pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("encoder", new HttpResponseEncoder());
//		         pipeline.addLast("streamer", new ChunkedWriteHandler());
            pipeline.addLast("aggregator", new HttpChunkAggregator(20480000));//设置块的最大字节数
            //http处理handler
            pipeline.addLast("handler", new PixcelHandler(executor1, requestParser));
            return pipeline;
        }
    }
}
