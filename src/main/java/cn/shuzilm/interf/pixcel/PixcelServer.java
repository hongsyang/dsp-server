package cn.shuzilm.interf.pixcel;

import cn.shuzilm.backend.timing.pixel.PixelCronDispatch;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.interf.pixcel.parser.AdViewClickParameterParserImpl;
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
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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


    //扫描包
    private static Reflections reflections = new Reflections("cn.shuzilm.interf.pixcel.parser");
    //加载所有的实现接口的类
    private static Set<Class<? extends ParameterParser>> subTypesOf;
    //超时线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("EXECUTOR_THREADS"));
    //创建requestParser 解析的map
    private static ConcurrentHashMap<String, Object> requestParser = null;

    public static void main(String[] args) {
        subTypesOf = reflections.getSubTypesOf(ParameterParser.class);
        requestParser = createMap(subTypesOf);
        //从redis中取数据
        startPixcelPaeser();
        System.out.println("开始启动服务");
        PixelCronDispatch.startPixelDispatch();
        PixcelServer server = new PixcelServer();
        server.start(configs.getInt("PIXCEL_PORT"));
    }

    private static void startPixcelPaeser() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Object adviewclick = JedisQueueManager.getElementFromQueue("adviewclick");
                    Object adviewexp = JedisQueueManager.getElementFromQueue("adviewexp");
                    Object adviewnurl = JedisQueueManager.getElementFromQueue("adviewnurl");
                    Object lingjiclick = JedisQueueManager.getElementFromQueue("lingjiclick");
                    Object lingjiexp = JedisQueueManager.getElementFromQueue("lingjiexp");
                    Object youyiclick = JedisQueueManager.getElementFromQueue("youyiclick");
                    Object youyiexp = JedisQueueManager.getElementFromQueue("youyiexp");
                    Object youyiimp = JedisQueueManager.getElementFromQueue("youyiimp");
                    System.out.println(1);
                    if (adviewclick != null) {

                    }
                }

            }
        });

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
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newFixedThreadPool(configs.getInt("N_THREADS")), Executors.newCachedThreadPool(), configs.getInt("N_THREADS")));
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
            pipeline.addLast("handler", new PixcelHandler(executor, requestParser));
            return pipeline;
        }
    }
}
