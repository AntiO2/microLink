package org.microserviceteam.workflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

@Configuration
public class ActivitiSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        // 创建一个内存用户，赋予 ROLE_ACTIVITI_USER 权限
        // 这样 Activiti 在启动时就能找到这个 Bean
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(new User("admin", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"))));
        return manager;
    }
}