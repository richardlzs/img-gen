package com.micerlabs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.micerlabs.mapper")
public class RunApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RunApplication.class);
        //为了确保安全，请在相同路径新建application-local.yml文档以本地化保存密码，并添加到.gitignore中
        app.setDefaultProperties(Collections.singletonMap("spring.config.name", "application-local"));

        app.run(args);
    }
}
