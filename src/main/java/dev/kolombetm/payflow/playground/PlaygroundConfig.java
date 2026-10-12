package dev.kolombetm.payflow.playground;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaygroundConfig {

    @Bean
    Greeter Greeter() {
        System.out.printf("Create greeter");
        return new Greeter();
    }
}
