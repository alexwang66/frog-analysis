package com.alexwang.analysis.service;

import com.alexwang.analysis.dao.UserRepository;
import com.alexwang.analysis.po.User;
import com.alexwang.analysis.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp implements UserService{

    @Autowired
    private UserRepository userRepository;
    private String DEFALT_AVATAR_URL = "/images/JFROG.png";

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
