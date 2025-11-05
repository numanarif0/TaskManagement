package com.yzmglstm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            // Adım 1: Postman gibi REST API istemcileri için CSRF'ı devre dışı bırak.
            .csrf(csrf -> csrf.disable()) 
            
            // Adım 2: Hangi yollara izin verileceğini belirle.
            .authorizeHttpRequests(authz -> authz
                // Kullanıcı kayıt endpoint'ine herkesin erişebilmesi için izin ver.
                .requestMatchers("/rest/api/users/save").permitAll() 
                
                // Senin test ettiğin 'get' endpoint'ine (şimdilik) izin ver.
                .requestMatchers("/rest/api/users/get").permitAll() 
                .requestMatchers("/rest/api/users/login").permitAll()
                
                // Geri kalan tüm istekler kimlik doğrulaması gerektirsin.
                .anyRequest().authenticated() 
            );
        
        return http.build();
    }
}