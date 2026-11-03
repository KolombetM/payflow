package playground;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

public class ContainerPlayground {
    static void main() {
        System.out.println("1. before context");
        var context = new AnnotationConfigApplicationContext(PlaygroundConfig.class);
        System.out.println("2. context created");
        Greeter bean1 = context.getBean(Greeter.class);
        System.out.println("3. first getBean done");
        Greeter bean2 = context.getBean(Greeter.class);
        System.out.println(bean1 == bean2);
        System.out.println(bean1.greet("       nick"));

        FormatterRegistry formatterRegistry = context.getBean(FormatterRegistry.class);
        Set<String> keys  = formatterRegistry.getKeys();
        System.out.println(keys);
        for(String key : keys){
            System.out.println(formatterRegistry.format(key, "nick"));
        }
        context.close();
    }
}
