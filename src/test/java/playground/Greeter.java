package playground;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Greeter {
    private final NameFormatter nameFormatter;

    public Greeter(@Qualifier("upperCaseNameFormatter") NameFormatter nameFormatter){
        this.nameFormatter = nameFormatter;
        System.out.println("Create greeter bean");
    }

    @PostConstruct
    public void init(){
        System.out.println("Greeter @PostConstruct");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("Greeter @PreDestroy");
    }

    public String greet(String name) {
        return "Hello, " + this.nameFormatter.format(name);
    }
}
