package playground;

import org.springframework.stereotype.Component;

@Component
public class Greeter {
    private final NameFormatter nameFormatter;

    public Greeter(NameFormatter nameFormatter){
        this.nameFormatter = nameFormatter;
        System.out.println("Create greeter bean");
    }

    public String greet(String name) {
        return "Hello, " + this.nameFormatter.format(name);
    }
}
