package com.techtalentpulse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile({"dev", "demo"})
public class LocalFrontendCorsConfig implements WebMvcConfigurer {

  private final String allowedOrigins;

  public LocalFrontendCorsConfig(
      @Value("${tech-talent-pulse.web.cors.allowed-origins}") String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(parseAllowedOrigins())
        .allowedMethods("GET", "POST", "OPTIONS");
    registry
        .addMapping("/actuator/health")
        .allowedOrigins(parseAllowedOrigins())
        .allowedMethods("GET", "OPTIONS");
  }

  private String[] parseAllowedOrigins() {
    return allowedOrigins.trim().split("\\s*,\\s*");
  }
}
