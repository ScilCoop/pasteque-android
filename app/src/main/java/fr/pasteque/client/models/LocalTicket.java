package fr.pasteque.client.models;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nsvir on 28/10/15.
 * n.svirchevsky@gmail.com
 */
public class LocalTicket extends Ticket {

    private boolean shareable = false;

    public LocalTicket(String label) {
        super(label);
    }

    public LocalTicket(String id, String ticketId) {
        super(id, ticketId);
    }

    @Override
    protected void updateTicket() {
        if (shareable) {
            super.updateTicket();
        }
        //disable updateTicket
    }

    public void switchShareable() {
        this.shareable = true;
    }

    public static LocalTicket fromJSON(Context context, JSONObject o) throws JSONException {
        return (LocalTicket) Ticket.fromJSON(context, o, new LocalTicketInstance());
    }

    protected static class LocalTicketInstance extends Ticket.TicketInstance {
        @Override
        public Ticket newTicket(String id, String label) {
            return new LocalTicket(id, label);
        }
    }
}
