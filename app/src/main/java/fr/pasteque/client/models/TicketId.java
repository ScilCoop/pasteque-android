package fr.pasteque.client.models;

import java.io.Serializable;

/**
 * Created by nsvir on 18/08/15.
 * n.svirchevsky@gmail.com
 */
public class TicketId implements Serializable {

    private boolean noNewTicket;
    private int numberTicket;

    public TicketId(int ticketId) {
        this.noNewTicket = true;
        this.numberTicket = ticketId;
    }

    public void notifyDataJustSent() {
        this.noNewTicket = true;
    }

    public boolean hasNotCreatedTickets() {
        return this.noNewTicket;
    }

    public int getId() { return this.numberTicket; }

    public int newTicketId() {
        return this.numberTicket;
    }

    public void ticketClosed() {
        this.noNewTicket = false;
        this.numberTicket++;
    }
}
