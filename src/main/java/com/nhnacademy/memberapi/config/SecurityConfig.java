package com.nhnacademy.memberapi.config;

import com.nhnacademy.memberapi.jwt.JWTFilter;
import com.nhnacademy.memberapi.jwt.JWTUtil;
import com.nhnacademy.memberapi.jwt.SocialLoginHandler;
import com.nhnacademy.memberapi.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
@Profile("op")
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final SocialLoginHandler socialLoginHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf((auth)-> auth.disable());
        http.formLogin((auth)-> auth.disable());
        http.httpBasic((auth) -> auth.disable());

        // OAuth2 설정
        http.oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((userInfo) -> userInfo
                        .userService(customOAuth2UserService))
                .successHandler(socialLoginHandler)
        );

        http.authorizeHttpRequests((auth)->auth
                .requestMatchers("/login", "/login/**", "/", "/signup", "/error","/reissue","/logout").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        // JWTFilter (다른 API 접근 시 토큰 검증용)
        http.addFilterBefore(new JWTFilter(jwtUtil), AuthorizationFilter.class);

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}