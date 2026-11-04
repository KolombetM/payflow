package playground;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class TicketPrinter {

    private final ObjectProvider<Ticket> tickets;
    public TicketPrinter(ObjectProvider<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void print() {
        System.out.println("Ticket Id: " + this.tickets.getObject().getId());
    }
}
