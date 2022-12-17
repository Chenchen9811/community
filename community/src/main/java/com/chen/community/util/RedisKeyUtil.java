package com.chen.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    private static final String PREFIX_FOLLOWEE = "followee";

    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";



    // 某个实体（用户、帖子）拥有的粉丝
    // 所以要用实体类型和实体id来唯一表示一个实体，所以key就是entityType加上entityId
    // value则是存userId，代表哪个用户关注了这个实体，并且以时间作为分数来排序
    // 对于当前这个entityType、entityId代表的实体来说，集合中存放的都是关注这个实体的人。
    // 对于集合中存放的实体来说，他们是当前用户的粉丝，所以这些实体是这个用户的follower（关注人）。
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户关注的实体, 以followee开头表示某个用户都关注了什么，key是用户和关注的实体类型，value则是关注的实体类型的id。
    // 以时间作为分数来排序，为了方便统计某人关注了什么，按照时间先后顺序列举出来。
    // 对于当前这个userId用户关注的entityType（实体）来说，集合中存放的是这个用户关注了多少这个实体。
    // 对于集合中存放的这些实体来说，它们是id为userId这个用户的关注目标，所以这些实体是这个用户的followee（被关注人）。
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个用户获得的赞
    // like:user:userId -> value是获取点赞的数量
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个实体获得的赞，value存放给该实体点赞的用户id
    // like:entity:entityType:entityId -> set(UserId)
    // 用集合来装点赞的数据，集合中存放的是点赞的用户Id，这样子方便统计谁对这个帖子点了赞，同时保证某个用户不能重复给某个实体点赞。
    // 统计某个实体收到的点赞数量，调用统计数量的方法size(Object k)
    // 查找谁给我点了赞，获取members(Object k)即可
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }


    // 验证码的key -> value(text) value存放验证码内容
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证ticket:String -> value(LoginTicket)
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户: user:userId -> value(User)
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间（从某天到某天之间）UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUkey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUkey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数，存放的是活跃的帖子， value存放的是帖子的id，使用的数据类型的Set，存放不重复的帖子。
    // 因为只要有对帖子的操作就会更新分数，那么肯定会有多个用户对同一个帖子进行操作，所以需要对帖子进行去重。
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

}
