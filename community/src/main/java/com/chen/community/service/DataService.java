package com.chen.community.service;

import java.util.Date;

public interface DataService {

    /**
     * 将指定ip计入UV
     * @param ip 要计入的ip
     */
    public void recordUV(String ip);

    /**
     *  统计指定日期范围内的UV
     * @param start 开始日期
     * @param end 结束日期
     * @return
     */
    public long calculateUV(Date start, Date end);

    /**
     * 将指定用户放入DAU中
     * @param userId 用户id
     */
    public void recordDAU(int userId);

    /**
     * 统计指定日期范围内的DAU
     * @param start 开始日期
     * @param end 结束日期
     */
    public long calculateDAU(Date start, Date end);

}
