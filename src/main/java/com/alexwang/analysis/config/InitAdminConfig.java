package com.alexwang.analysis.config;

import com.alexwang.analysis.po.Tag;
import com.alexwang.analysis.po.Type;
import com.alexwang.analysis.po.User;
import com.alexwang.analysis.service.TagService;
import com.alexwang.analysis.service.TypeService;
import com.alexwang.analysis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class InitAdminConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private TypeService typeService;

    @Autowired
    private TagService tagService;

    @Bean
    public User initAdminUser() {

        //init types
        List<Type> types = typeService.listType();

        if(types.isEmpty()){
            Type newType = new Type();
            newType.setName("技术");
            newType.setId(Long.valueOf(1));
            typeService.saveType(newType);
        }

        //init tags
        List<Tag> tags = tagService.listTag();
        if(tags.isEmpty()){
            Tag newTag = new Tag();
            newTag.setId(Long.valueOf(1));
            newTag.setName("docker");
            tagService.saveTag(newTag);
        }

//        init Admin user
        User user = userService.checkuser("admin", "password");
        if(user == null){
            return userService.saveUser("admin","password");
        } else {
            return user;
        }



    }
}