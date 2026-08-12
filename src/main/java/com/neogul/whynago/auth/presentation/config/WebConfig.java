package com.neogul.whynago.auth.presentation.config;

import com.neogul.whynago.auth.presentation.interceptor.AuthInterceptor;
import com.neogul.whynago.auth.presentation.interceptor.OptionalAuthInterceptor;
import com.neogul.whynago.auth.presentation.resolver.LoginUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final OptionalAuthInterceptor optionalAuthInterceptor;
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/questions",
                        "/api/questions/*"
                );
        // 문제 목록·단건 조회는 비로그인도 열람할 수 있고, 로그인 상태면 푼 문제를 표시한다.
        registry.addInterceptor(optionalAuthInterceptor)
                .addPathPatterns("/api/questions", "/api/questions/*");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://why-na-go.vercel.app", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}