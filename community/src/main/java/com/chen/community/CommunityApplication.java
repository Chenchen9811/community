package com.chen.community;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;


@SpringBootApplication
public class CommunityApplication {
    private static final Logger LOG = LoggerFactory.getLogger(CommunityApplication.class);

    @PostConstruct
    public void init() {
        // 解决netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommunityApplication.class);
        Environment environment = app.run(args).getEnvironment();
        LOG.info("启动成功！");
        LOG.info("地址：http://localhost:{}/community/index", environment.getProperty("server.port"));
    }



}
