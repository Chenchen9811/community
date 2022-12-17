package com.chen.community.controller;

import com.chen.community.entity.DiscussPost;
import com.chen.community.entity.Page;
import com.chen.community.entity.User;
import com.chen.community.service.ElasticsearchService;
import com.chen.community.service.LikeService;
import com.chen.community.service.UserService;
import com.chen.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    //   /search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
        elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 前端需要将搜索出来的帖子的作者以及点赞数量都显示出来，因此这里要进一步做处理
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost discussPost : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 搜索出来的帖子
                map.put("post", discussPost);
                // 帖子作者
                map.put("user", userService.selectUserByUserId(discussPost.getUserId()));
                // 帖子的点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null? 0 : (int)searchResult.getTotalElements());
        return "/site/search";
    }


}
