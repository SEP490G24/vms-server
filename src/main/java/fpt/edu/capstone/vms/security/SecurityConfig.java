package fpt.edu.capstone.vms.security;


import fpt.edu.capstone.vms.component.IpRateLimitingFilter;
import fpt.edu.capstone.vms.security.converter.JwtAuthenticationConverter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan(basePackages = {"fpt.edu.capstone.vms.security.converter"})
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final String jwkSetUri;

    @Bean
    public IpRateLimitingFilter ipRateLimitingFilter() {
        return new IpRateLimitingFilter();
    }

    public SecurityConfig(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.jwkSetUri = jwkSetUri;
    }

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity httpSecurity,
            @Value("${edu.fpt.capstone.vms.permitAll}") String[] permitAll
    ) throws Exception {

        // Enable and configure CORS
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Set up http security to use the JWT converter defined above
        httpSecurity
                .oauth2ResourceServer(customizer -> customizer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                                .jwkSetUri(jwkSetUri)));

        httpSecurity
            .addFilterBefore(ipRateLimitingFilter(), UsernamePasswordAuthenticationFilter.class)
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
