package com.chen.community.quartz;

import com.chen.community.controller.DiscussPostController;
import com.chen.community.entity.DiscussPost;
import com.chen.community.service.CommentService;
import com.chen.community.service.DiscussPostService;
import com.chen.community.service.ElasticsearchService;
import com.chen.community.service.LikeService;
import com.chen.community.util.CommunityConstant;
import com.chen.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscussPostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LikeService likeService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;


    // 纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元失败！", e);
        }
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 1、从redis中取出分数发生变化的帖子id
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);
        // 2、判断是否有帖子分数发生了变化
        if (operations.size() == 0) {
            logger.info("任务取消，没有需要刷新分数的帖子！");
            return;
        }
        logger.info("任务开始，正在刷新分数发生变化帖子的分数！");
        // 3、刷新帖子分数
        while (operations.size() > 0) {
            this.refresh((Integer)operations.pop());
        }
        logger.info("帖子分数刷新完毕！");
    }

    private void refresh(int discusspostId) {
        // 帖子分数公式 = log(精华分 + 评论数 * 10 + 点赞数 * 2 + 收藏数 * 2） + （发布时间 - 纪元）
        // 1、获取帖子
        DiscussPost discussPost = discussPostService.selectDiscussPostById(discusspostId);
        if (discussPost == null) {
            logger.error("该帖子不存在：" + discusspostId);
        }
        // 2、获取精华分（是否加精）
        boolean isWonderful = discussPost.getStatus() == 1; // 1 -> 加精
        // 3、评论数量
        int commentCount = discussPost.getCommentCount();
        // 4、点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discusspostId);
        // 5、计算权重
        double w = (isWonderful? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 6、总分数 = 权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (discussPost.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 7、更新帖子分数
        discussPostService.updateDiscussPostScore(discusspostId, score);
        // 同步elasticsearch数据
        discussPost.setScore(score);
        elasticsearchService.saveDiscussPost(discussPost);
    }
}
