package com.example.demo.apiservice;

import com.example.demo.filter.AuthFilter;
import com.example.demo.filter.AuditFilter;
import com.example.demo.model.GatewayRouteDef;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

@Configuration
public class Service8082Routes {

    @Bean
    public List<GatewayRouteDef> service8082RouteDefs(
            AuthFilter authFilter,
            AuditFilter auditFilter) {

        return List.of(

                new GatewayRouteDef(
                        "demo",
                        "/get82",
                        "http://localhost:8082",
                        "/employee",
                        Set.of(HttpMethod.GET, HttpMethod.POST),
                        List.of(authFilter.filter(), auditFilter.filter())
                )
        );
    }
}
