package playground;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaygroundConfig {

    @Bean
    Greeter greeter() {
        System.out.println("Create greeter");
        return new Greeter();
    }
}
