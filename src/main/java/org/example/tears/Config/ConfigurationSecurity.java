package org.example.tears.Config;


import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfigurationSecurity {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public ConfigurationSecurity(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ================= SWAGGER =================
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/"
                        ).permitAll()

                        // ================= AUTH =================
                        .requestMatchers("/api/v1/tears/auth/**").permitAll()

                        // ================= CONTENT (TERMS / PRIVACY / FAQS) =================
                        .requestMatchers("/api/v1/tears/content/**").permitAll()

                        // ================= PUBLIC CAR DATA =================
                        .requestMatchers(
                                "/api/v1/tears/cars/brands",
                                "/api/v1/tears/cars/brands/**",
                                "/api/v1/tears/cars/search-brand",
                                "/api/v1/tears/cars/search-model",
                                "/api/v1/tears/cars/filter"
                        ).permitAll()

                        // ================= USER =================
                        .requestMatchers(
                                "/api/v1/tears/users/customer/profile",
                                "/api/v1/tears/users/employee/profile",
                                "/api/v1/tears/users/update",
                                "/api/v1/tears/users/notifications"
                        ).authenticated()

                        // ================= CUSTOMER =================
                        .requestMatchers(
                                "/api/v1/tears/customer/**",
                                "/api/v1/tears/cars/my-car",
                                "/api/v1/tears/cars/register/**",
                                "/api/v1/tears/service-request/**",
                                "/api/v1/tears/cars/extract-owner",
                                "/api/v1/tears/cars/extract-user-name"
                        ).hasRole("CUSTOMER")

                        // ================= EMPLOYEE =================
                        .requestMatchers("/api/v1/tears/employee/**")
                        .hasRole("EMPLOYEE")

                        // ================= PRICING =================
                        .requestMatchers("/api/v1/tears/pricing/**")
                        .hasRole("PRICING")

                        // ================= ADMIN =================
                        .requestMatchers(
                                "/api/v1/tears/admin/**",
                                "/api/v1/tears/dashboard/admin/**",
                                "/admin/payment-settings/**"
                        ).hasRole("ADMIN")

                        // ================= STATIC =================
                        .requestMatchers("/uploads/**", "/carimage/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}