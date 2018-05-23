package com.github.turistpro.nettail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class NetTailApplication {
    public static void main(String... args) {
        SpringApplication.run(NetTailApplication.class, args);
    }
}
