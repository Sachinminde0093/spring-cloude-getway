package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class GlobalLoggingFilter {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalLoggingFilter.class);

    // Safe allow-list of headers
    private static final List<String> LOGGABLE_HEADERS = List.of(
            "User-Agent",
            "X-Correlation-Id"
    );

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> loggingFilter() {

        return (request, next) -> {

            long startTime = System.currentTimeMillis();

            // Correlation ID
            String correlationId =
                    request.headers().firstHeader("X-Correlation-Id");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }

            MDC.put("correlationId", correlationId);

            try {
                // ---- INCOMING REQUEST ----
                log.info(
                        "IN  | method={} path={}",
                        request.method(),
                        request.path()
                );

                String headerLine = buildHeaderLine(request);
                if (!headerLine.isEmpty()) {
                    log.info("headers={}", headerLine);
                }

                ServerResponse response = next.handle(request);

                long duration = System.currentTimeMillis() - startTime;

                // ---- OUTGOING RESPONSE ----
                log.info(
                        "OUT | method={} path={} status={} time={}ms",
                        request.method(),
                        request.path(),
                        response.statusCode(),
                        duration
                );

                return response;
            } finally {
                MDC.clear();
            }
        };
    }

    private String buildHeaderLine(ServerRequest request) {
        return LOGGABLE_HEADERS.stream()
                .map(h -> {
                    String v = request.headers().firstHeader(h);
                    return v != null ? h + "=" + v : null;
                })
                .filter(v -> v != null)
                .collect(Collectors.joining(", "));
    }
}
