package me.josephzhu.proxytest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties(ServerConfig.class)
@Slf4j
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ServerConfig serverConfig;

    @Override
    public void run(String... args) throws Exception {
        applicationContext.getBean(serverConfig.getType(), ProxyServer.class).start();
    }
}
