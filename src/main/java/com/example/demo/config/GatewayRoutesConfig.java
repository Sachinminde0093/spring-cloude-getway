package com.example.demo.config;

import com.example.demo.model.GatewayRouteDef;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayRoutesConfig {

    private final HandlerFilterFunction<ServerResponse, ServerResponse> commonFilter;
    private final List<GatewayRouteDef> allRoutes = new ArrayList<>();

    public GatewayRoutesConfig(
            HandlerFilterFunction<ServerResponse, ServerResponse> exceptionFilter,
            HandlerFilterFunction<ServerResponse, ServerResponse> loggingFilter,
            List<List<GatewayRouteDef>> routeGroups) {

        // Common filters auto-applied
        this.commonFilter = exceptionFilter.andThen(loggingFilter);

        // merge all route definition beans
        routeGroups.forEach(allRoutes::addAll);
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        RouterFunction<ServerResponse> result = null;

        for (GatewayRouteDef def : allRoutes) {

            HandlerFilterFunction<ServerResponse, ServerResponse> finalFilter =
                    combine(commonFilter, def.extraFilters());

            for (HttpMethod method : def.methods()) {

                RouterFunction<ServerResponse> rf =
                        buildRoute(def, method, finalFilter);

                result = (result == null) ? rf : result.and(rf);
            }
        }
        return result;
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse> combine(
            HandlerFilterFunction<ServerResponse, ServerResponse> common,
            List<HandlerFilterFunction<ServerResponse, ServerResponse>> extras) {

        HandlerFilterFunction<ServerResponse, ServerResponse> chain = common;
        for (var f : extras) {
            chain = chain.andThen(f);
        }
        return chain;
    }

    private RouterFunction<ServerResponse> buildRoute(
            GatewayRouteDef def,
            HttpMethod method,
            HandlerFilterFunction<ServerResponse, ServerResponse> filter) {

        var builder = route(def.id() + "_" + method.name().toLowerCase());

        if (method == HttpMethod.GET) {
            return builder.GET(def.gatewayPath(), http())
                    .filter(filter)
                    .before(rewritePath(def.gatewayPath(), def.downstreamPath()))
                    .before(uri(def.downstreamUri()))
                    .build();
        }

        if (method == HttpMethod.POST) {
            return builder.POST(def.gatewayPath(), http())
                    .filter(filter)
                    .before(rewritePath(def.gatewayPath(), def.downstreamPath()))
                    .before(uri(def.downstreamUri()))
                    .build();
        }

        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }
}
