package com.books.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * Custom Gateway Filter that transcribes POST requests to other HTTP methods.
 * This allows frontends that only support POST to interact with RESTful APIs.
 */
@Component
public class TranscribeMethodGatewayFilterFactory
    extends AbstractGatewayFilterFactory<TranscribeMethodGatewayFilterFactory.Config> {

    public TranscribeMethodGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpMethod targetMethod = HttpMethod.valueOf(config.getTargetMethod().toUpperCase());

            ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .method(targetMethod)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    public static class Config {
        private String targetMethod;

        public String getTargetMethod() {
            return targetMethod;
        }

        public void setTargetMethod(String targetMethod) {
            this.targetMethod = targetMethod;
        }
    }
}
