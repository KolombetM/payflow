package playground;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class FormatterRegistry {
    private final Map<String, NameFormatter> formatters;

    public FormatterRegistry(Map<String, NameFormatter> formatters) {
        this.formatters = formatters;
    }

    public Set<String> getKeys(){
        return this.formatters.keySet();
    }

    public String format(String formatterName, String name) {
        try {
            return formatters.get(formatterName).format(name);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formatter  " + formatterName + " not found");
        }
    }
}
