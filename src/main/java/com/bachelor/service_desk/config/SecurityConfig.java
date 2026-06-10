package com.bachelor.service_desk.config;

import com.bachelor.service_desk.security.CustomAuthenticationProvider;
import com.bachelor.service_desk.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final List<String> DEFAULT_FRONTEND_ORIGINS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5174",
            "http://127.0.0.1:5174",
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "https://service-desk-frontend-iota.vercel.app"
    );

    @Value("${FRONTEND_URL:https://service-desk-frontend-iota.vercel.app}")
    private String frontendUrl;

    @Value("${app.cors.allowed-origins:${CORS_ALLOWED_ORIGINS:}}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-origin-patterns:${CORS_ALLOWED_ORIGIN_PATTERNS:https://*.vercel.app}}")
    private String allowedOriginPatterns;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtFilter jwtFilter,
            CustomAuthenticationProvider customAuthenticationProvider
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            String jwtError = (String) request.getAttribute(JwtFilter.JWT_ERROR_ATTR);
                            String message = JwtFilter.JWT_ERROR_EXPIRED.equals(jwtError)
                                    ? "Access token expired"
                                    : "Authentication required";
                            response.getWriter().write("{\"message\":\"" + message + "\"}");
                        })
                )
                .authenticationProvider(customAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedCorsOrigins());
        configuration.setAllowedOriginPatterns(allowedCorsOriginPatterns());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> allowedCorsOrigins() {
        List<String> origins = new ArrayList<>(DEFAULT_FRONTEND_ORIGINS);
        origins.addAll(splitCorsValues(frontendUrl).stream()
                .filter(origin -> !origin.contains("*"))
                .toList());
        origins.addAll(splitCorsValues(allowedOrigins).stream()
                .filter(origin -> !origin.contains("*"))
                .toList());

        return origins.stream()
                .distinct()
                .toList();
    }

    private List<String> allowedCorsOriginPatterns() {
        List<String> patterns = new ArrayList<>();
        patterns.addAll(splitCorsValues(allowedOriginPatterns));
        patterns.addAll(splitCorsValues(frontendUrl).stream()
                .filter(origin -> origin.contains("*"))
                .toList());
        patterns.addAll(splitCorsValues(allowedOrigins).stream()
                .filter(origin -> origin.contains("*"))
                .toList());

        return patterns.stream()
                .distinct()
                .toList();
    }

    private List<String> splitCorsValues(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(this::removeTrailingSlash)
                .toList();
    }

    private String removeTrailingSlash(String value) {
        if (value.length() > 1 && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}
