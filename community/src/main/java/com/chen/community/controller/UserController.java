package com.chen.community.controller;

import com.chen.community.annotation.LoginRequired;
import com.chen.community.entity.User;
import com.chen.community.service.FollowService;
import com.chen.community.service.LikeService;
import com.chen.community.service.UserService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import com.chen.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    // 更新的是当前用户的头像，所以要HostHolder
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage.isEmpty()) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        } else {
            // 取原始文件名
            String originalFilename = headerImage.getOriginalFilename();
            // 从最后一个点的后面开始截取，即取后缀名
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            if (StringUtils.isBlank(suffix)) {
                model.addAttribute("error", "文件格式不正确！");
                return "/site/setting";
            }
            // 生成随机文件名
            String fileName = CommunityUtil.generateUUID() + suffix;
            // 确定文件存放的路径
            File dest = new File(uploadPath + "/" + fileName);
            try {
                // 将图片写入文件
                headerImage.transferTo(dest);
            } catch (IOException e) {
                logger.error("上传文件失败:" + e.getMessage());
                throw new RuntimeException("上传文件失败，服务器发生异常");
            }
            // 更新当前用户的头像路径（web访问路径）
            // http://localhost:8080/community/user/header/xxx.png
            User user = hostHolder.getUser();
            String headerUrl = domain + contextPath + "/user/header/" + fileName;
            userService.updateHeaderUrl(user.getId(), headerUrl);
            return "redirect:/index";
        }
    }

    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器存放头像的路径
        filename = uploadPath + "/" + filename;
        // 文件的后缀
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        // 响应图片给浏览器
        response.setContentType("image/" + suffix);
        try (
                OutputStream outputStream = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(filename);
                )
        {

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    // 个人主页（可以查看任何人的主页，因此传进来的是目标人的userId，这个目标可以是自己可以是别人）
    // 这里要显示这个用户收到的点赞数量
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.selectUserByUserId(userId);
        // 防止传入的userId是无效的。
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注，这里可以不用登录，但如果要点击关注的话必须要先登录
        boolean hasFollowed = false;
        // 获取当前登录用户
        User loginUser = hostHolder.getUser();
        if (loginUser != null) {
            // 如果用户已登录，那么可以这个登录用户可以关注别的用户
            hasFollowed = followService.hasFollowed(loginUser.getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
