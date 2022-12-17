package com.chen.community.service.impl;

import com.chen.community.dao.LoginTicketMapper;
import com.chen.community.dao.UserMapper;
import com.chen.community.entity.LoginTicket;
import com.chen.community.entity.User;
import com.chen.community.service.UserService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import com.chen.community.util.MailClient;
import com.chen.community.util.RedisKeyUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public String selectUserNameByUserId(int userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            return user.getUsername();
        }
        return "";
    }

    @Override
    public User selectUserByUserId(int userId) {
        User user = getCache(userId);
        if (user == null) {
            user = initCache(userId);
        }
        return user;
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (!ObjectUtils.isEmpty(u)) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (!ObjectUtils.isEmpty(u)) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5)); // 生成5位的salt
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt())); // 原密码+salt一起加密
        user.setType(0); // 默认是0-普通用户
        user.setStatus(0); // 0-未激活
        user.setActivationCode(CommunityUtil.generateUUID()); // 设置激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000))); // 设置随机头像，用户登陆后可以自行修改
        user.setCreateTime(new Date()); // 设置创建时间为当前时间
        userMapper.insertUser(user); // 创建用户后插入到数据库

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code    激活地址
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        //System.out.println(content);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }


    @Override
    public int activation(int userId, String activationCode) {
        // 业务逻辑：首先通过userId从数据库中查找该用户，然后获取该用户的登录状态。根据登录状态做出不同的响应。
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) { // 用户状态为1 - 已激活，即如果重复激活，则返回提示信息。
            return ACTIVATION_REPEAT;
        }
        // 前端传的激活码activationCode与数据库中该用户的激活码匹配成功，那么激活。
        else if (user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1); // 修改用户的激活状态
            clearCache(userId); // 对用户状态进行了修改，那么就清理缓存。
            return ACTIVATION_SUCCESS;
        } else return ACTIVATION_FAILURE;
    }

    @Override
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(username)) {
            map.put("userMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isEmpty(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        if (expiredSeconds <= 0) {
            map.put("expiredSecondsMsg", "过期时间秒数必须大于0");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) { // 如果密码不匹配
            map.put("usernameMsg", "密码不正确！");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    @Override
    public LoginTicket selectLoginTicketByTicket(String ticket) {
//       return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    @Override
    public int updateHeaderUrl(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl); // 更新完后清缓存
        clearCache(userId); // 清缓存
        return rows;
    }

    @Override
    public User selectUserByUsername(String username) {
        return userMapper.selectByName(username);
    }

    //1、优先从缓存中取值，取到直接返回
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //2、取不到则初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3、数据变更时，清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     * 获取用户权限
     * @param userId 用户id
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.selectUserByUserId(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1 : {
                        return AUTHORITY_ADMIN;
                    }
                    case 2 : {
                        return AUTHORITY_MODERATOR;
                    }
                    default: {
                        return AUTHORITY_USER;
                    }
                }
            }
        });
        return list;
    }
}
