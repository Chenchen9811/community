package com.chen.community.service;

import com.chen.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface ElasticsearchService {

    // 向ES中添加一个帖子
    void saveDiscussPost(DiscussPost discussPost);

    // 删除一个帖子
    void deleteDiscussPost(int id);


    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);


}
