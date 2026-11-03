package playground;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Greeter {
    private final NameFormatter nameFormatter;

    public Greeter(@Qualifier("upperCaseNameFormatter") NameFormatter nameFormatter){
        this.nameFormatter = nameFormatter;
        System.out.println("Create greeter bean");
    }

    public String greet(String name) {
        return "Hello, " + this.nameFormatter.format(name);
    }
}
