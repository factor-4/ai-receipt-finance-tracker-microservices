package com.kulubotti.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow cookies and authenticated sessions
        config.setAllowCredentials(true);

        // EXPLICITLY allow your Render frontend (Make sure there is NO trailing slash!)
        config.setAllowedOrigins(Arrays.asList(
                "https://kulubotti-frontend.onrender.com",
                "http://localhost:5173" // Keep this so it still works locally!
        ));

        // Allow all headers (like Authorization for your JWT)
        config.setAllowedHeaders(Arrays.asList("*"));

        // Allow all standard web actions, especially OPTIONS for preflight
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}