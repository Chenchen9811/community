package com.chen.community.util;

import com.chen.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 起到容器的作用，用于代替session对象，持有对象信息。
 */
@Component
public class HostHolder {
    private static ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
