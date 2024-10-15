package com.alexwang.blog.service;

import com.alexwang.blog.dao.UserRepository;
import com.alexwang.blog.po.User;
import com.alexwang.blog.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp implements UserService{

    @Autowired
    private UserRepository userRepository;
    private String DEFALT_AVATAR_URL = "/images/wechat.jpg";

    @Override
    public User checkuser(String username, String password) {
        User user = userRepository.findByUsernameAndPassword(username, MD5Utils.code(password));
        return user;
    }

    @Override
    public User saveUser(String username, String password) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(MD5Utils.code(password));
        newUser.setAvatar(DEFALT_AVATAR_URL);
        User user = userRepository.save(newUser);
        return user;
    }

}
