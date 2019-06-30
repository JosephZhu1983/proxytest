package me.josephzhu.proxytest.layer4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import me.josephzhu.proxytest.ProxyServer;
import me.josephzhu.proxytest.ServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("Layer4ProxyServer")
@Slf4j
public class Layer4ProxyServer extends ProxyServer {

    @Autowired
    ServerConfig serverConfig;

    public static final EventLoopGroup serverBossGroup = new NioEventLoopGroup();
    public static final EventLoopGroup serverWorkerGroup = new NioEventLoopGroup();
    public static final EventLoopGroup backendWorkerGroup = new NioEventLoopGroup();

    @Override
    public void start() throws Exception {

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(serverBossGroup, serverWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new FrontendHandler(serverConfig.getBackendIp(), serverConfig.getBackendPort(), serverConfig.getBackendThreadModel()));
                        }
                    })
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(serverConfig.getServerIp(), serverConfig.getServerPort())
                    .addListener(future -> log.info("{} Started with config: {}", getClass().getSimpleName(), serverConfig))
                    .sync().channel().closeFuture().sync();
        } finally {
            serverBossGroup.shutdownGracefully();
            serverWorkerGroup.shutdownGracefully();
        }
    }
}
