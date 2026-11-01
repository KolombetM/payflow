package playground;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ComponentScan(basePackages = "playground")
public class PlaygroundConfig {
    @Bean
    public Greeter greeter(NameFormatter nameFormatter){
        return new Greeter(nameFormatter);
    }

    @Bean
    public NameFormatter nameFormatter() {
        return new NameFormatter();
    }
}
