package com.chen.community.controller;

import com.chen.community.entity.User;
import com.chen.community.service.UserService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.CommunityUtil;
import com.chen.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> resultMap = userService.register(user);
        // 根据返回的map进一步的给前端响应
        // resultMap为空代表注册成功
        if (resultMap == null || resultMap.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已向您邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index"); // 注册成功后跳转到首页
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", resultMap.get("usernameMsg"));
            model.addAttribute("passwordMsg", resultMap.get("passwordMsg"));
            model.addAttribute("emailMsg", resultMap.get("emailMsg"));
            return "/site/register";
        }
    }

    // 访问登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String toLoginPage() {
        return "/site/login";
    }

    // http://localhost:8080/community/activation/101/code    激活地址
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activationResult = userService.activation(userId, code);
        if (activationResult == ACTIVATION_SUCCESS) { // 激活成功后跳转到登录页面
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用！");
            model.addAttribute("target", "/login"); // 跳转到登录页
        } else if (activationResult == ACTIVATION_REPEAT) { // 重复激活那么跳转到首页
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index"); // 跳转到首页
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index"); // 跳转到首页
        }
        return "/site/operate-result";
    }

    // 生成验证码，每个请求都要有一个验证码。
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        String text = kaptchaProducer.createText(); // 生成验证码内容
        BufferedImage image = kaptchaProducer.createImage(text); // 通过验证码内容创建图片
//        // 将验证码存入session
//        session.setAttribute("kaptcha", text);
        // 验证码的归属，临时给打开登录界面的用户随机生成一个字符串
        String kaptchaOwner = CommunityUtil.generateUUID();
        // 这个凭证要通过cookie发给客户端，客户端用cookie保存
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        // 设置cookie的生存时间
        cookie.setMaxAge(60);
        // 设置cookie生效路径
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // redis生成key
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        // 将验证码存入redis，value存的是验证码内容，过期时间为60s
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);
        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    /**
     *
     * @param model
     * @param username 账号
     * @param password 密码
     * @param code 传进来的验证码
     * @param rememberMe 是否选了✔记住我
     * @param session 用于比较验证码是否准确
     * @param cookie 通过cookie返回登录凭证
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code,
                        boolean rememberMe, HttpSession session, HttpServletResponse response, @CookieValue("kaptchaOwner")String kaptchaOwner) {
        // 从cookie中的随机字符串拼接redisKey来获取存在Redis中的验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }
        // 将redis中的验证码和用户输入的验证码相比较，如果不正确则重新跳回登录页面
        if (StringUtils.isBlank(kaptcha) ||StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        // 检查账号，密码
        int expiredSeconds = rememberMe? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> resultMap = userService.login(username, password, expiredSeconds);
        if (resultMap.containsKey("ticket")) {  // 登录成功
            Cookie cookie = new Cookie("ticket", (String) resultMap.get("ticket"));
            cookie.setPath(contextPath); // 设置cookie的生效路径
            cookie.setMaxAge(expiredSeconds); // 设置cookie生效时间
            response.addCookie(cookie);
            return "redirect:/index";
        } else { // 登录失败
            model.addAttribute("usernameMsg", resultMap.get("usernameMsg"));
            model.addAttribute("passwordMsg", resultMap.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 退出登录
     * @param cookie 通过cookie中的ticket来确认登录凭证。然后通过凭证退出。
     * @return
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(Model model, @CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login"; // 重定向时默认是get请求
    }
}
