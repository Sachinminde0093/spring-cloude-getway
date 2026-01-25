package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GlobalExceptionFilter {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionFilter.class);

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> exceptionFilter() {

        return (request, next) -> {
            try {
                return next.handle(request);
            } catch (Exception ex) {

                log.error(
                        "Gateway error | {} {}",
                        request.method(),
                        request.path(),
                        ex
                );

                return ServerResponse
                        .status(502)
                        .body("Gateway error");
            }
        };
    }
}
