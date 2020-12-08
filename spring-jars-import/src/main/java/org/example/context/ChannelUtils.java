package org.example.context;

import io.netty.channel.ChannelHandlerContext;
import org.example.api.IMessageConverter;

import java.net.InetSocketAddress;

/**
 * @author 罗涛
 * @title ChannelUtils
 * @date 2020/10/27 11:47
 */
public class ChannelUtils {

    public static int getLocalPort(ChannelHandlerContext ctx){
        InetSocketAddress local = ((InetSocketAddress)ctx.channel().localAddress());
        int port = local.getPort();
        return port;
    }

    public static IMessageConverter getConverter(ChannelHandlerContext ctx){
        return ctx.channel().attr(Attributes.PROTOCOL).get();
    }

    public static void putConverter(ChannelHandlerContext ctx, IMessageConverter converter){
        ctx.channel().attr(Attributes.PROTOCOL).setIfAbsent(converter);
    }
}
