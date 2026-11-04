package playground;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class TicketPrinter {

    private final Ticket ticket;
    public TicketPrinter(Ticket ticket) {
        this.ticket = ticket;
    }

    public void print() {
        System.out.println("Ticket Id: " + this.ticket.getId());
    }
}
