package playground;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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

    @PostConstruct
    public void init(){
        System.out.println("Ticket @PostConstruct");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("Ticket @PreDestroy");
    }

    public String getId() {
        return id;
    }
}
