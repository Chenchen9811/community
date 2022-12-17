package com.chen.community.dao;

import com.chen.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    /**
     * 通过id查询用户
     * @param id 用户id
     * @return
     */
    User selectById(int id);

    /**
     * 通过name查询用户
     * @param username 用户名字
     * @return
     */
    User selectByName(String username);

    /**
     * 通过email查询用户
     * @param email 用户邮箱
     * @return
     */
    User selectByEmail(String email);

    /**
     * 增加一个用户
     * @param user
     * @return 插入数据的行数
     */
    int insertUser(User user);

    /**
     * 修改用户的状态
     * @param id 用户id
     * @param status
     * @return 修改的条数
     */
    int updateStatus(int id, int status);

    /**
     *
     * @param id 用户id
     * @param headerUrl 用户头像路径
     * @return 修改的条数
     */
    int updateHeader(int id, String headerUrl);

    /**
     * 修改用户密码
     * @param id 用户id
     * @param password 最新的密码
     * @return 修改的条数
     */
    int updatePassword(int id, String password);
}
