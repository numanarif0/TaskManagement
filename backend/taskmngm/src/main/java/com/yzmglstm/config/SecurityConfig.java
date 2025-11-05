package com.yzmglstm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/rest/api/auth/save").permitAll() 
                
                .requestMatchers("/rest/api/auth/get").permitAll() 
                .requestMatchers("/rest/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/rest/api/tasks").permitAll()
                
                .anyRequest().authenticated() 
            );
        
        return http.build();
    }
}