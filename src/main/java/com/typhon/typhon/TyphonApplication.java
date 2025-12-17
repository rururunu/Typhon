package com.typhon.typhon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.typhon.typhon.mapper")
public class TyphonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TyphonApplication.class, args);
    }

}
