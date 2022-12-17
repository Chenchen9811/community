package com.chen.community.dao;

import com.chen.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@Deprecated// 声明该组件为不推荐使用
public interface LoginTicketMapper {

    /**
     * 插入一条登录凭证
     * @param loginTicket
     * @return
     */
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 通过ticket查询凭证
     * @param ticket
     * @return
     */
    LoginTicket selectByTicket(String ticket);

    /**
     * 修改登录状态
     * @param ticket
     * @return
     */
    int updateStatus(String ticket, int status);
}
