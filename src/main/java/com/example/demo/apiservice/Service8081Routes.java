package com.example.demo.apiservice;

import com.example.demo.model.GatewayRouteDef;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

@Configuration
public class Service8081Routes {

    @Bean
    public List<GatewayRouteDef> service8081RouteDefs() {

        return List.of(

                new GatewayRouteDef(
                        "getemp",
                        "/get81",
                        "http://localhost:8081",
                        "/employee",
                        Set.of(HttpMethod.GET, HttpMethod.POST),
                        List.of()   // only common filters
                )
        );
    }
}
