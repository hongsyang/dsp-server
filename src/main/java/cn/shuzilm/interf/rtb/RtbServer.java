package cn.shuzilm.interf.rtb;

import cn.shuzilm.backend.rtb.RuleMatching;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.redis.RedisManager;
import cn.shuzilm.interf.pixcel.parser.ParameterParser;
import cn.shuzilm.interf.rtb.parser.RequestService;
import cn.shuzilm.util.IpBlacklistUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
* @Description:    RtbServer
* @Author:         houkp
* @CreateDate:     2019/3/7 17:32
* @UpdateUser:     houkp
* @UpdateDate:     2019/3/7 17:32
* @UpdateRemark:   netty 4.1.24
* @Version:        1.0
*/
public class RtbServer {


    private static RuleMatching ruleMatching;

    private static JedisManager jedisManager;

    private static RedisManager redisManager;

    private static IpBlacklistUtil ipBlacklist;


    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = null;

    private static final Logger log = LoggerFactory.getLogger(RtbServer.class);

    private ServerBootstrap bootstrap;
    //主线程
    private EventLoopGroup bossGroup;
    //工作线程
    private EventLoopGroup workerGroup;
    //超时线程池
    private ExecutorService executor = Executors.newFixedThreadPool(configs.getInt("RTB_EXECUTOR_THREADS"));

    //扫描包
    private static Reflections reflections = new Reflections("cn.shuzilm.interf.rtb.parser");
    //加载所有的实现接口的类
    private static Set<Class<? extends RequestService>> subTypesOf;
    //创建requestParser 解析的map
    private static ConcurrentHashMap<String, Object> requestParser = null;


    public static void main(String[] args) {
        try {
            MDC.put("sift", "dsp-server");
            configs = AppConfigs.getInstance(FILTER_CONFIG);
            //初始化 加载配置
            ipBlacklist = IpBlacklistUtil.getInstance();
            //TODO redis的线程数bug
            jedisManager = JedisManager.getInstance();
            redisManager = RedisManager.getInstance();
            ruleMatching = RuleMatching.getInstance();
            subTypesOf = reflections.getSubTypesOf(RequestService.class);
//            for (Class<? extends RequestService> aClass : subTypesOf) {
//                System.out.println(aClass.getName());
//            }
            requestParser = createMap(subTypesOf);
            RtbServer server = new RtbServer();
            server.start(configs.getInt("RTB_PORT"));

        } catch (Exception e) {
            MDC.put("sift", "dsp-netty-exception");
            log.error("", e);
        }

    }

    /**
     * 扫描宝加入实现类
     *
     * @param subTypesOf
     * @return
     */
    private static ConcurrentHashMap<String, Object> createMap(Set<Class<? extends RequestService>> subTypesOf) {
        ConcurrentHashMap map = new ConcurrentHashMap();
        String oldChar = "class cn.shuzilm.interf.rtb.parser.";
        for (Object o : subTypesOf) {
            map.put(o.toString().replace(oldChar, "").toLowerCase().replace("requestserviceimpl", ""), o.toString().replace("class ", ""));
        }
        log.debug("map:{}", map);
        return map;
    }


    public void start(int port) {

        // boss线程池
        log.debug("BOSS_THREADS :{}", configs.getInt("BOSS_THREADS"));
        //TODO 线程池参考阿里框架
        bossGroup = new NioEventLoopGroup(configs.getInt("BOSS_THREADS"));
        // worker线程池
        log.debug("WORK_THREADS :{}", configs.getInt("WORK_THREADS"));
        workerGroup = new NioEventLoopGroup(configs.getInt("WORK_THREADS"));


        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        // 使用TCP
        bootstrap.channel(NioServerSocketChannel.class);
        // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
        //如果队列已满，客户端连接将被拒绝。默认值，128。
        bootstrap.option(ChannelOption.SO_BACKLOG, 128);
        // 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。为了尽可能的利用网络带宽，TCP总是希望尽可能的发送足够大的数据。这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。
        //默认是false。设置为true：表示实时发送
        bootstrap.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
        //SO_REUSEADDR允许启动一个监听服务器并捆绑其众所周知端口，即使以前建立的将此端口用做他们的本地端口的连接仍存在。这通常是重启监听服务器时出现，若不设置此选项，则bind时将出错。
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE);
        //Netty4使用对象池，重用缓冲区
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        // 初始化配置的处理器
        bootstrap.childHandler(new ServerPipelineFactory());
        try {
            log.debug("admin start on :{}", port);
            ChannelFuture f = bootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("服务启动异常", e);
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    private class ServerPipelineFactory extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {

            ChannelPipeline pipeline = ch.pipeline();


            // ----Protobuf处理器，这里的配置是关键----
//            pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());// 用于decode前解决半包和粘包问题（利用包头中的包含数组长度来识别半包粘包）
            //配置Protobuf解码处理器，消息接收到了就会自动解码，ProtobufDecoder是netty自带的，Message是自己定义的Protobuf类
//            pipeline.addLast("protobufDecoder", new ProtobufDecoder(BidserverSsp.BidRequest.getDefaultInstance()));
            // 用于在序列化的字节数组前加上一个简单的包头，只包含序列化的字节长度。
//            pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
            //配置Protobuf编码器，发送的消息会先经过编码
//            pipeline.addLast("protobufEncoder", new ProtobufEncoder());

//            pipeline.addLast(new ReadTimeoutHandler(60));
//            pipeline.addLast(new WriteTimeoutHandler(10, TimeUnit.MILLISECONDS));
            //http服务器端对request解码
            pipeline.addLast("decoder", new HttpRequestDecoder());
            //http服务器端对response编码
            pipeline.addLast("encoder", new HttpResponseEncoder());
            //将多个消息转化成一个
            pipeline.addLast("http-aggregator",new HttpObjectAggregator(65535));
            //解决大码流的问题
//            pipeline.addLast("http-chunked",new ChunkedWriteHandler());
            //http处理handler
            pipeline.addLast("handler", new RtbHandler(executor,requestParser));


        }
    }




}