package com.chen.community.quartz;

import com.chen.community.service.DiscussPostService;
import com.chen.community.service.EntityLikeService;
import com.chen.community.util.CommunityConstant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisToMysqlJob implements Job, CommunityConstant {

    @Autowired
    private EntityLikeService entityLikeService;

    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 获取Redis用户收到的关注数据
        // 1.

    }
}
