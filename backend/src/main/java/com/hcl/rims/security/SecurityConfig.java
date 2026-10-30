package com.hcl.rims.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Content-Disposition", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/h2-console/**", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/health").permitAll()
                // ADMIN manages users
                .requestMatchers("/api/users/**", "/api/admin/**").hasRole("ADMIN")
                // ERP imports (MANAGER+)
                .requestMatchers(HttpMethod.POST, "/api/erp/**").hasAnyRole("ADMIN", "MANAGER")
                // Catalog management & suppliers (MANAGER+)
                .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**", "/api/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**", "/api/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**", "/api/suppliers/**").hasAnyRole("ADMIN", "MANAGER")
                // Inventory adjustment and transfers (STAFF, MANAGER, ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/inventory/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                // Orders (STAFF, MANAGER, ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                // DELETE endpoints
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("ADMIN", "MANAGER")
                // VIEWER read-only (all authenticated roles can read)
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "MANAGER", "STAFF", "VIEWER")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
