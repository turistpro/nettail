package com.github.turistpro.nettail;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class CustomWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!(exchange.getRequest().getURI().getPath().startsWith("/api") ||
                exchange.getRequest().getURI().getPath().endsWith(".js") ||
                exchange.getRequest().getURI().getPath().endsWith(".css")
        )) {
            return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().path("/index.html").build()).build());
        }

        return chain.filter(exchange);
    }
}