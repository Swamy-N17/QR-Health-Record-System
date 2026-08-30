package com.clinic.qrhealthrecord.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.clinic.qrhealthrecord.security.CustomAuthEntryPoint;
import com.clinic.qrhealthrecord.security.CustomAccessDeniedHandler;
import com.clinic.qrhealthrecord.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder,
                           CustomAuthEntryPoint customAuthEntryPoint,
                           CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password",
                        "/css/**", "/js/**",
                        "/", "/index.html", "/login.html", "/forgot-password.html", "/reset-password.html", "/access-denied.html"
                ).permitAll()
                .requestMatchers("/api/auth/logout").permitAll()
                .requestMatchers("/api/auth/change-password").authenticated()
                .requestMatchers("/api/super-admin/**", "/super-admin-dashboard.html").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/**", "/admin-dashboard.html",
                        "/register-doctor.html", "/register-patient.html",
                        "/manage-doctors.html", "/manage-patients.html").hasRole("ADMIN")
                .requestMatchers("/api/doctor/**", "/doctor-dashboard.html").hasRole("DOCTOR")
                .requestMatchers("/api/patient/**", "/patient-dashboard.html").hasRole("PATIENT")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) ->
                    response.setStatus(HttpStatus.OK.value())
                )
            );

        return http.build();
    }
}