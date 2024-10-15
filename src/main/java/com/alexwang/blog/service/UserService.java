package com.alexwang.blog.service;

import com.alexwang.blog.po.User;

public interface UserService {

    User checkuser(String username, String password);

    User saveUser(String username, String password);
}
