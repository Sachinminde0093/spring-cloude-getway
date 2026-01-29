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

    private final List<GatewayRouteDef> allRoutes = new ArrayList<>();

    public GatewayRoutesConfig(List<List<GatewayRouteDef>> routeGroups) {
        // merge all route definition beans
        routeGroups.forEach(allRoutes::addAll);
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        RouterFunction<ServerResponse> result = null;

        for (GatewayRouteDef def : allRoutes) {

            HandlerFilterFunction<ServerResponse, ServerResponse> routeFilter =
                    combine(def.extraFilters());

            for (HttpMethod method : def.methods()) {

                RouterFunction<ServerResponse> rf =
                        buildRoute(def, method, routeFilter);

                result = (result == null) ? rf : result.and(rf);
            }
        }
        return result;
    }

    /**
     * Combine route-specific filters only
     */
    private HandlerFilterFunction<ServerResponse, ServerResponse> combine(
            List<HandlerFilterFunction<ServerResponse, ServerResponse>> filters) {

        // ✅ Correct no-op filter for WebMVC
        HandlerFilterFunction<ServerResponse, ServerResponse> chain =
                (request, next) -> next.handle(request);

        for (HandlerFilterFunction<ServerResponse, ServerResponse> filter : filters) {
            chain = chain.andThen(filter);
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
