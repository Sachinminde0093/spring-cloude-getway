package com.example.demo.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class AuthFilter {

    public HandlerFilterFunction<ServerResponse, ServerResponse> filter() {
        return (req, next) -> {
            if (req.headers().firstHeader("Authorization") == null) {
                return ServerResponse.status(401).body("Unauthorized");
            }
            return next.handle(req);
        };
    }
}
