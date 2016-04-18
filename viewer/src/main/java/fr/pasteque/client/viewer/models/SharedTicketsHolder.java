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
                this.get(index).update(ticket);
                this.get(index).updated = true;
                return;
            }
            index++;
        }
        ticket.updated = true;
        this.add(ticket);
    }

    public void updating() {
        for (Ticket each: this){
            each.updated = false;
        }
    }

    public void updated() {
        ArrayList<Ticket> trash = new ArrayList<>();
        for (Ticket each: this){
            if (!each.updated) {
                trash.add(each);
            }
        }
        this.removeAll(trash);
    }
}
