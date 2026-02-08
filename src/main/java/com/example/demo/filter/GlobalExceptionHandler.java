package com.example.demo.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler implements HandlerExceptionResolver {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        logGatewayError(request, ex);

        try {
            Throwable root = getRootCause(ex);

            if (ex instanceof IllegalArgumentException) {
                write(response, 400, "Bad Request");
            }
            else if (ex instanceof SecurityException) {
                write(response, 403, "Forbidden");
            }
            else if (ex instanceof ResourceAccessException
                    || root instanceof ConnectException
                    || root instanceof UnknownHostException) {

                write(response, 502, "Downstream service unavailable");
            }
            else if (root instanceof SocketTimeoutException) {
                write(response, 504, "Gateway timeout");
            }
            else {
                write(response, 500, "Internal Gateway Error");
            }

        } catch (IOException ignored) {}

        return new ModelAndView(); // handled
    }

    // ---------------- helpers ----------------

    private void logGatewayError(HttpServletRequest request, Exception ex) {

        log.error("""
[GATEWAY ERROR]
CorrelationId : {}
Method        : {}
Path          : {}
Exception     : {}
RootCause     : {}
Message       : {}
""",
                MDC.get("correlationId"),
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getName(),
                getRootCause(ex).getClass().getName(),
                ex.getMessage(),
                ex
        );
    }

    private Throwable getRootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private void write(HttpServletResponse response,
                       int status,
                       String message) throws IOException {

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write("""
            {
              "error": "%s",
              "correlationId": "%s"
            }
        """.formatted(message, MDC.get("correlationId")));
    }
}
