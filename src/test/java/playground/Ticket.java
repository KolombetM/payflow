package playground;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope("prototype")
public class Ticket {
    private final String id;

    Ticket() {
        this.id = UUID.randomUUID().toString();
        System.out.println("Created Ticket with id: " + id);
    }

    public String getId() {
        return id;
    }
}
