package fr.pasteque.client.viewer.models;

import fr.pasteque.api.models.TicketLineModel;
import fr.pasteque.api.models.TicketModel;

import java.util.ArrayList;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 17:02.
 */
public class Ticket {

    public String id;
    public ArrayList<TicketLine> lines = new ArrayList<>();

    public Ticket(TicketModel ticketModel) {
        this.copy(ticketModel);
    }

    private void copy(TicketModel ticketModel) {
        this.id = ticketModel.id;
        lines.clear();
        for (TicketLineModel line: ticketModel.lines) {
            lines.add(new TicketLine(line));
        }
    }
}
