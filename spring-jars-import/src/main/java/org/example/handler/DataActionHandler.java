package org.example.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.example.api.IMessageConverter;
import org.example.context.Attributes;
import org.example.context.ChannelUtils;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title DataActionHandler
 * @date 2020/6/22 20:23
 */

@Slf4j
@Component
@ChannelHandler.Sharable
public class DataActionHandler extends ChannelDuplexHandler implements Attributes {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("与设备建立连接，remote:{}", channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            Channel channel = ctx.channel();
            log.warn("断开连接, local = {}, remote = {}", channel.localAddress(), channel.remoteAddress());
        } finally {
            ctx.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channel read!");
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IMessageConverter converter = ChannelUtils.getConverter(ctx);
            IdleStateEvent idleStateEvent = (IdleStateEvent)evt;
            IdleState state = idleStateEvent.state();
            switch (state) {
                case READER_IDLE:
                    // 检测最后消息时间是否已经超出最大时限，
                    // 主动断开连接
                    log.info("读超时！");
                    break;
                case WRITER_IDLE:
                    log.info("写超时！");
                    break;
                case ALL_IDLE:
                    log.warn("空闲channel：{}", ctx.channel());
                    log.info("interval message: {}",  converter.printMsg());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String remoteAddr = ctx.channel().remoteAddress().toString();
        if (remoteAddr.contains("100.")) {
            ctx.close();
            return;
        }
        log.error("DataActionHandler 发生异常：" + cause.getMessage(), cause);
        ctx.close();
    }
}
