package com.chen.community;

import com.chen.community.util.SensitiveWordsFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitivesTests {
    @Autowired
    private SensitiveWordsFilter sensitiveWordsFilter;

    @Test
    public void testSensitiveFilter() {
        String origin = "这里可以赌博，可以吸毒，有人是傻逼";
        String filteredWords = sensitiveWordsFilter.filteredWords(origin);
        System.out.println(filteredWords);
    }
}
