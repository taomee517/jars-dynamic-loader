package org.example.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.example.context.Attributes;
import org.example.context.ProtocolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @author 罗涛
 * @title ProtocolDispatchHandler
 * @date 2020/5/8 12:17
 */

@Slf4j
@Component
@ChannelHandler.Sharable
public class ProtocolDispatchHandler extends ChannelInboundHandlerAdapter implements Attributes {

    @Autowired
    ProtocolContext protocolContext;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            InetSocketAddress local = ((InetSocketAddress)ctx.channel().localAddress());
            InetSocketAddress remote = (msg instanceof DatagramPacket)?((DatagramPacket) msg).sender():((InetSocketAddress)ctx.channel().remoteAddress());
            log.info("收到数据：local={}, remote={}", local, remote);
            int port = local.getPort();
//            IMessageConverter messageConverter = protocolContext.getMessageConverterByPort(port);;
//            ctx.channel().attr(PROTOCOL).set(messageConverter);
            ctx.pipeline().remove(this);
        } finally {
            ctx.fireChannelRead(msg);
        }
    }
}
