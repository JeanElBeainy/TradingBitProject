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
                .authorizeHttpRequests(auth -> auth
                        //Access to static files (CSS & JS)
                        .requestMatchers( "/commonFiles/**",
                                "/dashboard/**",
                                "/disclaimer/**",
                                "/document/**",
                                "/error/**",
                                "images/**",
                                "/main/**",
                                "/profile/**",
                                "/signin/**",
                                "/signup/**",
                                "/swap/**",
                                "/faq/**"
                        ).permitAll()

                        //Access to guests
                        .requestMatchers("/",
                                "/register",
                                "/login",
                                "/docs",
                                "/dashboard",
                                "/faq",
                                "/disclaimer",
                                "/error"

                        ).permitAll()

                        //Access to users
                        .requestMatchers(
                                "/profile/**",
                                "/swap/**"

                                ).hasRole("USER")
                        .requestMatchers("/logout").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(config -> config
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}