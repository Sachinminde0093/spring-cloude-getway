package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DownstreamLoggingInterceptor
        implements ClientHttpRequestInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(DownstreamLoggingInterceptor.class);

    // 🔐 Internal token (example – normally from config / vault)
    private static final String INTERNAL_TOKEN =
            "Bearer my-internal-service-token-12345";

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution)
            throws IOException {

        String requestId = MDC.get("requestId");
        String correlationId = MDC.get("correlationId");

        // 1️⃣ ADD TOKEN TO DOWNSTREAM REQUEST (INTERNAL ONLY)
        request.getHeaders().add(HttpHeaders.AUTHORIZATION, INTERNAL_TOKEN);

        // 2️⃣ Log downstream REQUEST (token masked)
        log.info("""
[DOWNSTREAM REQUEST]
RequestId     : {}
CorrelationId : {}
Method        : {}
URI           : {}
Headers       : {}
Payload       : {}
""",
                requestId,
                correlationId,
                request.getMethod(),
                request.getURI(),
                filterHeaders(request.getHeaders()),
                shouldLogPayload(request)
                        ? (body.length == 0 ? "<empty>"
                        : new String(body, StandardCharsets.UTF_8))
                        : "<payload-skipped>"
        );

        ClientHttpResponse response =
                execution.execute(request, body);

        // 3️⃣ Log downstream RESPONSE (NO body)
        log.info("""
[DOWNSTREAM RESPONSE]
RequestId     : {}
CorrelationId : {}
Status        : {}
""",
                requestId,
                correlationId,
                response.getStatusCode()
        );

        return response;
    }

    // ---------------- helpers ----------------

    private boolean shouldLogPayload(HttpRequest request) {
        return request.getMethod() != null &&
                ("POST".equals(request.getMethod().name())
                        || "PUT".equals(request.getMethod().name()));
    }

    // 🔐 Mask sensitive headers before logging
    private Map<String, String> filterHeaders(HttpHeaders headers) {

        Map<String, String> filtered = new LinkedHashMap<>();

        headers.forEach((key, values) -> {
            String lower = key.toLowerCase();

            if (lower.contains("authorization")) {
                // mask token
                filtered.put(key, maskToken(values));
                return;
            }

            if (lower.contains("cookie") || lower.contains("token")) {
                return; // skip completely
            }

            filtered.put(key, String.join(",", values));
        });

        return filtered;
    }

    private String maskToken(java.util.List<String> values) {
        if (values.isEmpty()) return "<empty>";
        String token = values.get(0);
        if (token.length() <= 10) return "****";
        return token.substring(0, 10) + "****";
    }
}
