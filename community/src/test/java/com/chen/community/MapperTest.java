package com.chen.community;

import com.chen.community.dao.*;
import com.chen.community.entity.*;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest implements CommunityConstant{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private EntityLikeMapper entityLikeMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        User liubei = userMapper.selectByName("liubei");
        System.out.println(liubei);

        User byEmail = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(byEmail);
    }


    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setUsername("awajiwo2");
        user.setPassword("nawaji2");
        user.setSalt("jiwoguo");
        user.setEmail("awajiwo@jiwoguo.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdate() {
        int updateHeader = userMapper.updateHeader(150, "http://www.nocoder.com/150.png");
        int updatePassword = userMapper.updatePassword(150, "nawajiwo");
        int updateStatus = userMapper.updateStatus(150, 1);
        User user = userMapper.selectById(150);
        System.out.println(user);
    }

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10, 0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
        int rows = discussPostMapper.selectAllDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void test() {
        int a = 123;
        int b = 123;
        System.out.println(a == b); // true
        User user1 = new User();
        User user2 = new User();
        System.out.println(user1 == user2); // false

        String s1 = "123";
        String s2 = "123";
        String s3 = new String("123");
        System.out.println(s1 == s2); // true
        System.out.println(s1 == s3); // false

        Integer i1 = 123;
        Integer i2 = 123;
        System.out.println(i1 == i2); // true
        Integer i3 = 132;
        Integer i4 = 132;
        System.out.println(i3 == i4); // false
    }

    @Test
    public void testTicket() {
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(150);
//        loginTicket.setTicket(CommunityUtil.generateUUID());
//        loginTicket.setStatus(1);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10)); // 十分钟后过期
//        int i = loginTicketMapper.insertLoginTicket(loginTicket);
//        System.out.println(i);
//
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket("23062ce532774da79d10797139fe54b8");
//        System.out.println(loginTicket);

        int updateStatus = loginTicketMapper.updateStatus("23062ce532774da79d10797139fe54b8", 0);
        System.out.println(updateStatus);
    }

    @Test
    public void testMessages() {
//        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
//        for (Message message : messages) {
//            System.out.println(message);
//        }
        int i = messageMapper.selectConversationsCount(111);
        System.out.println(i);
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
        int i1 = messageMapper.selectLettersUnreadCount(111, "111_112");
        System.out.println(i1);
        int i2 = messageMapper.selectLettersCount("111_112");
        System.out.println(i2);
    }

    @Test
    public void testEntityLikeMapper() {
        EntityLike entityLike = new EntityLike();
        entityLike.setEntityType(ENTITY_TYPE_USER);
        entityLike.setEntityId(111);
        entityLike.setUserId(159);
        entityLike.setCreateTime(new Date());
        int i = entityLikeMapper.insertEntityLike(entityLike);
        System.out.println(i);
    }
}
