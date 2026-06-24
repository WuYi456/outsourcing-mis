package com.tx.outsourcingmis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 外包人员管理系统启动类
 *
 * <p>Spring Boot 应用入口，启动时自动扫描 Mapper 接口并启用异步处理。
 */
@SpringBootApplication
@MapperScan("com.tx.outsourcingmis.mapper")
@EnableAsync
public class OutsourcingMisApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutsourcingMisApplication.class, args);
    }
}