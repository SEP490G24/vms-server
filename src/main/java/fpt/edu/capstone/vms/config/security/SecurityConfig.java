package fpt.edu.capstone.vms.config.security;


import fpt.edu.capstone.vms.security.KeycloakJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * https://reflectoring.io/spring-cors/
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan(basePackages = {"fpt.edu.capstone.vms.security"})
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
    private final String jwkSetUri;

    public SecurityConfig(
            KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) {
        this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
        this.jwkSetUri = jwkSetUri;
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            @Value("${edu.fpt.capstone.permitAll}") String[] permitAll
    ) throws Exception {

        // Enable and configure CORS
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Set up http security to use the JWT converter defined above
        httpSecurity
                .oauth2ResourceServer(customizer -> customizer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
                                .jwkSetUri(jwkSetUri)));

        httpSecurity
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(permitAll).permitAll()
                        .anyRequest()
                        .authenticated()
                );

        // State-less session (state in access-token only)
        httpSecurity.sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Disable CSRF because of state-less session-management
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    public CorsConfigurationSource corsConfigurationSource() {
        final var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));

        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
