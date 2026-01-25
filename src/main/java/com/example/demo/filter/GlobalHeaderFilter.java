package com.example.demo.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GlobalHeaderFilter {

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> headerFilter() {

        return (request, next) -> {

            ServerRequest mutatedRequest =
                    ServerRequest.from(request)
                            .header("X-Gateway", "Spring-Cloud-Gateway-WebMVC")
                            .header("X-Correlation-Id",
                                    request.headers()
                                            .firstHeader("X-Correlation-Id") != null
                                            ? request.headers().firstHeader("X-Correlation-Id")
                                            : java.util.UUID.randomUUID().toString())
                            .build();

            return next.handle(mutatedRequest);
        };
    }
}
