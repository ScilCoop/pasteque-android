package fr.pasteque.client.viewer.models;

import java.util.ArrayList;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 17:01.
 */
public class SharedTicketsHolder extends ArrayList<Ticket> {
    public void update(Ticket ticket) {
        int index = 0;
        while (index < this.size()) {
            if (this.get(index).id.equals(ticket.id)) {
                this.set(index, ticket);
                return;
            }
            index++;
        }
        this.add(ticket);
    }
}
