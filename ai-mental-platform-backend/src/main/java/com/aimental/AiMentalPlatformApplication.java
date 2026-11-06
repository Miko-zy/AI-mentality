package com.aimental;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI陪伴与心理韧性成长平台启动类
 */
@SpringBootApplication
@MapperScan("com.aimental.mapper")
public class AiMentalPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMentalPlatformApplication.class, args);
    }
}
