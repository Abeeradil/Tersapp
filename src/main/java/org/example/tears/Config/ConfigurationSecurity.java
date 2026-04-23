package org.example.tears.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfigurationSecurity {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ================= SWAGGER =================
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ================= AUTH (OPEN) =================
                        .requestMatchers("/api/v1/tears/auth/**").permitAll()

                        // ================= CONTENT (OPEN) =================
                        .requestMatchers("/api/v1/tears/content/**").permitAll()

                        // ================= PUBLIC CAR DATA (OPEN) =================
                        .requestMatchers(
                                "/api/v1/tears/cars/brands",
                                "/api/v1/tears/cars/brands/**",
                                "/api/v1/tears/cars/search-brand",
                                "/api/v1/tears/cars/search-model",
                                "/api/v1/tears/cars/filter"
                        ).permitAll()

                        // ================= USER PROFILE =================
                        .requestMatchers(
                                "/api/v1/tears/users/profile",
                                "/api/v1/tears/users/update",
                                "/api/v1/tears/users/notifications"
                        ).authenticated()

                        // ================= CUSTOMER ONLY =================
                        .requestMatchers("/api/v1/tears/customer/**").hasAuthority("CUSTOMER")

                        .requestMatchers("/api/v1/tears/cars/my-car")
                        .hasAuthority("CUSTOMER")

                        .requestMatchers("/api/v1/tears/cars/register/**")
                        .hasAuthority("CUSTOMER")

                        .requestMatchers("/api/v1/tears/service-request/**")
                        .hasAuthority("CUSTOMER")

                        // ================= EMPLOYEE =================
                        .requestMatchers("/api/v1/tears/employee/**")
                        .hasAuthority("EMPLOYEE")

                        // ================= PRICING ROLE =================
                        .requestMatchers("/api/v1/tears/pricing/**")
                        .hasAuthority("PRICING")

                        // ================= ADMIN =================
                        .requestMatchers("/api/v1/tears/admin/**").hasAuthority("ADMIN")

                        .requestMatchers("/api/v1/tears/dashboard/admin/**")
                        .hasAuthority("ADMIN")

                        // ================= PAYMENT SETTINGS =================
                        .requestMatchers("/admin/payment-settings/**")
                        .hasAuthority("ADMIN")

                        // ================= DEFAULT =================
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}