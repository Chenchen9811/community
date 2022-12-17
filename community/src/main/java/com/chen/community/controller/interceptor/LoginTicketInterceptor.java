package com.chen.community.controller.interceptor;
import com.chen.community.entity.LoginTicket;
import com.chen.community.entity.User;
import com.chen.community.service.UserService;
import com.chen.community.util.CookieUtil;
import com.chen.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 通过cookie得到登录凭证ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        // 通过ticket查询LoginTicket
        if (!StringUtils.isBlank(ticket)) {
            LoginTicket loginTicket = userService.selectLoginTicketByTicket(ticket);
            // 检查凭证是否有效（LoginTicket != null，状态有效并且未过期）
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 凭证有效，那么就查询用户
                User user = userService.selectUserByUserId(loginTicket.getUserId());
                // 在本次请求中持有用户(服务器会为每次请求都创建一个线程来处理，因此这要用多线程来存这个user，并且要保证线程隔离）
                // 这里要用ThreadLocal，这里把User存到了当前线程中。
                hostHolder.setUser(user);
                // 构建用户认证结果并存入SecurityContext，以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getUsername(), userService.getAuthorities(user.getId())
                );
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    // postHandle在模板引擎之前执行，这里要把User存到模板引擎中。
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    // 请求结束的时候要将User清掉。
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
