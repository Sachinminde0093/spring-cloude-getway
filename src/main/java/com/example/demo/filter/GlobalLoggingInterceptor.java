package com.example.demo.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

@Component
public class GlobalLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalLoggingInterceptor.class);

    private static final String START_TIME = "startTime";
    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        request.setAttribute(START_TIME, System.currentTimeMillis());

        String correlationId = request.getHeader(CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);

        response.setHeader(CORRELATION_ID, correlationId);

        Map<String, String> headers = extractHeaders(request, correlationId);

        log.info("""
[{}] [{} {}] Incoming request - headers: {}
""", correlationId, request.getMethod(), request.getRequestURI(), headers);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        try {
            long start = (long) request.getAttribute(START_TIME);
            long timeTaken = System.currentTimeMillis() - start;

            log.info("""
[{}] [{} {}] Outgoing response | status={} | time={}ms
""",
                    MDC.get("correlationId"),
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    timeTaken);

            if (ex != null) {
                log.error("Request completed with exception", ex);
            }
        } finally {
            MDC.clear();
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request,
                                               String correlationId) {

        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (isSensitive(name)) continue;
            headers.put(name, request.getHeader(name));
        }

        headers.put(CORRELATION_ID, correlationId);
        return headers;
    }

    private boolean isSensitive(String header) {
        String h = header.toLowerCase();
        return h.contains("authorization")
                || h.contains("cookie")
                || h.contains("token");
    }
}
