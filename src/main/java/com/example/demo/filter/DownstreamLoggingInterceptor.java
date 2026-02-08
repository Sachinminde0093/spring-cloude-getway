package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class DownstreamLoggingInterceptor
        implements ClientHttpRequestInterceptor {

    private static final Logger log =
            LoggerFactory.getLogger(DownstreamLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution)
            throws IOException {

        String requestId = MDC.get("requestId");
        String correlationId = MDC.get("correlationId");

        // 🔹 Propagate headers
        if (requestId != null) {
            request.getHeaders().add("X-Request-Id", requestId);
        }
        if (correlationId != null) {
            request.getHeaders().add("X-Correlation-Id", correlationId);
        }

        // 🔥 Log downstream REQUEST
        log.info("""
[DOWNSTREAM REQUEST]
RequestId     : {}
CorrelationId : {}
Method        : {}
URI           : {}
Headers       : {}
""",
                requestId,
                correlationId,
                request.getMethod(),
                request.getURI(),
                request.getHeaders()
        );

        long start = System.currentTimeMillis();

        try {
            ClientHttpResponse response =
                    execution.execute(request, body);

            long timeTaken = System.currentTimeMillis() - start;

            // 🔥 Log downstream RESPONSE
            log.info("""
[DOWNSTREAM RESPONSE]
RequestId     : {}
CorrelationId : {}
Status        : {}
TimeTaken(ms): {}
""",
                    requestId,
                    correlationId,
                    response.getStatusCode(),
                    timeTaken
            );

            return response;

        } catch (IOException ex) {

            // 🔥 Log downstream ERROR
            log.error("""
[DOWNSTREAM ERROR]
RequestId     : {}
CorrelationId : {}
Method        : {}
URI           : {}
Exception     : {}
Message       : {}
""",
                    requestId,
                    correlationId,
                    request.getMethod(),
                    request.getURI(),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }
}
