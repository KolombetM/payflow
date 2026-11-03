package playground;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CapitalizingNameFormatter implements NameFormatter {

    public CapitalizingNameFormatter() {
        System.out.println("Create capitalizing formatter");
    }

    @Override
    public String format(String name) {
        if (name == null) return "";
        name = name.trim();
        if (name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
