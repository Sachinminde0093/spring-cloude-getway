package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

//    @Bean
//    public RouterFunction<ServerResponse> customRoutes() {
//        // @formatter:off
//        return route("path_route")
//                .GET("/get", http())
//                .before(uri("https://httpbin.org"))
//                .build().and(route("host_route")
//                        .route(host("*.myhost.org"), http())
//                        .before(uri("https://httpbin.org"))
//                        .build());
//    }
}
