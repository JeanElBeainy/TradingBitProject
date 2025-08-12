package com.market.tradingbit.config;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth //causing error: not displaying CSS anymore (did not happen previously)
                        .requestMatchers( "/commonFiles/**","/main/**", "images/**").permitAll()
                        .requestMatchers("/signup/**", "signin/**").permitAll()
                        .requestMatchers("/dashboard/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/dashboard/**").hasRole("USER")
                        .requestMatchers("/crypto/**").hasRole("USER")
                        .requestMatchers("/profile/**").hasRole("USER")
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/logout").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(config -> config.logoutSuccessUrl("/login"))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}