package com.example.bcapi;

import org.springframework.boot.SpringApplication;

public class TestBcapiApplication {

    public static void main(String[] args) {
        SpringApplication.from(BcapiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
