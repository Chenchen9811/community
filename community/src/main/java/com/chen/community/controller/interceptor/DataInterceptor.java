package com.chen.community.controller.interceptor;

import com.chen.community.entity.User;
import com.chen.community.service.DataService;
import com.chen.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 根据ip统计UV
        dataService.recordUV(request.getRemoteAddr());
        // 统计DAU
        User user;
        if ((user = hostHolder.getUser()) != null) {
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
