package fr.pasteque.client.models;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nsvir on 28/10/15.
 * n.svirchevsky@gmail.com
 */
public class UnshareableTicket extends Ticket {

    public UnshareableTicket(String id, String ticketId) {
        super(id, ticketId);
    }

    @Override
    protected void updateTicket() {
        //disable updateTicket
    }

    public static UnshareableTicket fromJSON(Context context, JSONObject o) throws JSONException {
        return (UnshareableTicket) Ticket.fromJSON(context, o, new UnshareableInstance());
    }

    protected static class UnshareableInstance extends Ticket.TicketInstance {
        @Override
        public Ticket newTicket(String id, String label) {
            return new UnshareableTicket(id, label);
        }
    }
}
