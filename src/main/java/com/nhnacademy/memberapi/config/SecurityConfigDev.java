package com.nhnacademy.memberapi.config;

import com.nhnacademy.memberapi.jwt.JWTFilter;
import com.nhnacademy.memberapi.jwt.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableWebSecurity
@Profile("dev")
public class SecurityConfigDev {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfigDev(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
    }

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

        // 프레임 옵션 설정: 모든 URL의 프레임 보호 해제(X-Frame-Options를 비활성화)
        // h2 DB를 위해 dev 환경에서만 X-Frame-Options 임시 비활성화
        http.headers((headers) -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        http.authorizeHttpRequests((auth)->auth
                .requestMatchers("/member/**", "/auth/**", "/error", "/h2-console/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());

        // JWTFilter (다른 API 접근 시 토큰 검증용)
        http.addFilterBefore(new JWTFilter(jwtUtil), AuthorizationFilter.class);

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}