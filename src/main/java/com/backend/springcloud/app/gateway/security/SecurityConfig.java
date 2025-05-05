package com.backend.springcloud.app.gateway.security;

import jakarta.ws.rs.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http.authorizeExchange(authz -> {
                    authz.pathMatchers("/authorized", "logout").permitAll()
                            .pathMatchers(HttpMethod.GET, "/api/a/products/list", "/api/b/items/list", "/api/c/users/list").permitAll()
                            .pathMatchers(HttpMethod.GET, "/api/a/products/find/{id}", "/api/b/items/find/{id}", "/api/c/users/find/{id}").hasAnyRole("USER", "ADMIN")
                            .pathMatchers("/api/a/products/**", "/api/b/items/**", "/api/c/users/**").hasRole("ADMIN")
                            /*.pathMatchers(HttpMethod.POST, "/api/a/products/create", "/api/b/items/create", "/api/c/users/create").hasRole("ADMIN")
                            .pathMatchers(HttpMethod.PUT, "/api/a/products/update/{id}", "/api/b/items/update/{id}", "/api/c/users/update/{id}").hasRole("ADMIN")
                            .pathMatchers(HttpMethod.DELETE, "/api/a/products/delete/{id}", "/api/b/items/delete/{id}", "/api/c/users/delete/{id}").hasRole("ADMIN")*/
                            .anyExchange().authenticated();
                }).cors(csrf -> csrf.disable())
                .oauth2Login(withDefaults())
                .oauth2Client(withDefaults())
                .oauth2ResourceServer(oAuth2 -> oAuth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(new Converter<Jwt, Mono<AbstractAuthenticationToken>>() {

                                    @Override
                                    public Mono<AbstractAuthenticationToken> convert(Jwt source) {
                                        Collection<String> roles = source.getClaimAsStringList("roles");
                                        Collection<GrantedAuthority> authorities = roles.stream()
                                                .map(SimpleGrantedAuthority::new)
                                                .collect(Collectors.toList());

                                        return Mono.just(new JwtAuthenticationToken(source, authorities));
                                    }
                                })

                        )
                )
                .build();
    }

}





/*
mvc
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests((authz) -> {
                    authz
                            .requestMatchers("/authorized", "/logout").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/products", "/api/items", "/api/users").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/products/{id}", "/api/items/{id}", "/api/users/{id}").hasAnyRole("ADMIN", "USER")
                            .requestMatchers("/api/products/**", "/api/items/**", "/api/users/**").hasRole("ADMIN")
                            .anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .oauth2Login(login -> login.loginPage("/oauth2/authorization/client-app"))
                .oauth2Client(withDefaults())
                .oauth2ResourceServer(withDefaults())
                .build();
    }

}*/
