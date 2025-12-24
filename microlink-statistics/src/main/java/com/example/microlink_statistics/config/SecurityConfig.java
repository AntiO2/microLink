package com.example.microlink_statistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security 配置类
 * 在 Spring Boot 3+ 中，需要明确配置哪些端点是公开的。
 *
 * @author Rolland1944
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 授权请求配置
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // 允许所有对 /api/v1/statistics/** 路径的请求，无需认证
                    .requestMatchers("/api/statistics/**").permitAll()
                    // 允许对 actuator 健康检查端点的请求
                    .requestMatchers("/actuator/**").permitAll()
                    // 其他所有请求都需要认证
                    .anyRequest().authenticated()
            )
            // 启用 HTTP Basic 认证，用于其他需要保护的端点（如果未来有的话）
            .httpBasic(withDefaults())
            // 禁用 CSRF (跨站请求伪造) 保护，因为我们的 API 是无状态的，主要由服务调用
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
