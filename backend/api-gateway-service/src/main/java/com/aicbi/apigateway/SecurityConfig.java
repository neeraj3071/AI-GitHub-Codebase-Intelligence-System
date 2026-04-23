package com.aicbi.apigateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  @ConditionalOnProperty(name = "auth.enabled", havingValue = "true")
  SecurityFilterChain securedFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/repos/ingest").hasAnyAuthority("ROLE_INGEST", "ROLE_ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/query").hasAnyAuthority("ROLE_QUERY", "ROLE_ADMIN")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  @Bean
  @ConditionalOnProperty(name = "auth.enabled", havingValue = "false", matchIfMissing = true)
  SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::mapAuthorities);
    return converter;
  }

  private Collection<GrantedAuthority> mapAuthorities(Jwt jwt) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    Object rolesClaim = jwt.getClaims().get("roles");
    if (rolesClaim instanceof Collection<?> roles) {
      for (Object role : roles) {
        if (role != null) {
          authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()));
        }
      }
    }

    Object scopeClaim = jwt.getClaims().get("scope");
    if (scopeClaim instanceof String scopeString && !scopeString.isBlank()) {
      for (String scope : scopeString.split("\\s+")) {
        authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
      }
    }

    return authorities;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${auth.allowed-origins:http://localhost:3000}") String allowedOrigins) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
