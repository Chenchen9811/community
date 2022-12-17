package com.chen.community.controller;

import com.chen.community.annotation.LoginRequired;
import com.chen.community.entity.Event;
import com.chen.community.entity.Page;
import com.chen.community.entity.User;
import com.chen.community.event.EventProducer;
import com.chen.community.service.FollowService;
import com.chen.community.service.UserService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import com.chen.community.util.HostHolder;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(FollowController.class);
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        // 获取当前登录用户
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 因为目前只能关注人，所以EntityUserId就是entityId
        // 用户收到关注通知后，可以点击查看是哪个用户点的赞然后跳转到该用户的个人主页，因此这里要传进来点赞人的id，方便拼接个人主页的URL
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "已关注！");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unFollow(int entityType, int entityId) {
        // 获取当前登录用户
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    // 查找关注当前用户（当前点击的个人主页中的用户）的人
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(Model model, @PathVariable("userId") int userId, Page page) {
        User user = userService.selectUserByUserId(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 页面上要显示该user（个人主页代表的用户）关注的人和关注该user的人，因此这个user的名字要放到页面上显示
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + user.getId()); // 点击跳转路径
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        logger.info("userList:{}", userList);
        // 还要判断当前用户能否关注关注了个人主页代表的用户的用户
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User followeeUser = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(followeeUser.getId()));
                //userList.add(map);
            }
        }
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    // 查询某个用户（当前点击的个人主页中的用户）的粉丝
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(Model model, @PathVariable("userId") int userId, Page page) {
        User user = userService.selectUserByUserId(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 页面上要显示该user（个人主页代表的用户）关注的人和关注该user的人，因此这个user的名字要放到页面上显示
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + user.getId()); // 点击跳转路径
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        // 还要判断当前用户能否关注关注了个人主页代表的用户的用户
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User followerUser = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(followerUser.getId()));
                //userList.add(map);
            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";
    }


    private boolean hasFollowed(int userId) {
        // 判断当前用户是否登录，没登陆就返回false
        User user = hostHolder.getUser();
        if (user == null) return false;
        // 登陆了就判断当前用户是否关注了id为userId的这个用户
        return followService.hasFollowed(user.getId(), ENTITY_TYPE_USER, userId);
    }

}
