package com.neogul.whynago;

import org.springframework.boot.SpringApplication;

public class TestWhynagoApplication {

    public static void main(String[] args) {
        SpringApplication.from(WhynagoApplication::main).with(TestcontainersConfiguration.class)
                .run(args);
    }

}
