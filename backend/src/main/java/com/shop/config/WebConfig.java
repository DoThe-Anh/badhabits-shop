package com.shop.config;

import com.shop.entity.Cart;
import com.shop.service.CartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:../uploads}")
    private String uploadDir;

    @Bean
    @SessionScope
    public Cart sessionCart() {
        return new Cart();
    }

    @Bean
    @SessionScope
    public CartService cartService(Cart cart) {
        return new CartService(cart);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Phục vụ file ở thư mục uploads/ (cấu hình ở app.upload.dir) tại URL /uploads/**
        Path absolute = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolute.toUri().toString());
    }
}
