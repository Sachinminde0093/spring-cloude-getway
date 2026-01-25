package com.example.demo.model;

import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Set;

public record GatewayRouteDef(
        String id,
        String gatewayPath,
        String downstreamUri,
        String downstreamPath,
        Set<HttpMethod> methods,
        List<HandlerFilterFunction<ServerResponse, ServerResponse>> extraFilters
) {}
