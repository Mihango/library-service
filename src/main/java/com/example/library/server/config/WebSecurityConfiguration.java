package com.example.library.server.config;


import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebFluxSecurity // auto configures SecurityWebFilterChain
public class WebSecurityConfiguration {


    @Bean
    public PasswordEncoder passwordEncoder() {
        // adds new delegating password encoder in spring security 5
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
