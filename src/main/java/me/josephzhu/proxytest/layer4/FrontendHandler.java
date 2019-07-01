package me.josephzhu.proxytest.layer4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.josephzhu.proxytest.BackendThreadModel;

public class FrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;
    private final BackendThreadModel backendThreadModel;
    private Channel outboundChannel;

    public FrontendHandler(String remoteHost, int remotePort, BackendThreadModel backendThreadModel) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.backendThreadModel = backendThreadModel;
    }

    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        Bootstrap b = new Bootstrap();
        switch (backendThreadModel) {
            case ReuseServerGroup: {
                b.group(Layer4ProxyServer.serverWorkerGroup);
                break;
            }
            case IndividualGroup: {
                b.group(Layer4ProxyServer.backendWorkerGroup);
                break;
            }
            case ReuseServerThread: {
                b.group(inboundChannel.eventLoop());
                break;
            }
            default:
                break;
        }
        b.option(ChannelOption.AUTO_READ, true)
                .channel(NioSocketChannel.class)
                .handler(new BackendHandler(inboundChannel));
        ChannelFuture f = b.connect(remoteHost, remotePort).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                inboundChannel.read();
            } else {
                inboundChannel.close();
            }
        });
        outboundChannel = f.channel();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }
}
