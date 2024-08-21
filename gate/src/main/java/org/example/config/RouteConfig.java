package org.example.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
public class RouteConfig {
    @Autowired
    ObjectMapper objectMapper;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("client",
                        r -> r.path("/client/**")
                                .filters(filter -> filter
                                        .stripPrefix(1)
                                        .filter(getGatewayFilter())
                                        .addRequestHeader("uuid", UUID.randomUUID().toString())
                                        .modifyRequestBody(String.class, String.class, modifyRequestBody())
                                        .modifyResponseBody(String.class, String.class, modifyResponseBody()))
                                .uri("http://127.0.0.1:8081"))
                .build();
    }

    private GatewayFilter getGatewayFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            request.mutate()
                    .header("additionalHeader","true");

            String authorization = request.getHeaders().getFirst("Authorization");
            if (authorization == null){
                ServerHttpResponse response = exchange.getResponse();
                DataBuffer dataBuffer = response.bufferFactory().wrap("Authorization is not set".getBytes());
                return response.writeWith(Mono.just(dataBuffer));
            }
            return chain.filter(exchange);
        };
    }

    private RewriteFunction<String, String> modifyRequestBody() {
        return (serverWebExchange, s) -> {
            ServerHttpRequest request = serverWebExchange.getRequest();
            URI uri = request.getURI();
            String path = uri.getPath();

            log.info("{}", path);
            log.info("request body: {}", s);
            log.info("headers: {}", request.getHeaders());

            String modifyRequestBody = "{\"name\": \"replace request\", \"modified\": \"true\"}";
            log.info("modify request body: {}", modifyRequestBody);

            return Mono.just(modifyRequestBody);
        };
    }

    private RewriteFunction<String, String> modifyResponseBody() {
        return (serverWebExchange, s) -> {
            ServerHttpRequest request = serverWebExchange.getRequest();
            URI uri = request.getURI();
            String path = uri.getPath();

            log.info("");
            log.info("{}", path);
            log.info("response body: {}", s);
            log.info("headers: {}", request.getHeaders());

            String replace = s.replace("}", "\n,\"uuid\":\"" + request.getHeaders().getFirst("uuid") + "\"");
            log.info("modify response body: {}", replace);

            return Mono.just(replace);
        };
    }
}
