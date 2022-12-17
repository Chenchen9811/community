package com.chen.community;

import com.chen.community.entity.DiscussPost;
import com.chen.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 1; i <= 300000; i ++) {
            DiscussPost discussPost = new DiscussPost();
            discussPost.setId(111);
            discussPost.setTitle("互联网求职暖春秋计划！！");
            discussPost.setContent("awajiwo！！！！！来" + i + "个OFFER！！！");
            discussPost.setCreateTime(new Date());
            discussPost.setScore(Math.random() * 20000);
            discussPostService.addDiscussPost(discussPost);
        }
    }

    @Test
    public void testCache() {
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 0));
    }

}
