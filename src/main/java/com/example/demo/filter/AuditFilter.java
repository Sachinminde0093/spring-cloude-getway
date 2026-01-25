package com.example.demo.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class AuditFilter {

    public HandlerFilterFunction<ServerResponse, ServerResponse> filter() {
        return (req, next) -> {
            System.out.println("AUDIT -> " + req.path());
            return next.handle(req);
        };
    }
}
