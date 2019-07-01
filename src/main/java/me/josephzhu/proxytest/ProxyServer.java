package me.josephzhu.proxytest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class ProxyServer {

    public static final EventLoopGroup serverBossGroup = new NioEventLoopGroup();
    public static final EventLoopGroup serverWorkerGroup = new NioEventLoopGroup();
    public static final EventLoopGroup backendWorkerGroup = new NioEventLoopGroup();
    @Autowired
    protected ServerConfig serverConfig;

    protected abstract ChannelInitializer<Channel> getChannelInitializer();

    protected void config(ServerBootstrap b) {
    }

    public void start() throws Exception {

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(serverBossGroup, serverWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(getChannelInitializer());
            b.childOption(ChannelOption.SO_RCVBUF, serverConfig.getReceiveBuffer())
                    .childOption(ChannelOption.SO_SNDBUF, serverConfig.getSendBuffer());
            if (serverConfig.getAllocatorType() == AllocatorType.Pooled)
                b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            if (serverConfig.getAllocatorType() == AllocatorType.Unpooled)
                b.childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
            config(b);
            b.bind(serverConfig.getServerIp(), serverConfig.getServerPort())
                    .addListener(future -> log.info("{} Started with config: {}", getClass().getSimpleName(), serverConfig))
                    .sync().channel().closeFuture().sync();
        } finally {
            serverBossGroup.shutdownGracefully();
            serverWorkerGroup.shutdownGracefully();
        }
    }
}
