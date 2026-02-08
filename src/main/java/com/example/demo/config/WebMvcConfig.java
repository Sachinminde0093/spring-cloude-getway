package com.example.demo.config;


import com.example.demo.filter.GlobalLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class  WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private GlobalLoggingInterceptor globalLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalLoggingInterceptor)
                .addPathPatterns("/**");
    }
}
