package playground;

import org.springframework.stereotype.Component;

@Component
public interface NameFormatter {
    public String format(String name);
}
