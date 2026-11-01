package playground;

import org.springframework.stereotype.Component;

@Component
public class NameFormatter {
    public NameFormatter(){
        System.out.println("Create formatter");
    }

    public String format(String name) {
        name = name.trim();
        if (name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
