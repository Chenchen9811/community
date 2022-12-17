package com.chen.community.controller;

import com.chen.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


@Controller
public class HelloController {

    @RequestMapping("/apache/hello")
    @ResponseBody
    public String hello() {
        return "hello Spring Boot!";
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String key = request.getHeader(name);
            System.out.println(name + ": " + key);
        }
        System.out.println(request.getParameter("code"));

        // 返回响应数据
        response.setContentType("text/html:charset=UTF-8"); // 设置返回响应数据的类型

        try (PrintWriter writer = response.getWriter()) {
            writer.write("<h1> 牛客网 </h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //   /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "current", required = false, defaultValue = "10") int limit) {
        return "some students";
    }

    // /student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents2(@PathVariable("id") Integer id) {
        System.out.println(id);
        return "a student";
    }

    // /teacher
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "张三");
        modelAndView.addObject("age", "30");
        modelAndView.setViewName("/demo/view"); // 这里的/demo/view其实是/demo/view.html
        return modelAndView;
    }

    // /School
    @RequestMapping(value = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name", "河海大学");
        model.addAttribute("age", "107");
        return "/demo/view";
    }

    // 响应JSON数据（异步请求中用的比较频繁，即当前网页不刷新但是悄悄的访问了服务器，比如注册网站的会员时，输入注册名称但是显示名称重复，此时网页没有刷新，但是却偷偷的访问了服务器）
    // Java对象 -> JSON字符串 -> JS对象（浏览器使用）
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 1111);
        map.put("gender", "male");
        map.put("salary", 8000.0);
        return map;
    }

    // 查询所有员工
    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps() {
        ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
        HashMap<String, Object> map;
        map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 1111);
        map.put("gender", "male");
        map.put("salary", 8000.0);
        arrayList.add(map);

        map = new HashMap<>();
        map.put("name", "张5");
        map.put("age", 1112);
        map.put("gender", "male");
        map.put("salary", 9000.0);
        arrayList.add(map);

        map = new HashMap<>();
        map.put("name", "张6");
        map.put("age", 1115);
        map.put("gender", "male");
        map.put("salary", 5000.0);
        arrayList.add(map);
        return arrayList;
    }

    // cookie示例
    // 服务器收到浏览器的请求后，在响应报文中添加cookie传回给服务器。这里就相当于服务器了，因为是后端。
    @RequestMapping(value = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response) {
        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置cookie生效范围：指定浏览器访问哪个路径时才带上cookie，而不是让浏览器访问所有路径都带上cookie，避免资源浪费。
        cookie.setPath("/community"); // 设置cookie在哪个路径有效
        // cookie的生存时间：cookie在浏览器中默认关闭就消失了。设置了生存时间后可以长期存在硬盘里。
        cookie.setMaxAge(60 * 10); // 设置cookie生存时间为60s * 10 = 600s
        // 发送cookie
        response.addCookie(cookie);
        return "set cookie";
    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }

    // session示例
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    // AJAX示例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("age", age);
        return CommunityUtil.getJSONString(0, "ok", map);
    }
}
