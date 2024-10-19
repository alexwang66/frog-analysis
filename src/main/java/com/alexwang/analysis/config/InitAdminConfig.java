package com.alexwang.analysis.config;

import com.alexwang.analysis.po.Tag;
import com.alexwang.analysis.po.Type;
import com.alexwang.analysis.po.User;
import com.alexwang.analysis.service.TagService;
import com.alexwang.analysis.service.TypeService;
import com.alexwang.analysis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    // Inject admin user and password from application.yml or environment variables
    @Value("${app.default.user}")
    private String defaultAdminUsername;

    @Value("${app.default.password}")
    private String defaultAdminPassword;

    @Bean
    public User initAdminUser() {

        // Initialize types if empty
        List<Type> types = typeService.listType();
        if (types.isEmpty()) {
            Type newType = new Type();
            newType.setName("技术");
            newType.setId(Long.valueOf(1));
            typeService.saveType(newType);
        }

        // Initialize tags if empty
        List<Tag> tags = tagService.listTag();
        if (tags.isEmpty()) {
            Tag newTag = new Tag();
            newTag.setId(Long.valueOf(1));
            newTag.setName("docker");
            tagService.saveTag(newTag);
        }

        // Initialize Admin user using injected username and password from application.yml
        User user = userService.checkuser(defaultAdminUsername, defaultAdminPassword);
        if (user == null) {
            return userService.saveUser(defaultAdminUsername, defaultAdminPassword);
        } else {
            return user;
        }
    }
}
