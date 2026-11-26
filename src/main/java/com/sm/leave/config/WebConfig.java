package com.sm.leave.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * WebConfig 只負責 SPA 靜態資源的 fallback 路由。
 * CORS 設定已移至 SecurityConfig.corsConfigurationSource()，
 * 確保 preflight OPTIONS 請求在 Security filter 層就能正確處理。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 靜態資源存在（.js / .css / .png 等）直接回傳
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // 前端 SPA 路由（/dashboard、/register 等）統一回傳 index.html
                        return location.createRelative("index.html");
                    }
                });
    }
}