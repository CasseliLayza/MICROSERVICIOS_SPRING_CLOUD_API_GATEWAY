package com.backend.springcloud.app.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered {
    private final Logger LOGGER = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        LOGGER.info("Ejecutando el Filtro antes del request PRE");

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .headers(h -> h.add("token", "thisIsATokenSha256"))
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(modifiedRequest)
                .build();

        String AllRequestHeaders = modifiedExchange.getRequest().getHeaders().toString();
        LOGGER.info("AllRequestHeaders>>>>>>>>>>: " + AllRequestHeaders);

        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            LOGGER.info("Ejecutando el Filtro POST response");
            String token = modifiedExchange.getRequest().getHeaders().getFirst("token");
            if (token != null) {
                LOGGER.info("Token1>>>>>>>>>>: " + token);
                modifiedExchange.getResponse().getHeaders().set("token1", token);
            }

            Optional.of(modifiedExchange.getRequest().getHeaders().getFirst("token"))
                    .ifPresent(value -> {
                        LOGGER.info("Token2>>>>>>>>>>: " + value);
                        modifiedExchange.getResponse().getHeaders().add("token2", value);
                    });

            modifiedExchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "red").build());
            //modifiedExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);

        }));
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
