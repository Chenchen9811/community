package com.chen.community.controller;

import com.chen.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    // 打开统计网站
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDatePage() {
        return "/site/admin/data";
    }

    // 统计网站UV
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern="yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern="yyyy-MM-dd") Date end, Model model) {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        // 下面这两个是返回统计页面时方便显示是从什么时候开始到什么时候结束的统计数据
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        return "forward:/data"; // getUV处理到一半（查询了数据），然后转发到/data继续做处理（显示数据），仍然是同一个请求。这里的请求方法是POST，那么转发到的那个处理方法也要支持POST。所以/data这个方法也要支持POST请求。
    }

    // 统计网站DAU
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern="yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern="yyyy-MM-dd") Date end, Model model) {
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        // 下面这两个是返回统计页面时方便显示是从什么时候开始到什么时候结束的统计数据
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/data"; // getDAU处理到一半（查询了数据），然后转发到/data继续做处理（显示数据），仍然是同一个请求。这里的请求方法是POST，那么转发到的那个处理方法也要支持POST。所以/data这个方法也要支持POST请求。
    }


}
