package com.chen.community;

import com.chen.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TestSendMail {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void testSendMail() {
        mailClient.sendMail("583821570@qq.com", "hello!", "this is a mail from 583821570@qq.com");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "583821570@qq.com");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("583821570@qq.com", "html", "this is a html mail from 583821570@qq.com");
    }
}
