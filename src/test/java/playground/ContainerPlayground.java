package playground;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ContainerPlayground {
    static void main() {
        System.out.println("1. before context");
        var context = new AnnotationConfigApplicationContext(PlaygroundConfig.class);
        System.out.println("2. context created");
        Greeter bean1 = context.getBean(Greeter.class);
        System.out.println("3. first getBean done");
        Greeter bean2 = context.getBean(Greeter.class);
        System.out.println(bean1 == bean2);
        System.out.println(bean1.greet("Nick"));;
        context.close();
    }
}
