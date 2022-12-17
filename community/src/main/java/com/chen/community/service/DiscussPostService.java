package com.chen.community.service;


import com.chen.community.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {

    /**
     * 查询帖子。
     * @param userId 如果为0代表是首页，显示的是所有帖子。如果userId有值，代表的是用户个人主页，那么显示的就是用户发布的帖子。所以这里要用到动态SQL。
     * @param offset 起始行的行号，如果要查第1行开始的那么offset = 1,第0行开始，那么offset = 0。
     * @param limit 每页最多显示多少行数据
     * @param orderMode 排序方式. 0:时间， 1:热度
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode);

    int findDiscussPostRows(int userId);

    /**
     * 插入一个帖子
     * @param discussPost 待插入的帖子
     * @return
     */
    int addDiscussPost(DiscussPost discussPost);

    /**
     * 根据id查询帖子
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子的评论数量
     * @param id 帖子的id
     * @param commentCount 更新的数量
     * @return
     */
    int updateDiscussPostCommentCount(int id, int commentCount);

    /**
     * 修改帖子类型
     * @param id 帖子id
     * @param type 要修改的类型
     * @return
     */
    int updateDiscussPostType(int id, int type);

    /**
     *
     * @param id 帖子id
     * @param status 要修改的状态
     * @return
     */
    int updateDiscussPostStatus(int id, int status);

    /**
     * 修改帖子分数
     * @param id 帖子id
     * @param score 要修改的分数
     * @return
     */
    int updateDiscussPostScore(int id, double score);

    /**
     * 通过帖子id查询帖子所属人的id
     * @param discussPostId
     * @return 帖子所属人id
     */
    int findUserIdByDiscussPostId(int discussPostId);
}
