package com.alexwang.analysis.service;

import com.alexwang.analysis.po.User;

public interface UserService {

    User checkuser(String username, String password);

    User saveUser(String username, String password);
}
