package org.example.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.api.IMessageConverter;
import org.example.context.Attributes;
import org.example.context.ProtocolContext;
import org.example.handler.DataActionHandler;
import org.example.handler.ProtocolDispatchHandler;
import org.example.handler.RecordHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 罗涛
 * @title NettyServer
 * @date 2020/12/8 10:48
 */

@Slf4j
@Component
public class NettyServer implements SmartLifecycle, InitializingBean {

    @Autowired
    ProtocolContext protocolContext;

    @Autowired
    ProtocolDispatchHandler dispatchHandler;

    @Autowired
    DataActionHandler dataActionHandler;


    boolean running = false;
    EventLoopGroup boss;
    EventLoopGroup workers;
//    ServerBootstrap tcpBootstrap;

    Map<Integer, Channel> channelMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void start() {
        initEventPool();
//        tcpBootstrap = buildTcpBootStrap();
        startInnerTcpServers();
        running = true;
    }

    @Override
    public void stop() {
        try {
            for (Channel channel : channelMap.values()) {
                channel.close().syncUninterruptibly();
            }
            boss.shutdownGracefully();
            workers.shutdownGracefully();
            boss.awaitTermination(30, TimeUnit.SECONDS);
            workers.awaitTermination(30, TimeUnit.SECONDS);
            running = false;
        } catch (Exception e) {
            log.error("MQTT server关闭时发生异常：" + e.getMessage(), e);
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private ServerBootstrap buildTcpBootStrap(){
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, workers)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .handler(new RecordHandler())
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        Integer interval = ch.attr(Attributes.INTERVAL).get();
                        pipeline.addFirst("idle", new IdleStateHandler(0, 0, interval, TimeUnit.SECONDS));
                        pipeline.addLast("data-action", dataActionHandler);
                    }
                });
        return bootstrap;
    }


    public void startInnerTcpServers() {
        try {
            Set<IMessageConverter> tcpConverters = protocolContext.tcpConverters;
            if (CollectionUtils.isEmpty(tcpConverters)) {
                log.warn("TCP协议解析器为空,请确认是否已注入Spring Context!!!");
                return;
            }
            for (IMessageConverter messageConverter : tcpConverters) {
                startSingleTcpServer(messageConverter);
            }
        } catch (Exception e) {
            log.error("TCP server初始化时发生异常：{0}", e);
        }
    }

    public void startSingleTcpServer(IMessageConverter messageConverter) throws InterruptedException {
        ServerBootstrap tcpBootstrap = buildTcpBootStrap();
        int port = messageConverter.port();
        int interval = messageConverter.interval();
        //配置定时器
        tcpBootstrap.childAttr(Attributes.INTERVAL, interval);
        //配置处理接口
        tcpBootstrap.childAttr(Attributes.PROTOCOL, messageConverter);
        ChannelFuture channelFuture = tcpBootstrap.bind(port).sync();
        String name = messageConverter.name();
        log.info("成功监听{}端口：{}", name, port);
        Channel channel = channelFuture.channel();
        channelMap.put(port, channel);
    }

    public void closeSingleChannel(int port){
        try {
            Channel channel = channelMap.get(port);
            if (Objects.nonNull(channel)) {
                channel.close();
                RecordHandler recordHandler = (RecordHandler)channel.pipeline().get("RecordHandler#0");
                recordHandler.closeAllChildrenChannel();
            }
        } catch (Exception e) {
            log.error("关闭channel时发生异常：" + e.getMessage(), e);
        }
    }

    /**
     * 初始化EventPool 参数
     */
    private void initEventPool() {
        int bossThreads = 1;
        int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
        if (useEpoll()) {
            boss = new EpollEventLoopGroup(bossThreads, new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "LINUX_BOSS_" + index.incrementAndGet());
                }
            });
            workers = new EpollEventLoopGroup(workerThreads, new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "LINUX_WORKER_" + index.incrementAndGet());
                }
            });
        } else {
            boss = new NioEventLoopGroup(bossThreads, new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "BOSS_" + index.incrementAndGet());
                }
            });
            workers = new NioEventLoopGroup(workerThreads, new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "WORKER_" + index.incrementAndGet());
                }
            });
        }
    }


    private boolean useEpoll() {
        String osName = System.getProperty("os.name");
        boolean linuxSystem = osName != null && osName.toLowerCase().contains("linux");
        return linuxSystem && Epoll.isAvailable();
    }
}
