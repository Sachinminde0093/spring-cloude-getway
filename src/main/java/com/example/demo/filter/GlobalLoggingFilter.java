package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GlobalLoggingFilter {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalLoggingFilter.class);

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> loggingFilter() {

        return (request, next) -> {

            long startTime = System.currentTimeMillis();

            String correlationId =
                    request.headers().firstHeader("X-Correlation-Id");

            if (correlationId == null) {
                correlationId = java.util.UUID.randomUUID().toString();
            }

            log.info(
                    "[{}] ➡ {} {}",
                    correlationId,
                    request.method(),
                    request.path()
            );

            ServerResponse response = next.handle(request);

            long duration = System.currentTimeMillis() - startTime;

            log.info(
                    "[{}] ⬅ {} {} | status={} | time={}ms",
                    correlationId,
                    request.method(),
                    request.path(),
                    response.statusCode(),
                    duration
            );

            return response;
        };
    }
}
