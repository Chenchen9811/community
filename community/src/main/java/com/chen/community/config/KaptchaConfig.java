package com.chen.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("keptcha.image.height", "40");
        properties.setProperty("keptcha.textproducer.font.size", "32");
        properties.setProperty("keptcha.textproducer.font.color", "0, 0, 0");
        properties.setProperty("keptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("keptcha.textproducer.char.length", "4");
        properties.setProperty("keptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise"); // 图片中的干扰，为了防止机器人暴力破解
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
