package com.dinesync.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

/**
 * DatabaseConfig — configures Spring Security to allow all requests
 * and ensures DataSource URL has proper jdbc: prefix for PostgreSQL driver.
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String raw = dbUrl != null ? dbUrl.trim() : "";
        String cleanUrl = raw.startsWith("jdbc:") ? raw.substring(5) : raw;

        String finalUrl = raw.startsWith("jdbc:") ? raw : "jdbc:" + raw;
        String finalUser = (username != null && !username.isBlank()) ? username.trim() : null;
        String finalPass = (password != null && !password.isBlank()) ? password.trim() : null;

        if (cleanUrl.contains("@")) {
            int schemeIdx = cleanUrl.indexOf("://");
            String afterScheme = schemeIdx >= 0 ? cleanUrl.substring(schemeIdx + 3) : cleanUrl;
            int atIdx = afterScheme.lastIndexOf('@');
            String userPassPart = afterScheme.substring(0, atIdx);
            String hostDbPart = afterScheme.substring(atIdx + 1);

            int colonIdx = userPassPart.indexOf(':');
            if (colonIdx >= 0) {
                if (finalUser == null || finalUser.isBlank()) {
                    finalUser = userPassPart.substring(0, colonIdx);
                }
                if (finalPass == null || finalPass.isBlank()) {
                    finalPass = userPassPart.substring(colonIdx + 1);
                }
            } else {
                if (finalUser == null || finalUser.isBlank()) {
                    finalUser = userPassPart;
                }
            }

            finalUrl = "jdbc:postgresql://" + hostDbPart;
            if (!finalUrl.contains("?")) {
                finalUrl += "?sslmode=require";
            }
        }


        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(finalUrl);
        if (finalUser != null && !finalUser.isBlank()) {
            config.setUsername(finalUser);
        }
        if (finalPass != null && !finalPass.isBlank()) {
            config.setPassword(finalPass);
        }
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

    /**
     * Explicitly configure Spring Security for this app.
     * DineSync handles its own auth flow via the custom /api/auth endpoints,
     * so the default generated login page/user is not needed.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails internalUser = User.withUsername("dinesync")
                .password("{noop}dinesync-internal")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(internalUser);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/signup.html",
                    "/restaurants.html",
                    "/restaurant.html",
                    "/reservation.html",
                    "/my-reservations.html",
                    "/profile.html",
                    "/confirmation.html",
                    "/css/**",
                    "/js/**",
                    "/api/auth/**",
                    "/api/restaurants/**",
                    "/api/admin/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
