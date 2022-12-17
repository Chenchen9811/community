package com.chen.community.service;

import com.chen.community.entity.LoginTicket;
import com.chen.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService {

    String selectUserNameByUserId(int userId);

    User selectUserByUserId(int userId);

    /**
     * 注册
     * @param user
     * @return 注册的相关信息，为空时则表示注册成功，若不为空则表示注册过程中有问题，具体信息在map中显示。
     */
    Map<String, Object> register(User user);

    /**
     * 激活账号
     * @param userId
     * @param activationCode 返回激活状态
     * @return
     */
    public int activation(int userId, String activationCode);

    /**
     * 登录
     * @param username 账号名
     * @param password 用户未加密的原始密码
     * @param expiredSeconds 过期时间
     * @return
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds);

    /**
     * 依据登录凭证退出登录。
     * @param ticket
     */
    public void logout(String ticket);

    /**
     * 通过登录凭证ticket得到LoginTicket
     * @param ticket
     * @return
     */
    public LoginTicket selectLoginTicketByTicket(String ticket);

    /**
     * 更新用户头像的路径
     * @param userId 用户id
     * @param headerUrl 用户头像的路径（即用户头像从哪里取）
     * @return
     */
    public int updateHeaderUrl(int userId, String headerUrl);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return
     */
    User selectUserByUsername(String username);

    /**
     * 获取用户权限
     * @param userId 用户id
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId);

}
