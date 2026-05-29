package com.novelreading.config;

import com.novelreading.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

/**
 * Spring Security 配置
 * 配置 JWT 认证、公开接口与受保护接口
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 公开接口：无需 JWT 即可访问 */
    private static final String[] PUBLIC_PATHS = {
            "/auth/login", "/auth/register",
            "/admin/auth/login",
            "/api/auth/login", "/api/auth/register",
            "/api/admin/auth/login",
            "/publishers", "/tags", "/authors",
            "/novels/**", "/novels", "/chapters/**", "/speech/**"
    };
    /** 需登录的接口（/bookshelf、/user、/reading-progress 及其子路径） */
    private static final String[] AUTH_PATHS = { "/bookshelf", "/bookshelf/**", "/user", "/user/**", "/reading-progress", "/reading-progress/**" };
    /** 管理员接口：需管理员 JWT */
    private static final String[] ADMIN_PATHS = { "/admin", "/admin/**" };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 启用 CORS（使用 WebMvcConfigurer 提供的 CORS 配置），否则可能出现 Invalid CORS request
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/novels/*/favorite").authenticated()
                        .requestMatchers(HttpMethod.POST, "/novels/*/comments").authenticated()
                        .requestMatchers(AUTH_PATHS).authenticated()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(ADMIN_PATHS).authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
