package com.chen.community.service.impl;

import com.chen.community.service.DataService;
import com.chen.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    /**
     * 将指定ip计入UV
     * @param ip 要计入的ip
     */
    @Override
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    /**
     *  统计指定日期范围内的UV
     * @param start 开始日期
     * @param end 结束日期
     * @return
     */
    @Override
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 从开始日期遍历到结束日期，每个日期的key都不同。
        // 所以首先整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String uvKey = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE, 1);
        }

        // 存放合并数据后的key
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * 将指定用户放入DAU中
     * @param userId 用户id
     */
    @Override
    public void recordDAU(int userId) {
        String daUkey = RedisKeyUtil.getDAUkey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(daUkey, userId, true);
    }

    /**
     * 统计指定日期范围内的DAU
     * @param start 开始日期
     * @param end 结束日期
     */
    @Override
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 从开始日期遍历到结束日期，每个日期的key都不同。
        // 所以首先整理该日期范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String daUkey = RedisKeyUtil.getDAUkey(df.format(calendar.getTime()));
            keyList.add(daUkey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        // 进行OR运算然后返回
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                // 存放合并数据后的key
                String daUkey = RedisKeyUtil.getDAUkey(df.format(start), df.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        daUkey.getBytes(), keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(daUkey.getBytes());
            }
        });
    }
}
