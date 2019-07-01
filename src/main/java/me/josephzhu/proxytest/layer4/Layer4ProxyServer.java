package me.josephzhu.proxytest.layer4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import me.josephzhu.proxytest.ProxyServer;
import org.springframework.stereotype.Component;

@Component("Layer4ProxyServer")
@Slf4j
public class Layer4ProxyServer extends ProxyServer {

    @Override
    protected void config(ServerBootstrap b) {
        b.childOption(ChannelOption.AUTO_READ, false);
    }

    @Override
    protected ChannelInitializer<Channel> getChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new FrontendHandler(serverConfig.getBackendIp(), serverConfig.getBackendPort(), serverConfig.getBackendThreadModel()));
            }
        };
    }
}
