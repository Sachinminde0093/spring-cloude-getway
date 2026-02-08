package com.example.demo.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class GlobalLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalLoggingInterceptor.class);

    private static final String START_TIME = "startTime";
    private static final String CORRELATION_ID = "X-Correlation-Id";
    private static final String REQUEST_ID = "X-Request-Id";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        request.setAttribute(START_TIME, System.currentTimeMillis());

        String requestId = UUID.randomUUID().toString();
        String correlationId = Optional
                .ofNullable(request.getHeader(CORRELATION_ID))
                .orElse(UUID.randomUUID().toString());

        MDC.put("requestId", requestId);
        MDC.put("correlationId", correlationId);

        response.setHeader(REQUEST_ID, requestId);
        response.setHeader(CORRELATION_ID, correlationId);

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

            // 🔥 Gateway REQUEST (payload only for POST / PUT)
            log.info("""
[GATEWAY REQUEST]
RequestId     : {}
CorrelationId : {}
Method        : {}
Path          : {}
Headers       : {}
Payload       : {}
""",
                    MDC.get("requestId"),
                    MDC.get("correlationId"),
                    request.getMethod(),
                    request.getRequestURI(),
                    extractHeaders(request),
                    extractPayload(request)
            );

            // 🔥 Gateway RESPONSE (NO payload)
            log.info("""
[GATEWAY RESPONSE]
RequestId     : {}
CorrelationId : {}
Status        : {}
TimeTaken(ms): {}
""",
                    MDC.get("requestId"),
                    MDC.get("correlationId"),
                    response.getStatus(),
                    timeTaken
            );

        } finally {
            MDC.clear();
        }
    }

    // ---------------- helpers ----------------

    private boolean shouldLogPayload(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                || "PUT".equals(request.getMethod());
    }

    private String extractPayload(HttpServletRequest request) {

        if (!shouldLogPayload(request)) {
            return "<payload-skipped>";
        }

        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return "<payload-not-cached>";
        }

        byte[] body = wrapper.getContentAsByteArray();
        if (body.length == 0) {
            return "<empty>";
        }

        return new String(body, StandardCharsets.UTF_8);
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (isSensitive(name)) continue;
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    private boolean isSensitive(String header) {
        String h = header.toLowerCase();
        return h.contains("authorization")
                || h.contains("cookie")
                || h.contains("token");
    }
}
