package org.example.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author 罗涛
 * @title RecordHandler
 * @date 2020/12/8 17:07
 */
@ChannelHandler.Sharable
public class RecordHandler extends SimpleChannelInboundHandler<NioSocketChannel> {
    private Set<Channel> channelSet;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NioSocketChannel msg) throws Exception {
        try {
            if (Objects.isNull(channelSet)) {
                channelSet = new HashSet<>();
                channelSet.add(msg);
            }else {
                channelSet.add(msg);
            }
        } finally {
            ctx.fireChannelRead(msg);
        }
    }

    public void closeAllChildrenChannel(){
        if(Objects.nonNull(channelSet) && channelSet.size()>0){
            for (Channel ch : channelSet) {
                ch.close();
            }
        }
    }
}
