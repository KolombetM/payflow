package playground;

import org.springframework.stereotype.Component;

@Component
public class UpperCaseNameFormatter implements NameFormatter {

    public UpperCaseNameFormatter() {
        System.out.println("Create uppercase formatter");
    }

    @Override
    public String format(String name) {
        if (name == null) return "";
        name = name.trim();
        if (name.isEmpty()) return "";
        return name.toUpperCase();
    }
}
