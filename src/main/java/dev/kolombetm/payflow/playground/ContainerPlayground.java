package dev.kolombetm.payflow.playground;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContainerPlayground {
    static void main() {
        System.out.println("1. before context");
        var context = new AnnotationConfigApplicationContext(PlaygroundConfig.class);
        System.out.println("2. context created");
        Greeter bean1 = context.getBean(Greeter.class);
        System.out.println("3. first getBean done");
    }
}
