package me.josephzhu.proxytest.layer7;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import me.josephzhu.proxytest.ProxyServer;
import org.springframework.stereotype.Component;

@Component("Layer7ProxyServer")
@Slf4j
public class Layer7ProxyServer extends ProxyServer {

    @Override
    protected ChannelInitializer<Channel> getChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(serverConfig.getMaxContentLength()));
                ch.pipeline().addLast(new FrontendHandler(serverConfig.getBackendIp(), serverConfig.getBackendPort(), serverConfig.getBackendThreadModel()));
            }
        };
    }
}
